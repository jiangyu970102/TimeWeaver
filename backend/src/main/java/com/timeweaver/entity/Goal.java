package com.timeweaver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_goal")
public class Goal {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;
    private String goalType;      // daily / weekly / monthly
    private Long categoryId;
    private Integer targetValue;
    private Integer currentValue;
    private String unit;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;       // 0-进行中, 1-已完成, 2-已放弃

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
