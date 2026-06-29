package com.timeweaver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_time_record")
public class TimeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long categoryId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMin;
    private String description;
    private String tags;
    private String source;     // manual / pomodoro / auto
    private LocalDate recordDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
