package com.timeweaver.controller;

import com.timeweaver.common.utils.ResponseResult;
import com.timeweaver.dto.request.LoginRequest;
import com.timeweaver.dto.request.RegisterRequest;
import com.timeweaver.dto.response.LoginResponse;
import com.timeweaver.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseResult<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseResult.success();
    }

    @PostMapping("/login")
    public ResponseResult<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseResult.success(response);
    }

    @PostMapping("/refresh")
    public ResponseResult<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseResult.failed(400, "token 不能为空");
        }
        LoginResponse response = authService.refresh(token);
        return ResponseResult.success(response);
    }
}
