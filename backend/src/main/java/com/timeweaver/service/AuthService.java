package com.timeweaver.service;

import com.timeweaver.common.constant.ApiCode;
import com.timeweaver.common.exception.BusinessException;
import com.timeweaver.config.JwtUtil;
import com.timeweaver.dto.request.LoginRequest;
import com.timeweaver.dto.request.RegisterRequest;
import com.timeweaver.dto.response.LoginResponse;
import com.timeweaver.entity.User;
import com.timeweaver.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest request) {
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException(ApiCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(0);
        user.setStatus(1);
        user.setDailyGoalMin(240);
        user.setWeeklyGoalMin(1200);
        user.setPomodoroDuration(25);
        user.setBreakDuration(5);

        userMapper.insert(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ApiCode.USERNAME_OR_PASSWORD_ERROR);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ApiCode.USER_DISABLED);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ApiCode.USERNAME_OR_PASSWORD_ERROR);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        var userInfo = new LoginResponse.UserInfo(
                user.getId(), user.getUsername(), user.getNickname(),
                user.getEmail(), user.getDailyGoalMin(), user.getWeeklyGoalMin()
        );
        return new LoginResponse(token, "Bearer", userInfo);
    }

    public LoginResponse refresh(String oldToken) {
        Claims claims = jwtUtil.parseToken(oldToken);
        Long userId = claims.get("user_id", Long.class);
        String username = claims.get("username", String.class);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED, "用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ApiCode.USER_DISABLED);
        }

        String newToken = jwtUtil.generateToken(user.getId(), user.getUsername());
        var userInfo = new LoginResponse.UserInfo(
                user.getId(), user.getUsername(), user.getNickname(),
                user.getEmail(), user.getDailyGoalMin(), user.getWeeklyGoalMin()
        );
        return new LoginResponse(newToken, "Bearer", userInfo);
    }
}
