package com.timeweaver.controller;

import com.timeweaver.common.utils.ResponseResult;
import com.timeweaver.entity.TimeRecord;
import com.timeweaver.service.DailySummaryService;
import com.timeweaver.service.StatsService;
import com.timeweaver.service.TimeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final TimeRecordService timeRecordService;
    private final StatsService statsService;
    private final DailySummaryService dailySummaryService;

    @GetMapping("/overview")
    public ResponseResult<Map<String, Object>> overview(@AuthenticationPrincipal Long userId) {
        LocalDate today = LocalDate.now();

        var todayRecords = timeRecordService.getRecordsByDate(userId, today);
        var dailyStats = statsService.getDailyStats(userId, today);
        var ongoingRecord = timeRecordService.getCurrentRecord(userId);

        int totalMinutes = (int) dailyStats.getOrDefault("totalMinutes", 0);
        int recordCount = (int) dailyStats.getOrDefault("recordCount", 0);

        Map<String, Object> overview = new HashMap<>();
        overview.put("todayTotalMinutes", totalMinutes);
        overview.put("todayRecordCount", recordCount);
        overview.put("ongoingRecord", ongoingRecord);
        overview.put("todayRecords", todayRecords);
        overview.put("date", today.toString());

        return ResponseResult.success(overview);
    }

    @GetMapping("/summary")
    public ResponseResult<Map<String, Object>> dailySummary(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseResult.success(dailySummaryService.getDailySummary(userId, date));
    }
}
