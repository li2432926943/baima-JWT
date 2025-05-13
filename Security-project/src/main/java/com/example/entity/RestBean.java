package com.example.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.slf4j.MDC;

import java.util.Optional;

/**
 * Rest风格的统一响应实体类
 * 使用泛型设计，可以适应不同类型的响应数据
 * 提供静态工厂方法，根据不同参数创建不同类型的响应对象
 *
 * @param id 响应ID，用于链路追踪
 * @param code 状态码，如200成功，4xx错误等
 * @param data 响应数据，类型由泛型T决定
 * @param message 响应消息，如"请求成功"或错误说明
 * @param <T> 响应数据的类型参数，如String、Map、实体类等
 */
public record RestBean<T> (long id, int code, T data, String message) {
    
    /**
     * 创建成功响应对象（包含数据）
     * 当API需要返回数据时使用此方法
     * 
     * @param data 响应的数据对象，将被包装在返回的RestBean中
     * @param <T> 数据类型参数，会自动根据传入参数类型推断
     * @return 包含传入数据、成功状态码和默认成功消息的RestBean对象
     */
    public static <T> RestBean<T> success(T data){
        // 根据传入参数创建成功响应对象，状态码200，消息"请求成功"
        return new RestBean<>(requestId(), 200, data, "请求成功");
    }
    
    /**
     * 创建成功响应对象（不包含数据）
     * 当API成功执行但不需要返回数据时使用此方法
     * 
     * @param <T> 数据类型参数
     * @return 包含null数据、成功状态码和默认成功消息的RestBean对象
     */
    public static <T> RestBean<T> success(){
        // 通过调用另一个工厂方法创建对象，传入null表示无数据
        return success(null);
    }

    /**
     * 创建禁止访问(403)响应对象
     * 当用户无权限访问资源时使用此方法
     * 
     * @param message 错误消息
     * @param <T> 数据类型参数
     * @return 包含403状态码和指定错误消息的RestBean对象
     */
    public static <T> RestBean<T> forbidden(String message){
        // 通过调用通用failure方法创建对象，设置403状态码
        return failure(403, message);
    }

    /**
     * 创建未授权(401)响应对象
     * 当用户未登录或认证失败时使用此方法
     * 
     * @param message 错误消息
     * @param <T> 数据类型参数
     * @return 包含401状态码和指定错误消息的RestBean对象
     */
    public static <T> RestBean<T> unauthorized(String message){
        // 通过调用通用failure方法创建对象，设置401状态码
        return failure(401, message);
    }

    /**
     * 创建通用失败响应对象
     * 工厂方法根据传入的状态码和消息创建错误响应
     * 
     * @param code 状态码
     * @param message 错误消息
     * @param <T> 数据类型参数
     * @return 包含指定状态码和错误消息的RestBean对象
     */
    public static <T> RestBean<T> failure(int code, String message){
        // 直接创建对象，设置指定状态码和消息，数据为null
        return new RestBean<>(requestId(), code, null, message);
    }

    /**
     * 将RestBean对象转换为JSON字符串
     * 在控制器返回响应时使用此方法
     * 
     * @return JSON格式的字符串表示
     */
    public String asJsonString() {
        // 使用FastJSON库将对象转换为JSON字符串，保留null值
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }

    /**
     * 获取当前请求ID
     * 用于日志跟踪和错误排查
     * 
     * @return 请求ID，如果MDC中不存在则返回0
     */
    private static long requestId(){
        // 尝试从MDC中获取请求ID，如果不存在则默认为"0"
        String requestId = Optional.ofNullable(MDC.get("reqId")).orElse("0");
        return Long.parseLong(requestId);
    }
}