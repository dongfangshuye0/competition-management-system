package com.tangqilin.competition.common.security;

import com.tangqilin.competition.common.exception.BusinessException;
import com.tangqilin.competition.entity.User;
import com.tangqilin.competition.storage.DatabaseStore;
import com.tangqilin.competition.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final DatabaseStore databaseStore;

    public AuthInterceptor(JwtUtil jwtUtil, DatabaseStore databaseStore) {
        this.jwtUtil = jwtUtil;
        this.databaseStore = databaseStore;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw BusinessException.unauthorized("请先登录");
        }

        String token = authorization.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw BusinessException.unauthorized("登录已过期，请重新登录");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = databaseStore.findUserById(userId)
                .orElseThrow(() -> BusinessException.unauthorized("用户不存在或已被删除"));
        AuthContext.setUser(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
