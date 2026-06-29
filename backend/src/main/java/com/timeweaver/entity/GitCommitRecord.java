package com.timeweaver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_git_commit_record")
public class GitCommitRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String repoPath;
    private String commitHash;
    private String authorName;
    private String message;
    private LocalDateTime committedAt;
    private Long recordId;        // 关联的时间记录ID（导入后才非空）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
