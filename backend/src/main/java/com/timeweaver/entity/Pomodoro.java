package com.timeweaver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_pomodoro")
public class Pomodoro {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMin;
    private String taskName;
    private String taskDescription;
    private Integer completed;  // 0-中断/取消, 1-完成
    private Integer tomatoCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
