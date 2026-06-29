package com.timeweaver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String email;

    private Integer role;       // 0-普通用户, 1-管理员
    private Integer status;     // 0-禁用, 1-正常

    private Integer dailyGoalMin;
    private Integer weeklyGoalMin;
    private Integer pomodoroDuration;
    private Integer breakDuration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
