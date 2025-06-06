package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Account;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户账户数据访问接口
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
} 