package com.timeweaver.service;

import com.timeweaver.entity.GitCommitRecord;
import com.timeweaver.entity.GitRepoConfig;
import com.timeweaver.entity.TimeRecord;
import com.timeweaver.mapper.GitCommitRecordMapper;
import com.timeweaver.mapper.GitRepoConfigMapper;
import com.timeweaver.mapper.CategoryMapper;
import com.timeweaver.mapper.TimeRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitService {

    private final GitRepoConfigMapper repoConfigMapper;
    private final GitCommitRecordMapper commitRecordMapper;
    private final TimeRecordMapper timeRecordMapper;
    private final CategoryMapper categoryMapper;

    // ── 仓库配置 ──

    public List<GitRepoConfig> getRepos(Long userId) {
        return repoConfigMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<GitRepoConfig>lambdaQuery()
                        .eq(GitRepoConfig::getUserId, userId)
        );
    }

    public GitRepoConfig addRepo(Long userId, String repoPath) {
        GitRepoConfig config = new GitRepoConfig();
        config.setUserId(userId);
        config.setRepoPath(repoPath);
        config.setRepoName(new java.io.File(repoPath).getName());
        config.setAutoImport(false);
        repoConfigMapper.insert(config);
        return config;
    }

    public void removeRepo(Long userId, Long id) {
        repoConfigMapper.delete(com.baomidou.mybatisplus.core.toolkit.Wrappers
                .<GitRepoConfig>lambdaQuery()
                .eq(GitRepoConfig::getId, id)
                .eq(GitRepoConfig::getUserId, userId));
    }

    public void updateAutoImport(Long userId, Long id, boolean autoImport) {
        GitRepoConfig config = repoConfigMapper.selectById(id);
        if (config != null && config.getUserId().equals(userId)) {
            config.setAutoImport(autoImport);
            repoConfigMapper.updateById(config);
        }
    }

    // ── 扫描提交 ──

    public List<GitCommitRecord> scanCommits(Long userId, Long repoId, LocalDate date) {
        GitRepoConfig repo = repoConfigMapper.selectById(repoId);
        if (repo == null || !repo.getUserId().equals(userId)) {
            throw new RuntimeException("仓库不存在或无权限");
        }
        return scanCommits(userId, repo, date);
    }

    public List<GitCommitRecord> scanCommits(Long userId, GitRepoConfig repo, LocalDate date) {
        List<GitCommitRecord> results = new ArrayList<>();
        String repoPath = repo.getRepoPath();
        String since = date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "T00:00:00";
        String until = date.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE) + "T00:00:00";

        try {
            // 修复仓库路径中的空格
            String safePath = repoPath.replace(" ", "\\ ");
            ProcessBuilder pb = new ProcessBuilder(
                    "bash", "-c",
                    "cd " + safePath + " && git log --after=\"" + since + "\" --before=\"" + until
                            + "\" --format=\"%H|%an|%s|%aI\" --reverse 2>/dev/null"
            );
            Process process = pb.start();
            String line;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split("\\|", 4);
                    if (parts.length < 4) continue;

                    String hash = parts[0];
                    String author = parts[1];
                    String message = parts[2];
                    LocalDateTime committedAt = LocalDateTime.parse(parts[3].substring(0, 19),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                    // 去重
                    if (commitRecordMapper.countByHash(userId, hash) > 0) continue;

                    GitCommitRecord record = new GitCommitRecord();
                    record.setUserId(userId);
                    record.setRepoPath(repo.getRepoName());
                    record.setCommitHash(hash);
                    record.setAuthorName(author);
                    record.setMessage(message);
                    record.setCommittedAt(committedAt);
                    commitRecordMapper.insert(record);
                    results.add(record);
                }
            }
            process.waitFor();
        } catch (Exception e) {
            log.warn("扫描仓库失败: {} - {}", repoPath, e.getMessage());
        }

        return results;
    }

    public List<GitCommitRecord> scanAllRepos(Long userId, LocalDate date) {
        List<GitRepoConfig> repos = getRepos(userId);
        List<GitCommitRecord> all = new ArrayList<>();
        for (GitRepoConfig repo : repos) {
            all.addAll(scanCommits(userId, repo, date));
        }
        return all;
    }

    // ── 获取提交 ──

    public List<GitCommitRecord> getCommits(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return commitRecordMapper.findByUserAndDateRange(userId, start, end);
    }

    // ── 导入为时间记录 ──

    @Transactional
    public List<TimeRecord> importAsRecords(Long userId, LocalDate date) {
        List<GitCommitRecord> commits = getCommits(userId, date);
        if (commits.isEmpty()) return Collections.emptyList();

        // 按仓库+时间排序，分组为"编码时段"
        Map<String, List<GitCommitRecord>> byRepo = commits.stream()
                .collect(Collectors.groupingBy(GitCommitRecord::getRepoPath));

        // 找到开发·编码分类
        Long categoryId = categoryMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers
                        .<com.timeweaver.entity.Category>lambdaQuery()
                        .eq(com.timeweaver.entity.Category::getName, "开发·编码")
                        .isNull(com.timeweaver.entity.Category::getUserId)
        ).stream().findFirst().map(com.timeweaver.entity.Category::getId).orElse(null);

        if (categoryId == null) return Collections.emptyList();

        List<TimeRecord> imported = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        for (Map.Entry<String, List<GitCommitRecord>> entry : byRepo.entrySet()) {
            List<GitCommitRecord> repoCommits = entry.getValue();
            repoCommits.sort(Comparator.comparing(GitCommitRecord::getCommittedAt));

            // 将间隔 < 30 分钟的连续提交分组为一个"时段"
            List<List<GitCommitRecord>> sessions = new ArrayList<>();
            List<GitCommitRecord> current = new ArrayList<>();
            current.add(repoCommits.get(0));

            for (int i = 1; i < repoCommits.size(); i++) {
                long gap = java.time.Duration.between(
                        repoCommits.get(i - 1).getCommittedAt(),
                        repoCommits.get(i).getCommittedAt()
                ).toMinutes();
                if (gap > 30) {
                    sessions.add(current);
                    current = new ArrayList<>();
                }
                current.add(repoCommits.get(i));
            }
            sessions.add(current);

            // 每个时段生成一条 TimeRecord
            for (List<GitCommitRecord> session : sessions) {
                if (session.isEmpty()) continue;
                GitCommitRecord first = session.get(0);
                GitCommitRecord last = session.get(session.size() - 1);
                int durationMin = (int) java.time.Duration.between(first.getCommittedAt(), last.getCommittedAt()).toMinutes();
                if (durationMin < 1) durationMin = 1;

                // 合并 commit message
                String description = session.stream()
                        .map(GitCommitRecord::getMessage)
                        .filter(msg -> msg.length() > 0)
                        .collect(Collectors.joining("; "));
                if (description.length() > 200) description = description.substring(0, 197) + "...";

                String timeRange = first.getCommittedAt().format(fmt) + "-" + last.getCommittedAt().format(fmt);

                TimeRecord record = new TimeRecord();
                record.setUserId(userId);
                record.setCategoryId(categoryId);
                record.setStartTime(first.getCommittedAt());
                record.setEndTime(last.getCommittedAt());
                record.setDurationMin(Math.max(durationMin, 1));
                record.setDescription("[" + entry.getKey() + "] " + description);
                record.setTags("git," + entry.getKey());
                record.setSource("git");
                record.setRecordDate(date);
                timeRecordMapper.insert(record);

                // 关联 commit 到 time_record
                for (GitCommitRecord gc : session) {
                    gc.setRecordId(record.getId());
                    commitRecordMapper.updateById(gc);
                }
                imported.add(record);
            }
        }

        return imported;
    }
}
