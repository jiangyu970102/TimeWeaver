package com.timeweaver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_ai_session")
public class AiSession {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long recordId;
    private String toolName;      // claude-code / cursor / codex / chatgpt
    private String modelName;     // deepseek-v4-flash / gpt-4o / claude-sonnet / kimi-k2
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationSec;
    private Integer tokenCount;
    private BigDecimal costEstimate;
    private String sessionMeta;   // JSON
    private String source;        // manual / auto / detect

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
