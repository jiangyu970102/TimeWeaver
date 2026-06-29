package com.timeweaver.controller;

import com.timeweaver.common.utils.ResponseResult;
import com.timeweaver.entity.UserInsight;
import com.timeweaver.entity.WeeklyReport;
import com.timeweaver.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/weekly")
    public ResponseResult<WeeklyReport> weekly(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer week) {
        if (year == null || week == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            week = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        }
        return ResponseResult.success(reportService.getWeeklyReport(userId, year, week));
    }

    @PostMapping("/weekly/generate")
    public ResponseResult<WeeklyReport> generate(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer week) {
        if (year == null || week == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            week = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        }
        return ResponseResult.success(reportService.generateReport(userId, year, week));
    }

    @GetMapping("/list")
    public ResponseResult<List<WeeklyReport>> list(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseResult.success(reportService.getReportList(userId, limit));
    }

    @GetMapping("/insights")
    public ResponseResult<List<UserInsight>> insights(@AuthenticationPrincipal Long userId) {
        return ResponseResult.success(reportService.getInsights(userId));
    }

    @PutMapping("/insights/{id}/read")
    public ResponseResult<Void> markRead(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        reportService.markInsightRead(userId, id);
        return ResponseResult.success();
    }
}
