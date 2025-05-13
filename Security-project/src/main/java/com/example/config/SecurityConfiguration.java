package com.example.config;

import com.example.entity.RestBean;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.filter.JwtAuthenticationFilter;
import com.example.service.AccountService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Security安全配置类
 * 用于配置认证、授权规则和JWT相关处理
 */
@Configuration
@EnableMethodSecurity // 启用方法级别的安全注解
public class SecurityConfiguration {

    @Resource
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Resource
    JwtUtils jwtUtils;
    
    @Resource
    PasswordEncoder passwordEncoder;
    
    @Resource
    AccountService accountService; // 注入AccountService

    /**
     * 配置Spring Security的安全过滤器链
     * 
     * @param http HttpSecurity配置对象
     * @return 配置好的SecurityFilterChain
     * @throws Exception 可能抛出的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("==========配置SecurityFilterChain==========");
        return http
                // 添加一个调试过滤器
                .addFilterBefore((request, response, chain) -> {
                    System.out.println("==========收到HTTP请求==========");
                    System.out.println("请求方法: " + ((HttpServletRequest)request).getMethod());
                    System.out.println("请求路径: " + ((HttpServletRequest)request).getRequestURI());
                    System.out.println("Content-Type: " + ((HttpServletRequest)request).getContentType());
                    System.out.println("=================================");
                    chain.doFilter(request, response);
                }, UsernamePasswordAuthenticationFilter.class)
                // 配置请求授权规则
                .authorizeHttpRequests(conf -> {
                    System.out.println("配置请求授权规则...");
                    conf.requestMatchers("/api/auth/**").permitAll()
                       .anyRequest().authenticated();
                })
                // 禁用表单登录，改为使用自定义的JSON登录接口
                .formLogin(AbstractHttpConfigurer::disable)
                // 配置登出
                .logout(conf -> conf
                        // 设置登出URL
                        .logoutUrl("/api/auth/logout")
                        // 设置登出成功处理器，将JWT加入黑名单
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                // 配置异常处理
                .exceptionHandling(conf -> {
                    System.out.println("配置异常处理...");
                    conf.accessDeniedHandler(this::handleProcess)
                       .authenticationEntryPoint((request, response, exception) -> {
                           System.out.println("认证失败，请求路径: " + request.getRequestURI());
                           System.out.println("认证失败，异常类型: " + exception.getClass().getName());
                           System.out.println("认证失败，异常消息: " + exception.getMessage());
                           this.handleProcess(request, response, exception);
                       });
                })
                // 禁用CSRF保护，因为我们使用JWT是无状态的
                .csrf(AbstractHttpConfigurer::disable)
                // 配置会话管理，指定为无状态（不使用Session）
                .sessionManagement(conf -> conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 添加JWT认证过滤器，放在用户名密码认证过滤器之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 构建过滤器链
                .build();
    }

    /**
     * 通用处理方法，根据传入参数类型处理不同情况
     * 使用RestBean统一返回格式
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param exceptionOrAuthentication 可能是异常对象或认证对象
     * @throws IOException 可能的IO异常
     */
    private void handleProcess(HttpServletRequest request,
                              HttpServletResponse response,
                              Object exceptionOrAuthentication) throws IOException {
        // 设置响应内容类型为JSON
        response.setContentType("application/json;charset=utf-8");
        // 获取输出流
        PrintWriter writer = response.getWriter();
        
        // 打印调试信息
        System.out.println("处理异常或认证: " + exceptionOrAuthentication.getClass().getName());
        
        // 根据参数类型分别处理
        if(exceptionOrAuthentication instanceof AccessDeniedException exception) {
            // 处理访问被拒绝异常（权限不足）
            response.setStatus(403);
            writer.write(RestBean.forbidden("权限不足: " + exception.getMessage()).asJsonString());
        } else if(exceptionOrAuthentication instanceof Exception exception) {
            // 处理其他异常（包括未认证异常）
            response.setStatus(401);
            writer.write(RestBean.unauthorized("认证失败: " + exception.getMessage()).asJsonString());
        } else if(exceptionOrAuthentication instanceof Authentication authentication){
            // 处理认证成功的情况
            // 从认证对象中获取用户信息
            User user = (User) authentication.getPrincipal();
            String username = user.getUsername();
            // 获取用户角色，通常是第一个权限
            String role = user.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse("ROLE_USER");  // 默认角色
            // 这里假设用户ID，实际情况中应该从用户服务或数据库中获取
            int userId = getUserIdByUsername(username);
            // 生成JWT令牌，传入额外的参数
            String token = jwtUtils.createJwt(user, username, userId);
            
            if(token == null) {
                // 令牌生成失败（可能是因为生成过于频繁）
                writer.write(RestBean.forbidden("登录验证频繁，请稍后再试").asJsonString());
            } else {
                // 登录成功，返回JWT令牌和用户信息
                AuthorizeVO authorizeVO = new AuthorizeVO();
                authorizeVO.setUsername(username);
                authorizeVO.setRole(role);
                authorizeVO.setToken(token);
                authorizeVO.setExpire(jwtUtils.expireTime());
                writer.write(RestBean.success(authorizeVO).asJsonString());
            }
        }
    }

    /**
     * 根据用户名获取用户ID
     * 注意：这只是一个简单实现，实际应用中应该从数据库或用户服务中获取
     * 
     * @param username 用户名
     * @return 用户ID
     */
    private int getUserIdByUsername(String username) {
        // 这里应该实现从数据库或用户服务中获取用户ID的逻辑
        // 为了简化，这里只返回一个基于用户名哈希值的模拟ID
        return Math.abs(username.hashCode()) % 10000;
    }

    /**
     * 处理退出登录成功的情况
     * 使用RestBean统一返回格式
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param authentication 认证信息（可能为null）
     * @throws IOException 可能的IO异常
     */
    private void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException {
        // 设置响应内容类型为JSON
        response.setContentType("application/json;charset=utf-8");
        // 获取输出流
        PrintWriter writer = response.getWriter();
        
        // 从请求头中获取JWT令牌
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            // 使用jwtUtils让令牌失效
            boolean invalidated = jwtUtils.invalidateJwt(authorization);
            if (invalidated) {
                // 返回成功响应
                writer.write(RestBean.success("退出登录成功").asJsonString());
            } else {
                // 令牌失效操作失败
                response.setStatus(400); // 设置HTTP状态码为400
                writer.write(RestBean.failure(400, "退出登录失败").asJsonString());
            }
        } else {
            // 没有找到有效的令牌
            response.setStatus(400); // 设置HTTP状态码为400
            writer.write(RestBean.failure(400, "退出登录失败").asJsonString());
        }
    }
    
    /**
     * 配置认证管理器，使用注入的AccountService
     * 
     * @return 认证管理器实例
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        // 使用AccountService作为UserDetailsService
        provider.setUserDetailsService(accountService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(Collections.singletonList(provider));
    }
}