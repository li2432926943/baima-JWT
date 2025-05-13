package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.*;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户账户服务接口
 */
public interface AccountService extends IService<Account>, UserDetailsService {
    /**
     * 根据用户名或邮箱查找账户
     * @param text 用户名或邮箱
     * @return 账户信息
     */
    Account findAccountByNameOrEmail(String text);
    
    /**
     * 根据ID查找账户
     * @param id 用户ID
     * @return 账户信息
     */
    Account findAccountById(int id);
    
    /**
     * 创建新账户
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @return 是否创建成功
     */
    boolean createAccount(String username, String password, String email);
    
    /**
     * 发送电子邮件验证码
     * @param type 类型
     * @param email 邮箱
     * @param address IP地址
     * @return 操作结果
     */
    String registerEmailVerifyCode(String type,String email,String address);
    
    /**
     * 通过邮箱注册账户
     * @param vo 注册信息
     * @return 操作结果
     */
    String registerEmailAccount(EmailRegisterVO vo);
}
