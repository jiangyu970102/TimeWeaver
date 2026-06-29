package com.timeweaver.service;

import com.timeweaver.common.exception.BusinessException;
import com.timeweaver.config.JwtConfig;
import com.timeweaver.config.JwtUtil;
import com.timeweaver.dto.request.LoginRequest;
import com.timeweaver.dto.request.RegisterRequest;
import com.timeweaver.dto.response.LoginResponse;
import com.timeweaver.entity.User;
import com.timeweaver.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm-for-testing");
        jwtConfig.setExpiration(3600000L);
        jwtUtil = new JwtUtil(jwtConfig);
        authService = new AuthService(userMapper, passwordEncoder, jwtUtil);
    }

    @Test
    void register_ShouldThrowWhenUsernameExists() {
        when(userMapper.selectByUsername("existing")).thenReturn(new User());

        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setPassword("pass123");

        assertThrows(BusinessException.class, () -> authService.register(request));
    }

    @Test
    void register_ShouldSucceedWhenUsernameAvailable() {
        when(userMapper.selectByUsername("newuser")).thenReturn(null);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("pass123");
        request.setEmail("new@test.com");

        assertDoesNotThrow(() -> authService.register(request));
    }

    @Test
    void login_ShouldThrowWhenUserNotFound() {
        when(userMapper.selectByUsername("nobody")).thenReturn(null);

        LoginRequest request = new LoginRequest();
        request.setUsername("nobody");
        request.setPassword("pass123");

        assertThrows(BusinessException.class, () -> authService.login(request));
    }

    @Test
    void login_ShouldThrowWhenPasswordWrong() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword(passwordEncoder.encode("correct"));
        user.setStatus(1);

        when(userMapper.selectByUsername("test")).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("test");
        request.setPassword("wrong");

        assertThrows(BusinessException.class, () -> authService.login(request));
    }

    @Test
    void login_ShouldReturnTokenWhenCredentialsValid() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword(passwordEncoder.encode("correct"));
        user.setNickname("Tester");
        user.setEmail("test@test.com");
        user.setStatus(1);
        user.setDailyGoalMin(240);
        user.setWeeklyGoalMin(1200);

        when(userMapper.selectByUsername("test")).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("test");
        request.setPassword("correct");

        LoginResponse response = authService.login(request);

        assertNotNull(response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getUser());
        assertEquals("Tester", response.getUser().getNickname());
    }
}
