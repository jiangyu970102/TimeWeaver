package com.timeweaver.service;

import com.timeweaver.entity.AiSession;
import com.timeweaver.entity.GitCommitRecord;
import com.timeweaver.entity.TimeRecord;
import com.timeweaver.mapper.AiSessionMapper;
import com.timeweaver.mapper.CategoryMapper;
import com.timeweaver.mapper.GitCommitRecordMapper;
import com.timeweaver.mapper.GoalMapper;
import com.timeweaver.mapper.TimeRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailySummaryService {

    private final TimeRecordMapper timeRecordMapper;
    private final AiSessionMapper aiSessionMapper;
    private final GitCommitRecordMapper gitCommitRecordMapper;
    private final CategoryMapper categoryMapper;
    private final GoalMapper goalMapper;

    public Map<String, Object> getDailySummary(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        // ── 时间记录统计 ──
        List<TimeRecord> records = timeRecordMapper.findByUserAndDate(userId, date);
        int totalMinutes = records.stream()
                .mapToInt(r -> r.getDurationMin() != null ? r.getDurationMin() : 0)
                .sum();
        int recordCount = records.size();

        // 分类统计
        var categories = categoryMapper.selectList(null);
        Map<Long, String> catNames = categories.stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getName()));
        Map<Long, String> catColors = categories.stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getColor() != null ? c.getColor() : "#909399"));

        Map<Long, Integer> categoryMinutes = new LinkedHashMap<>();
        for (var r : records) {
            if (r.getCategoryId() != null && r.getDurationMin() != null) {
                categoryMinutes.merge(r.getCategoryId(), r.getDurationMin(), Integer::sum);
            }
        }
        List<Map<String, Object>> categoryBreakdown = new ArrayList<>();
        for (var entry : categoryMinutes.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("categoryId", entry.getKey());
            item.put("name", catNames.getOrDefault(entry.getKey(), "未知"));
            item.put("color", catColors.getOrDefault(entry.getKey(), "#909399"));
            item.put("minutes", entry.getValue());
            categoryBreakdown.add(item);
        }
        categoryBreakdown.sort((a, b) -> ((Integer) b.get("minutes")).compareTo((Integer) a.get("minutes")));

        // ── AI 会话统计 ──
        List<AiSession> sessions = aiSessionMapper.findByUserAndTimeRange(userId, start, end);
        int aiSessionCount = sessions.size();
        int aiTotalSeconds = sessions.stream()
                .mapToInt(s -> s.getDurationSec() != null ? s.getDurationSec() : 0)
                .sum();
        int aiTotalMinutes = aiTotalSeconds / 60;

        // ── Git 提交统计 ──
        List<GitCommitRecord> commits = gitCommitRecordMapper.findByUserAndDateRange(userId, start, end);
        int gitCommitCount = commits.size();

        // ── 目标进度 ──
        var goals = goalMapper.findActiveGoals(userId);
        int goalCount = goals.size();
        int completedGoals = (int) goals.stream().filter(g -> {
            Integer current = g.getCurrentValue();
            Integer target = g.getTargetValue();
            return current != null && target != null && current >= target;
        }).count();
        int goalProgress = goalCount > 0 ? (int) Math.round((double) completedGoals / goalCount * 100) : 0;

        // ── 汇总 ──
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("date", date.toString());
        summary.put("totalMinutes", totalMinutes);
        summary.put("recordCount", recordCount);
        summary.put("categoryBreakdown", categoryBreakdown);
        summary.put("aiSessionCount", aiSessionCount);
        summary.put("aiTotalMinutes", aiTotalMinutes);
        summary.put("gitCommitCount", gitCommitCount);
        summary.put("goalCount", goalCount);
        summary.put("completedGoals", completedGoals);
        summary.put("goalProgress", goalProgress);

        return summary;
    }
}
