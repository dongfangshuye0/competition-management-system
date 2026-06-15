package com.tangqilin.competition.service;

import com.tangqilin.competition.common.result.Result;
import com.tangqilin.competition.entity.dto.LoginDTO;
import com.tangqilin.competition.entity.dto.ProfileUpdateDTO;
import com.tangqilin.competition.entity.dto.RegisterDTO;
import com.tangqilin.competition.entity.dto.UserUpsertDTO;

public interface UserService {
    Result<?> login(LoginDTO loginDTO);
    Result<?> register(RegisterDTO registerDTO);
    Result<?> currentUser();
    Result<?> updateProfile(ProfileUpdateDTO profileUpdateDTO);
    Result<?> listUsers();
    Result<?> saveUser(UserUpsertDTO userUpsertDTO);
    Result<?> deleteUser(Long id);
    Result<?> listTeachers();
}
