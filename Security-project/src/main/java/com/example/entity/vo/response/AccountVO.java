package com.example.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountVO {
    int id;
    String username;
    String email;
    String role;
    String avatar;
    Date registerTime;
}
