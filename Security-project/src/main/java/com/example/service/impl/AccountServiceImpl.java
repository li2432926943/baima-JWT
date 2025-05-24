package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.*;
import com.example.mapper.AccountMapper;
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
    PasswordEncoder passwordEncoder;

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
        account.setPassword(passwordEncoder.encode(password));
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
        
        // 添加调试日志
        String redisKey = Const.VERIFY_EMAIL_DATA + email;
        System.out.println("存储验证码到Redis - 键: " + redisKey + ", 值: " + code);
        
        stringRedisTemplate.opsForValue()
                .set(redisKey, String.valueOf(code), 3, TimeUnit.MINUTES);
        
        // 验证是否成功存储
        String storedCode = stringRedisTemplate.opsForValue().get(redisKey);
        System.out.println("验证Redis存储 - 键: " + redisKey + ", 存储的值: " + storedCode);
        
        return null;
    }

    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        String email=vo.getEmail();
        String username=vo.getUsername();
        
        // 添加调试日志
        System.out.println("注册请求 - 邮箱: [" + email + "], 用户名: [" + username + "], 验证码: [" + vo.getCode() + "]");
        
        String redisKey = Const.VERIFY_EMAIL_DATA + email;
        System.out.println("从Redis获取验证码 - 键: " + redisKey);
        
        String code=stringRedisTemplate.opsForValue().get(redisKey);
        System.out.println("Redis中存储的验证码: " + code);
        
        if(code==null) return "请先获取验证码";
        if(!code.equals(vo.getCode())) return "验证码错误,请重新输入";
        if(this.existsAccountByEmail(email)) return "此电子邮件已被其他用户注册";
        if(this.existsAccountByUsername(username)) return "此用户名已被其他用户注册";
        String password=passwordEncoder.encode(vo.getPassword());
        Account account = new Account(null, username, password, email, "ROLE_USER", new Date(), null);
        if(this.save(account)){
            stringRedisTemplate.delete(redisKey);
            return null;
        }else{
            return "内部错误，请联系管理员";
        }
    }

    @Override
    public String resetConfirm(ConfirmResetVO vo) {
         String email=vo.getEmail();
         String code=stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA+email);
         if(code==null) return "请先获取验证码";
         if(!code.equals(vo.getCode()))return "验证码错误,请重新输入";
         return null;
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVO vo) {
         String email=vo.getEmail();
         String verify=this.resetConfirm(new ConfirmResetVO(email,vo.getCode()));
         if(verify!=null) return verify;
         String password=passwordEncoder.encode(vo.getPassword());
         boolean update=this.update().eq("email",email).set("password",password).update();
         if(update){
             //  删除验证码
             stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA+email);
         }
         return null;
    }

    @Override
    public String modifyEmail(int id, ModifyEmailVO vo) {
        String email = vo.getEmail();
        String code=getEmailVerifyCode(email);
        if(code==null) return "请先获取验证码";
        if(!code.equals(vo.getCode()))return "验证码错误,请重新输入";
        this.deleteEmailVerifyCode(email);
        Account account=this.findAccountByNameOrEmail(email);
        if(account != null && account.getId()!=id)
            return "此电子邮件已被其他用户注册";
        this.update()
                .set("email",email)
                .eq("id",id)
                .update();
        return null;
    }

    @Override
    public String changePassword(int id, ChangePasswordVO vo) {
        String password=this.query().eq("id",id).one().getPassword();
        if(!passwordEncoder.matches(vo.getPassword(),password))
            return "原密码错误，请重新输入";
        boolean success=this.update()
                .eq("id",id)
                .set("password",passwordEncoder.encode(vo.getNew_password()))
                .update();
        return success ? null:"未知错误，请联系管理员";
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
    /**
     * 获取Redis中存储的邮件验证码
     * @param email 电邮
     * @return 验证码
     */
    private String getEmailVerifyCode(String email){
        String key = Const.VERIFY_EMAIL_DATA + email;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 移除Redis中存储的邮件验证码
     * @param email 电邮
     */
    private void deleteEmailVerifyCode(String email){
        String key = Const.VERIFY_EMAIL_DATA + email;
        stringRedisTemplate.delete(key);
    }

} 