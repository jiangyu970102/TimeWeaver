package com.timeweaver.controller;

import com.timeweaver.common.utils.ResponseResult;
import com.timeweaver.entity.GitCommitRecord;
import com.timeweaver.entity.GitRepoConfig;
import com.timeweaver.entity.TimeRecord;
import com.timeweaver.service.GitService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/git")
@RequiredArgsConstructor
public class GitController {

    private final GitService gitService;

    // ── 仓库配置 ──

    @GetMapping("/repos")
    public ResponseResult<List<GitRepoConfig>> listRepos(@AuthenticationPrincipal Long userId) {
        return ResponseResult.success(gitService.getRepos(userId));
    }

    @PostMapping("/repos")
    public ResponseResult<GitRepoConfig> addRepo(@AuthenticationPrincipal Long userId,
                                                  @RequestBody Map<String, String> body) {
        return ResponseResult.success(gitService.addRepo(userId, body.get("repoPath")));
    }

    @DeleteMapping("/repos/{id}")
    public ResponseResult<Void> removeRepo(@AuthenticationPrincipal Long userId,
                                            @PathVariable Long id) {
        gitService.removeRepo(userId, id);
        return ResponseResult.success();
    }

    @PutMapping("/repos/{id}/auto-import")
    public ResponseResult<Void> updateAutoImport(@AuthenticationPrincipal Long userId,
                                                  @PathVariable Long id,
                                                  @RequestBody Map<String, Boolean> body) {
        gitService.updateAutoImport(userId, id, body.get("autoImport"));
        return ResponseResult.success();
    }

    // ── 提交记录 ──

    @GetMapping("/commits")
    public ResponseResult<List<GitCommitRecord>> getCommits(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseResult.success(gitService.getCommits(userId, date));
    }

    @PostMapping("/scan")
    public ResponseResult<List<GitCommitRecord>> scanCommits(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long repoId) {
        if (repoId != null) {
            return ResponseResult.success(gitService.scanCommits(userId, repoId, date));
        }
        return ResponseResult.success(gitService.scanAllRepos(userId, date));
    }

    @PostMapping("/import")
    public ResponseResult<List<TimeRecord>> importCommits(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseResult.success(gitService.importAsRecords(userId, date));
    }
}
