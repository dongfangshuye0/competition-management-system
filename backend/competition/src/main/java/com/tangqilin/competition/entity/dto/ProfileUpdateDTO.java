package com.tangqilin.competition.entity.dto;

import lombok.Data;

@Data
public class ProfileUpdateDTO {
    private String name;
    private String major;
    private String className;
    private String phone;
    private String email;
    private String password;
}
