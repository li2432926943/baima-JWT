package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.entity.mapper.AccountMapper;
import com.example.entity.vo.request.*;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 用户账户服务实现类
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {



    @Resource
    AmqpTemplate rabbitTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    PasswordEncoder encoder;

    @Resource
    FlowUtils flow;

    @Override
    public Account findAccountByNameOrEmail(String text) {
        QueryWrapper<Account> wrapper = new QueryWrapper<>();
        wrapper.eq("username", text).or().eq("email", text);
        return this.getOne(wrapper);
    }
    
    @Override
    public Account findAccountById(int id) {
        return this.getById(id);
    }
    
    @Override
    public boolean createAccount(String username, String password, String email) {
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(encoder.encode(password));
        account.setEmail(email);
        account.setRole("ROLE_USER"); // 默认角色
        account.setRegisterTime(new Date());
        return this.save(account);
    }

    @Override
    public String registerEmailVerifyCode(String type, String email, String address) {
        if(!this.verifyLimit(address))
            return "请求频繁，请稍后再试";
        Random random = new Random();
        int code=random.nextInt(899999)+100000;
        Map<String, Object> data = Map.of("type",type,"email", email, "code", code);
        rabbitTemplate.convertAndSend(Const.MQ_MAIL, data);
        stringRedisTemplate.opsForValue()
                .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);
        return null;
    }

    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        String email=vo.getEmail();
        String username=vo.getUsername();
        String code=stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA+email);
        String key=Const.VERIFY_EMAIL_DATA+email;
        if(code==null) return "请先获取验证码";
        if(!code.equals(vo.getCode())) return "验证码错误,请重新输入";
        if(this.existsAccountByEmail(email)) return "此电子邮件已被其他用户注册";
        if(this.existsAccountByUsername(username)) return "此用户名已被其他用户注册";
        String password=encoder.encode(vo.getPassword());
        Account account = new Account(null, username, password, email, "ROLE_USER", null, new Date());
        if(this.save(account)){
            stringRedisTemplate.delete(key);
            return null;
        }else{
            return "内部错误，请联系管理员";
        }
    }

    public boolean existsAccountByEmail(String email){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email",email));
    }
    public boolean existsAccountByUsername(String username){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username",username));
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if(account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(account.getUsername())
                .password(account.getPassword())
                .authorities(account.getRole())
                .build();
    }

    private boolean verifyLimit(String address)
    {
        String key= Const.VERIFY_EMAIL_LIMIT+address;
        return flow.limitOnceCheck(key,60);
    }
} 