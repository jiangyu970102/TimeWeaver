package com.timeweaver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timeweaver.common.constant.ApiCode;
import com.timeweaver.common.exception.BusinessException;
import com.timeweaver.entity.UserInsight;
import com.timeweaver.entity.WeeklyReport;
import com.timeweaver.mapper.UserInsightMapper;
import com.timeweaver.mapper.WeeklyReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final WeeklyReportMapper weeklyReportMapper;
    private final UserInsightMapper userInsightMapper;
    private final StatsService statsService;
    private final ObjectMapper objectMapper;
    private final OpenAiChatModel chatModel;
    private final com.timeweaver.handler.NotificationHandler notificationHandler;

    public WeeklyReport getWeeklyReport(Long userId, int year, int week) {
        WeeklyReport report = weeklyReportMapper.findByUserAndWeek(userId, year, week);
        if (report != null) return report;
        return generateReport(userId, year, week);
    }

    @Transactional
    public WeeklyReport generateReport(Long userId, int year, int week) {
        // Calculate date range
        LocalDate start = LocalDate.ofYearDay(year, 1)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .plusWeeks(week - 1);
        LocalDate end = start.plusDays(6);

        // Gather stats
        var trendData = statsService.getTrendData(userId, start, end);
        var categoryStats = statsService.getCategoryStats(userId, start, end);
        var comparison = statsService.getComparisonData(userId, start, end);

        int totalMinutes = trendData.stream().mapToInt(m -> (int) m.get("minutes")).sum();
        int activeDays = (int) trendData.stream().filter(m -> (int) m.get("minutes") > 0).count();
        int avgDaily = activeDays > 0 ? totalMinutes / activeDays : 0;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> breakdown = (List<Map<String, Object>>) categoryStats.get("breakdown");
        String topCategory = breakdown.isEmpty() ? "无" : (String) breakdown.get(0).get("name");

        // Try AI generation, fallback to template
        String summary;
        String suggestions;
        String strengths;
        String weaknesses;

        try {
            ReportContent aiContent = generateWithAI(userId, year, week, totalMinutes, activeDays, avgDaily, topCategory, breakdown, comparison);
            summary = aiContent.summary;
            suggestions = objectMapper.writeValueAsString(aiContent.suggestions);
            strengths = objectMapper.writeValueAsString(aiContent.strengths);
            weaknesses = objectMapper.writeValueAsString(aiContent.weaknesses);
        } catch (Exception e) {
            log.warn("AI generation failed, using template: {}", e.getMessage());
            summary = generateTemplateSummary(totalMinutes, activeDays, avgDaily, topCategory);
            suggestions = "[\"尝试使用番茄钟提高专注力\",\"为每天设定固定工作时间\"]";
            strengths = "[\"坚持记录时间\",\"持续进步\"]";
            weaknesses = generateTemplateWeaknesses(breakdown);
        }

        // Save report
        WeeklyReport report = new WeeklyReport();
        report.setUserId(userId);
        report.setYear(year);
        report.setWeek(week);
        report.setStartDate(start);
        report.setEndDate(end);
        report.setSummary(summary);
        report.setStatsSnapshot(statsToJson(trendData, categoryStats, comparison));
        report.setSuggestions(suggestions);
        report.setStrengths(strengths);
        report.setWeaknesses(weaknesses);
        report.setReportStatus("completed");
        weeklyReportMapper.insert(report);

        // Save insight
        UserInsight insight = new UserInsight();
        insight.setUserId(userId);
        insight.setType("weekly");
        insight.setTitle(String.format("第%d周周报已生成", week));
        insight.setContent(summary);
        insight.setIsRead(0);
        insight.setGeneratedAt(java.time.LocalDateTime.now());
        userInsightMapper.insert(insight);

        // Push WebSocket notification
        notificationHandler.sendNotification(userId, "周报已生成", summary);

        return report;
    }

    private ReportContent generateWithAI(Long userId, int year, int week,
                                          int totalMinutes, int activeDays, int avgDaily,
                                          String topCategory,
                                          List<Map<String, Object>> breakdown,
                                          Map<String, Object> comparison) {
        String breakdownStr = breakdown.stream()
                .map(b -> String.format("  - %s: %d分钟", b.get("name"), b.get("minutes")))
                .collect(Collectors.joining("\n"));

        int changePercent = (int) comparison.getOrDefault("changePercent", 0);

        String prompt = """
                你是一个时间管理分析师。请分析以下用户的时间数据，生成第%d周的周报。

                数据概览：
                - 总时长：%d分钟（%.1f小时）
                - 活跃天数：%d天
                - 日均时长：%d分钟
                - 最常活动：%s
                - 环比变化：%d%%

                分类详情：
                %s

                请生成以下内容（用中文）：
                1. summary: 一句总结（60字以内）
                2. suggestions: 2-3条改进建议（每条20字以内）
                3. strengths: 1-2个优点（每条15字以内）
                4. weaknesses: 1-2个待改进（每条15字以内）

                以JSON格式返回，格式：
                {"summary":"...","suggestions":["..."],"strengths":["..."],"weaknesses":["..."]}
                """.formatted(week, totalMinutes, totalMinutes / 60.0, activeDays, avgDaily, topCategory, changePercent, breakdownStr);

        ChatResponse response = chatModel.call(new Prompt(new UserMessage(prompt)));
        String content = response.getResult().getOutput().getText();

        // Parse JSON response
        try {
            return objectMapper.readValue(content, ReportContent.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse AI response as JSON, using fallback: {}", e.getMessage());
            // Try to extract summary from plain text
            return new ReportContent(
                    content.length() > 100 ? content.substring(0, 100) : content,
                    List.of("尝试使用番茄钟提高专注力", "为每天设定固定工作时间"),
                    List.of("坚持记录时间", "持续进步"),
                    generateTemplateWeaknessesList(breakdown)
            );
        }
    }

    private String generateTemplateSummary(int total, int activeDays, int avgDaily, String topCategory) {
        if (total == 0) return "本周暂无时间记录，开始记录你的时间吧！";
        return String.format("本周共记录%d小时%d分钟，活跃%d天，日均%d分钟。主要时间投入在「%s」。",
                total / 60, total % 60, activeDays, avgDaily, topCategory);
    }

    private String generateTemplateWeaknesses(List<Map<String, Object>> breakdown) {
        List<String> items = new ArrayList<>();
        if (breakdown.stream().noneMatch(b -> "开发·编码".equals(b.get("name")) || "学习·阅读".equals(b.get("name")))) {
            items.add("缺少深度工作时间");
        }
        if (breakdown.stream().anyMatch(b -> {
            String n = (String) b.get("name");
            return n != null && n.contains("娱乐") && (int) b.get("minutes") > 120;
        })) {
            items.add("娱乐时间偏多");
        }
        if (items.isEmpty()) items.add("时间分配较为均衡");
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            return "[\"时间分配可进一步优化\"]";
        }
    }

    private List<String> generateTemplateWeaknessesList(List<Map<String, Object>> breakdown) {
        List<String> items = new ArrayList<>();
        if (breakdown.stream().noneMatch(b -> {
            String n = (String) b.get("name");
            return n != null && (n.contains("编码") || n.contains("阅读") || n.contains("学习"));
        })) {
            items.add("缺少深度工作时间");
        }
        if (breakdown.stream().anyMatch(b -> {
            String n = (String) b.get("name");
            return n != null && n.contains("娱乐") && (int) b.get("minutes") > 120;
        })) {
            items.add("娱乐时间偏多");
        }
        if (items.isEmpty()) items.add("时间分配较为均衡");
        return items;
    }

    private String statsToJson(List<Map<String, Object>> trend,
                                Map<String, Object> categoryStats,
                                Map<String, Object> comparison) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("trend", trend);
        snapshot.put("categoryStats", categoryStats);
        snapshot.put("comparison", comparison);
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public List<WeeklyReport> getReportList(Long userId, int limit) {
        return weeklyReportMapper.findRecent(userId, Math.min(limit, 50));
    }

    public List<UserInsight> getInsights(Long userId) {
        return userInsightMapper.findLatest(userId, 20);
    }

    public void markInsightRead(Long userId, Long insightId) {
        UserInsight insight = userInsightMapper.selectById(insightId);
        if (insight != null && insight.getUserId().equals(userId)) {
            insight.setIsRead(1);
            userInsightMapper.updateById(insight);
        }
    }

    // Inner class for AI response parsing
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReportContent {
        private String summary;
        private List<String> suggestions;
        private List<String> strengths;
        private List<String> weaknesses;
    }
}
