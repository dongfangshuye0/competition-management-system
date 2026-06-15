package com.tangqilin.competition.entity.dto;

import lombok.Data;

@Data
public class UserUpsertDTO {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private String major;
    private String className;
    private String phone;
    private String email;
}
