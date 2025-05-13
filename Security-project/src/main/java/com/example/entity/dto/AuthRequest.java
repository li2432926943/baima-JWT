package com.example.entity.dto;

import lombok.Data;

/**
 * 用户登录请求数据传输对象
 */
@Data
public class AuthRequest {
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
} 