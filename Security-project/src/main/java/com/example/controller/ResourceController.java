package com.example.controller;

import com.example.entity.RestBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 资源控制器，演示不同权限级别的资源访问
 */
@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    /**
     * 所有已认证用户都可访问的资源
     */
    @GetMapping("/protected")
    public ResponseEntity<?> getProtectedResource() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(RestBean.success("受保护资源访问成功! 当前用户: " + authentication.getName()));
    }

    /**
     * 只有USER角色可访问的资源
     */
    @GetMapping("/user")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getUserResource() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(RestBean.success("用户资源访问成功! 当前用户: " + authentication.getName()));
    }

    /**
     * 只有ADMIN角色可访问的资源
     */
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAdminResource() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(RestBean.success("管理员资源访问成功! 当前管理员: " + authentication.getName()));
    }
}