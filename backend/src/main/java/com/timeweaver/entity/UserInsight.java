package com.timeweaver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user_insight")
public class UserInsight {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String type;        // weekly / monthly / achievement / milestone
    private String title;
    private String content;
    private Integer isRead;
    private LocalDateTime generatedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
