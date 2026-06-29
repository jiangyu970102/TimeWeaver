package com.timeweaver.dto.response;

import lombok.Data;

@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private Integer role;
    private Integer dailyGoalMin;
    private Integer weeklyGoalMin;
    private Integer pomodoroDuration;
    private Integer breakDuration;
}
