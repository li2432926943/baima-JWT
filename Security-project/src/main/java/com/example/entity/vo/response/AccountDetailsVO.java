package com.example.entity.vo.response;

import com.example.entity.BaseData;
import lombok.Data;

@Data
public class AccountDetailsVO implements BaseData {
    int gender;
    String phone;
    String qq;
    String wx;
    String desc;
}
