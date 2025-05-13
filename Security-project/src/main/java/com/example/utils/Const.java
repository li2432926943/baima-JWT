package com.example.utils;

/**
 * 常量类，存储系统中使用的常量
 */
public class Const {
    // JWT相关常量
    public static final String JWT_BLACK_LIST = "jwt:blacklist:";
    public static final String JWT_FREQUENCY = "jwt:frequency:";
    
    // 过滤器优先级
    public static final int ORDER_CORS = -102;
    public static final int ORDER_JWT = -101;

    //邮件验证码
    public final static String VERIFY_EMAIL_LIMIT = "verify:email:limit:";
    public final static String VERIFY_EMAIL_DATA = "verify:email:data:";

    //消息队列
    public final static String MQ_MAIL = "mail";

} 