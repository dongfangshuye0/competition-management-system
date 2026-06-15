package com.tangqilin.competition.service.impl;

import com.tangqilin.competition.common.constant.Roles;
import com.tangqilin.competition.common.exception.BusinessException;
import com.tangqilin.competition.common.security.AuthContext;
import com.tangqilin.competition.common.result.Result;
import com.tangqilin.competition.entity.User;
import com.tangqilin.competition.entity.dto.LoginDTO;
import com.tangqilin.competition.entity.dto.ProfileUpdateDTO;
import com.tangqilin.competition.entity.dto.RegisterDTO;
import com.tangqilin.competition.entity.dto.UserUpsertDTO;
import com.tangqilin.competition.service.UserService;
import com.tangqilin.competition.storage.DatabaseStore;
import com.tangqilin.competition.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final List<String> ALLOWED_ROLES = List.of(Roles.STUDENT, Roles.TEACHER, Roles.ADMIN);

    private final JwtUtil jwtUtil;
    private final DatabaseStore databaseStore;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(JwtUtil jwtUtil, DatabaseStore databaseStore, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.databaseStore = databaseStore;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Result<?> login(LoginDTO loginDTO) {
        if (loginDTO == null || !StringUtils.hasText(loginDTO.getUsername()) || !StringUtils.hasText(loginDTO.getPassword())) {
            throw BusinessException.badRequest("请输入账号和密码");
        }

        User user = databaseStore.findUserByUsername(loginDTO.getUsername().trim())
                .orElseThrow(() -> BusinessException.badRequest("账号或密码错误"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw BusinessException.badRequest("账号或密码错误");
        }

        String token = jwtUtil.generateToken(user);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", toUserView(user));
        return Result.success(data);
    }

    @Override
    public Result<?> register(RegisterDTO registerDTO) {
        validateRegister(registerDTO);
        String username = registerDTO.getUsername().trim();
        databaseStore.findUserByUsername(username).ifPresent(user -> {
            throw BusinessException.badRequest("账号已存在");
        });

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword().trim()));
        user.setName(registerDTO.getName().trim());
        user.setRole(Roles.STUDENT);
        user.setMajor(trim(registerDTO.getMajor()));
        user.setClassName(trim(registerDTO.getClassName()));
        user.setPhone(trim(registerDTO.getPhone()));
        user.setEmail(trim(registerDTO.getEmail()));
        databaseStore.saveUser(user);
        return Result.success(toUserView(user));
    }

    @Override
    public Result<?> currentUser() {
        return Result.success(toUserView(current()));
    }

    @Override
    public Result<?> updateProfile(ProfileUpdateDTO profileUpdateDTO) {
        User user = current();
        if (profileUpdateDTO == null) {
            throw BusinessException.badRequest("请求参数不能为空");
        }
        if (StringUtils.hasText(profileUpdateDTO.getName())) {
            user.setName(profileUpdateDTO.getName().trim());
        }
        user.setMajor(trim(profileUpdateDTO.getMajor()));
        user.setClassName(trim(profileUpdateDTO.getClassName()));
        user.setPhone(trim(profileUpdateDTO.getPhone()));
        user.setEmail(trim(profileUpdateDTO.getEmail()));
        if (StringUtils.hasText(profileUpdateDTO.getPassword())) {
            validatePassword(profileUpdateDTO.getPassword());
            user.setPassword(passwordEncoder.encode(profileUpdateDTO.getPassword().trim()));
        }
        databaseStore.saveUser(user);
        return Result.success(toUserView(user));
    }

    @Override
    public Result<?> listUsers() {
        requireAdmin(current());
        List<Map<String, Object>> users = databaseStore.listUsers().stream()
                .map(this::toUserView)
                .toList();
        return Result.success(users);
    }

    @Override
    public Result<?> saveUser(UserUpsertDTO userUpsertDTO) {
        requireAdmin(current());
        if (userUpsertDTO == null) {
            throw BusinessException.badRequest("请求参数不能为空");
        }
        String username = trim(userUpsertDTO.getUsername());
        String name = trim(userUpsertDTO.getName());
        String role = trim(userUpsertDTO.getRole());
        if (!StringUtils.hasText(username) || !StringUtils.hasText(name) || !StringUtils.hasText(role)) {
            throw BusinessException.badRequest("账号、姓名、角色不能为空");
        }
        if (!ALLOWED_ROLES.contains(role)) {
            throw BusinessException.badRequest("角色不合法");
        }

        User user;
        if (userUpsertDTO.getId() == null) {
            if (!StringUtils.hasText(userUpsertDTO.getPassword())) {
                throw BusinessException.badRequest("新建用户时必须设置密码");
            }
            validatePassword(userUpsertDTO.getPassword());
            databaseStore.findUserByUsername(username).ifPresent(existing -> {
                throw BusinessException.badRequest("账号已存在");
            });
            user = new User();
            user.setPassword(passwordEncoder.encode(userUpsertDTO.getPassword().trim()));
        } else {
            user = databaseStore.findUserById(userUpsertDTO.getId())
                    .orElseThrow(() -> BusinessException.notFound("用户不存在"));
            if (!user.getUsername().equalsIgnoreCase(username)) {
                databaseStore.findUserByUsername(username).ifPresent(existing -> {
                    throw BusinessException.badRequest("账号已存在");
                });
            }
            if (StringUtils.hasText(userUpsertDTO.getPassword())) {
                validatePassword(userUpsertDTO.getPassword());
                user.setPassword(passwordEncoder.encode(userUpsertDTO.getPassword().trim()));
            }
        }

        user.setUsername(username);
        user.setName(name);
        user.setRole(role);
        user.setMajor(trim(userUpsertDTO.getMajor()));
        user.setClassName(trim(userUpsertDTO.getClassName()));
        user.setPhone(trim(userUpsertDTO.getPhone()));
        user.setEmail(trim(userUpsertDTO.getEmail()));
        databaseStore.saveUser(user);
        return Result.success(toUserView(user));
    }

    @Override
    public Result<?> deleteUser(Long id) {
        User currentUser = current();
        requireAdmin(currentUser);
        if (currentUser.getId().equals(id)) {
            throw BusinessException.badRequest("不能删除当前登录账号");
        }
        User target = databaseStore.findUserById(id)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        if (Roles.ADMIN.equals(target.getRole())) {
            long adminCount = databaseStore.countUsersByRole(Roles.ADMIN);
            if (adminCount <= 1) {
                throw BusinessException.badRequest("至少保留一个管理员账号");
            }
        }
        boolean hasBusinessRecords = databaseStore.existsRegistrationByUserIdOrTeacherId(target.getId())
                || databaseStore.existsScoreByTeacherId(target.getId());
        if (hasBusinessRecords) {
            throw BusinessException.badRequest("该用户已关联报名或评审记录，不能直接删除");
        }
        databaseStore.deleteUser(id);
        return Result.success();
    }

    @Override
    public Result<?> listTeachers() {
        List<Map<String, Object>> teachers = databaseStore.listUsersByRole(Roles.TEACHER).stream()
                .map(this::toUserView)
                .toList();
        return Result.success(teachers);
    }

    private User current() {
        User user = AuthContext.getUser();
        if (user == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return user;
    }

    private void requireAdmin(User user) {
        if (!Roles.ADMIN.equals(user.getRole())) {
            throw BusinessException.forbidden("只有管理员可以执行该操作");
        }
    }

    private void validateRegister(RegisterDTO registerDTO) {
        if (registerDTO == null) {
            throw BusinessException.badRequest("请求参数不能为空");
        }
        if (!StringUtils.hasText(registerDTO.getUsername()) || !StringUtils.hasText(registerDTO.getPassword()) || !StringUtils.hasText(registerDTO.getName())) {
            throw BusinessException.badRequest("账号、密码、姓名不能为空");
        }
        validatePassword(registerDTO.getPassword());
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password) || password.trim().length() < MIN_PASSWORD_LENGTH) {
            throw BusinessException.badRequest("密码长度不能少于6位");
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private Map<String, Object> toUserView(User user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("name", user.getName());
        data.put("role", user.getRole());
        data.put("major", user.getMajor());
        data.put("className", user.getClassName());
        data.put("phone", user.getPhone());
        data.put("email", user.getEmail());
        data.put("createTime", user.getCreateTime());
        data.put("updateTime", user.getUpdateTime());
        return data;
    }
}
