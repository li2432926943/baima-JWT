package com.example.filter;

import com.example.utils.JwtUtils;
import com.example.utils.Const;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 从请求头提取JWT令牌并验证，验证通过后将用户信息存入SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Resource
    JwtUtils jwtUtils;
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        System.out.println("==========JWT过滤器检查==========");
        System.out.println("请求路径: " + path);
        System.out.println("请求方法: " + request.getMethod());
        
        // 检查路径是否匹配/api/auth/**模式
        boolean shouldSkip = path.startsWith("/api/auth/");
        System.out.println("是否跳过JWT过滤器: " + shouldSkip);
        System.out.println("==============================");
        return shouldSkip;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("开始处理JWT过滤器...");
        String authorization = request.getHeader("Authorization");
        System.out.println("Authorization头: " + (authorization != null ? authorization.substring(0, Math.min(20, authorization.length())) + "..." : "null"));
        
        if (authorization != null && authorization.startsWith("Bearer ")) {
            // 解析JWT令牌
            DecodedJWT jwt = jwtUtils.resolveJwt(authorization);
            
            // 如果JWT有效，提取用户信息并设置认证
            if (jwt != null) {
                System.out.println("JWT令牌有效");
                UserDetails userDetails = jwtUtils.toUser(jwt);
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // 从JWT中获取用户ID并设置到请求属性中
                Integer userId = jwtUtils.toId(jwt);
                if(userId != null) {
                    request.setAttribute(Const.ATTR_USER_ID, userId);
                    System.out.println("已设置用户ID到请求属性: " + userId);
                } else {
                    System.out.println("无法从JWT中提取用户ID");
                }
            } else {
                System.out.println("JWT令牌无效");
            }
        } else {
            System.out.println("请求中没有JWT令牌");
        }
        
        filterChain.doFilter(request, response);
        System.out.println("JWT过滤器处理完成");
    }
}