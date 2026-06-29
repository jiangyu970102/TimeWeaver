package com.timeweaver.controller;

import com.timeweaver.common.utils.ResponseResult;
import com.timeweaver.dto.response.UserProfileResponse;
import com.timeweaver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseResult<UserProfileResponse> getProfile(@AuthenticationPrincipal Long userId) {
        UserProfileResponse profile = userService.getProfile(userId);
        return ResponseResult.success(profile);
    }

    @PutMapping("/profile")
    public ResponseResult<Void> updateProfile(@AuthenticationPrincipal Long userId,
                                               @RequestBody UserProfileResponse request) {
        userService.updateProfile(userId, request);
        return ResponseResult.success();
    }

    @PutMapping("/preferences")
    public ResponseResult<Void> updatePreferences(@AuthenticationPrincipal Long userId,
                                                   @RequestBody UserProfileResponse request) {
        userService.updatePreferences(userId, request);
        return ResponseResult.success();
    }
}
