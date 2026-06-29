package com.timeweaver.service;

import com.timeweaver.common.constant.ApiCode;
import com.timeweaver.common.exception.BusinessException;
import com.timeweaver.dto.response.UserProfileResponse;
import com.timeweaver.entity.User;
import com.timeweaver.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public UserProfileResponse getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "用户不存在");
        }
        return toProfileResponse(user);
    }

    public void updateProfile(Long userId, UserProfileResponse request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "用户不存在");
        }
        user.setNickname(request.getNickname());
        user.setAvatar(request.getAvatar());
        user.setEmail(request.getEmail());
        userMapper.updateById(user);
    }

    public void updatePreferences(Long userId, UserProfileResponse request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "用户不存在");
        }
        user.setDailyGoalMin(request.getDailyGoalMin());
        user.setWeeklyGoalMin(request.getWeeklyGoalMin());
        user.setPomodoroDuration(request.getPomodoroDuration());
        user.setBreakDuration(request.getBreakDuration());
        userMapper.updateById(user);
    }

    private UserProfileResponse toProfileResponse(User user) {
        UserProfileResponse resp = new UserProfileResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setEmail(user.getEmail());
        resp.setRole(user.getRole());
        resp.setDailyGoalMin(user.getDailyGoalMin());
        resp.setWeeklyGoalMin(user.getWeeklyGoalMin());
        resp.setPomodoroDuration(user.getPomodoroDuration());
        resp.setBreakDuration(user.getBreakDuration());
        return resp;
    }
}
