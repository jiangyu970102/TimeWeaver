package com.timeweaver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_time_record_template")
public class TimeRecordTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;
    private Long categoryId;
    private String description;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
