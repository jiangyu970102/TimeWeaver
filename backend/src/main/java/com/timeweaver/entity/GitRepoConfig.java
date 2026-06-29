package com.timeweaver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_git_repo_config")
public class GitRepoConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String repoPath;
    private String repoName;
    private Boolean autoImport;   // 是否自动导入为时间记录

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
