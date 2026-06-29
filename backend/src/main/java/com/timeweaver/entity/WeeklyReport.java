package com.timeweaver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_weekly_report")
public class WeeklyReport {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Integer year;
    private Integer week;
    private LocalDate startDate;
    private LocalDate endDate;

    private String summary;
    private String statsSnapshot;     // JSON
    private String suggestions;       // JSON
    private String strengths;         // JSON
    private String weaknesses;        // JSON
    private String reportStatus;    // pending / completed / failed
    private Integer tokenUsage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
