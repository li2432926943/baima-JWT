package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {
    @Resource
    AccountService service;

    /**
     * 获取验证码接口
     */
    @PermitAll
    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern(regexp = "(register|reset|modify)") String type,
                                        HttpServletRequest request) {
        System.out.println("==========接收到验证码请求==========");
        System.out.println("Email: " + email);
        System.out.println("Type: " + type);
        System.out.println("IP地址: " + request.getRemoteAddr());
        
        String message = service.registerEmailVerifyCode(type, email, request.getRemoteAddr());
        
        System.out.println("验证码请求处理结果: " + (message == null ? "成功" : "失败: " + message));
        
        return message == null ? RestBean.success() : RestBean.failure(400, message);
    }
    
    /**
     * 进行用户注册操作，需要先请求邮件验证码
     * @param vo 注册信息
     * @return 是否注册成功
     */
    @PermitAll
    @PostMapping("/register")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVO vo){
        System.out.println("==========接收到注册请求==========");
        System.out.println("Email: " + vo.getEmail());
        System.out.println("用户名: " + vo.getUsername());
        System.out.println("验证码: " + vo.getCode());
        System.out.println("密码长度: " + (vo.getPassword() != null ? vo.getPassword().length() : 0));
        
        try {
            String result = service.registerEmailAccount(vo);
            System.out.println("注册请求处理结果: " + (result == null ? "成功" : "失败: " + result));
            return this.messageHandler(() -> result);
        } catch (Exception e) {
            System.out.println("注册请求处理异常: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return RestBean.failure(500, "服务器内部错误: " + e.getMessage());
        }
    }

    public RestBean<Void> messageHandler(Supplier<String> action){
        String message = action.get();
        return message == null ? RestBean.success() : RestBean.failure(400, message);
    }
}
