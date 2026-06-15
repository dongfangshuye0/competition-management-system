package com.tangqilin.competition.controller;

import com.tangqilin.competition.service.UserService;
import com.tangqilin.competition.common.result.Result;
import com.tangqilin.competition.entity.dto.ProfileUpdateDTO;
import com.tangqilin.competition.entity.dto.UserUpsertDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Result<?> currentUser() {
        return userService.currentUser();
    }

    @PutMapping("/me")
    public Result<?> updateProfile(@RequestBody ProfileUpdateDTO profileUpdateDTO) {
        return userService.updateProfile(profileUpdateDTO);
    }

    @GetMapping
    public Result<?> listUsers() {
        return userService.listUsers();
    }

    @GetMapping("/teachers")
    public Result<?> listTeachers() {
        return userService.listTeachers();
    }

    @PostMapping
    public Result<?> createUser(@RequestBody UserUpsertDTO userUpsertDTO) {
        return userService.saveUser(userUpsertDTO);
    }

    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody UserUpsertDTO userUpsertDTO) {
        userUpsertDTO.setId(id);
        return userService.saveUser(userUpsertDTO);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}
