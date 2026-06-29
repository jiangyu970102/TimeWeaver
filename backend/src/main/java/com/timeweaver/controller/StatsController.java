package com.timeweaver.controller;

import com.timeweaver.common.utils.ResponseResult;
import com.timeweaver.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/daily")
    public ResponseResult<Map<String, Object>> daily(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseResult.success(statsService.getDailyStats(userId, date));
    }

    @GetMapping("/weekly")
    public ResponseResult<Map<String, Object>> weekly(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer week) {
        if (year == null) year = LocalDate.now().getYear();
        if (week == null) week = LocalDate.now().get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return ResponseResult.success(statsService.getWeeklyStats(userId, year, week));
    }

    @GetMapping("/monthly")
    public ResponseResult<Map<String, Object>> monthly(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        return ResponseResult.success(statsService.getMonthlyStats(userId, year, month));
    }

    @GetMapping("/heatmap")
    public ResponseResult<List<Map<String, Object>>> heatmap(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        return ResponseResult.success(statsService.getHeatmapData(userId, year, month));
    }

    @GetMapping("/trend")
    public ResponseResult<List<Map<String, Object>>> trend(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseResult.success(statsService.getTrendData(userId, start, end));
    }

    @GetMapping("/comparison")
    public ResponseResult<Map<String, Object>> comparison(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentEnd) {
        return ResponseResult.success(statsService.getComparisonData(userId, currentStart, currentEnd));
    }

    @GetMapping("/categories")
    public ResponseResult<Map<String, Object>> categoryStats(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseResult.success(statsService.getCategoryStats(userId, start, end));
    }
}
