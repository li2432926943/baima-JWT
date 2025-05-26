package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.AuthRequestVO;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.service.AccountService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    @Resource
    JwtUtils jwtUtils;
    
    @Resource
    AccountService accountService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestVO request) {
        try {
            // 创建用于认证的令牌
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
            
            // 尝试认证
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            // 认证成功，设置SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 获取用户信息
            User user = (User) authentication.getPrincipal();
            String username = user.getUsername();
            String role = user.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse("ROLE_USER");
            
            // 查询数据库获取真实用户ID
            Account account = accountService.findAccountByNameOrEmail(username);
            if(account == null) {
                return ResponseEntity.status(500)
                    .body(RestBean.failure(500, "无法获取用户信息，请联系管理员"));
            }
            int userId = account.getId();
            
            // 生成JWT令牌
            String token = jwtUtils.createJwt(user, username, userId);
            
            if(token == null) {
                return ResponseEntity.status(429)
                    .body(RestBean.failure(429, "登录频率过高，请稍后再试"));
            }
            
            // 创建响应对象
            AuthorizeVO authorizeVO = new AuthorizeVO();
            authorizeVO.setUsername(username);
            authorizeVO.setRole(role);
            authorizeVO.setToken(token);
            authorizeVO.setExpire(jwtUtils.expireTime());
            
            // 返回成功响应
            return ResponseEntity.ok(RestBean.success(authorizeVO));
        } catch (AuthenticationException e) {
            // 认证失败
            return ResponseEntity.status(401)
                .body(RestBean.failure(401, "用户名或密码错误: " + e.getMessage()));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(RestBean.success("认证成功，您已登录!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            boolean invalidated = jwtUtils.invalidateJwt(authorization);
            if(invalidated) {
                SecurityContextHolder.clearContext();
                return ResponseEntity.ok(RestBean.success("退出登录成功"));
            } else {
                return ResponseEntity.badRequest().body(RestBean.failure(400, "退出登录失败，令牌无效"));
            }
        }
        return ResponseEntity.badRequest().body(RestBean.failure(400, "退出登录失败，未提供有效令牌"));
    }
}