package com.timeweaver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;
    private String icon;
    private String color;
    private String type;       // work / study / entertain / rest / social / other
    private Integer sortOrder;
    private Integer isSystem;  // 0-自定义, 1-系统预设

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
