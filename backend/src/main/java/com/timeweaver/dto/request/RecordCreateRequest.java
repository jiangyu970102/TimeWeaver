package com.timeweaver.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecordCreateRequest {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private String description;
    private String tags;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMin;
    private String source;     // manual / pomodoro
}
