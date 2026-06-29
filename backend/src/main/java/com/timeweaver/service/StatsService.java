package com.timeweaver.service;

import com.timeweaver.entity.TimeRecord;
import com.timeweaver.mapper.CategoryMapper;
import com.timeweaver.mapper.TimeRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final TimeRecordMapper timeRecordMapper;
    private final CategoryMapper categoryMapper;

    public Map<String, Object> getDailyStats(Long userId, LocalDate date) {
        var records = timeRecordMapper.findByUserAndDate(userId, date);
        return aggregateStats(records);
    }

    public Map<String, Object> getWeeklyStats(Long userId, int year, int week) {
        LocalDate start = LocalDate.ofYearDay(year, 1)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .plusWeeks(week - 1);
        LocalDate end = start.plusDays(6);

        List<Map<String, Object>> dailyStats = new ArrayList<>();
        int totalMinutes = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            var records = timeRecordMapper.findByUserAndDate(userId, d);
            int dayTotal = records.stream()
                    .mapToInt(r -> r.getDurationMin() != null ? r.getDurationMin() : 0)
                    .sum();
            totalMinutes += dayTotal;
            dailyStats.add(Map.of("date", d.toString(), "totalMinutes", dayTotal, "records", records.size()));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalMinutes", totalMinutes);
        result.put("dailyBreakdown", dailyStats);
        result.put("startDate", start.toString());
        result.put("endDate", end.toString());
        return result;
    }

    public Map<String, Object> getMonthlyStats(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        int totalMinutes = 0;
        int totalRecords = 0;

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            var records = timeRecordMapper.findByUserAndDate(userId, d);
            if (!records.isEmpty()) {
                int dayTotal = records.stream().mapToInt(r -> r.getDurationMin() != null ? r.getDurationMin() : 0).sum();
                totalMinutes += dayTotal;
                totalRecords += records.size();
            }
        }

        return Map.of(
                "year", year,
                "month", month,
                "totalMinutes", totalMinutes,
                "totalRecords", totalRecords,
                "avgDailyMinutes", totalMinutes / (long) Math.max(1, end.getDayOfMonth())
        );
    }

    public List<Map<String, Object>> getHeatmapData(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Map<String, Object>> data = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            var records = timeRecordMapper.findByUserAndDate(userId, d);
            int total = records.stream()
                    .mapToInt(r -> r.getDurationMin() != null ? r.getDurationMin() : 0)
                    .sum();
            data.add(Map.of("date", d.toString(), "minutes", total));
        }
        return data;
    }

    public List<Map<String, Object>> getTrendData(Long userId, LocalDate start, LocalDate end) {
        List<Map<String, Object>> trend = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            var records = timeRecordMapper.findByUserAndDate(userId, d);
            int total = records.stream()
                    .mapToInt(r -> r.getDurationMin() != null ? r.getDurationMin() : 0)
                    .sum();
            trend.add(Map.of("date", d.toString(), "minutes", total));
        }
        return trend;
    }

    public Map<String, Object> getComparisonData(Long userId, LocalDate currentStart, LocalDate currentEnd) {
        long days = currentStart.until(currentEnd).getDays() + 1;
        LocalDate previousStart = currentStart.minusDays(days);
        LocalDate previousEnd = currentStart.minusDays(1);

        int currentTotal = sumMinutesInRange(userId, currentStart, currentEnd);
        int previousTotal = sumMinutesInRange(userId, previousStart, previousEnd);

        return Map.of(
                "currentTotal", currentTotal,
                "previousTotal", previousTotal,
                "change", currentTotal - previousTotal,
                "changePercent", previousTotal > 0
                        ? (int) Math.round((double) (currentTotal - previousTotal) / previousTotal * 100)
                        : 100
        );
    }

    public Map<String, Object> getCategoryStats(Long userId, LocalDate start, LocalDate end) {
        Map<Long, Integer> categoryMinutes = new HashMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            var records = timeRecordMapper.findByUserAndDate(userId, d);
            for (var r : records) {
                if (r.getCategoryId() != null && r.getDurationMin() != null) {
                    categoryMinutes.merge(r.getCategoryId(), r.getDurationMin(), Integer::sum);
                }
            }
        }

        var categories = categoryMapper.selectList(null);
        Map<Long, String> catNames = categories.stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getName()));

        List<Map<String, Object>> breakdown = new ArrayList<>();
        for (var entry : categoryMinutes.entrySet()) {
            breakdown.add(Map.of(
                    "categoryId", entry.getKey(),
                    "name", catNames.getOrDefault(entry.getKey(), "未知"),
                    "minutes", entry.getValue()
            ));
        }
        breakdown.sort((a, b) -> ((Integer) b.get("minutes")).compareTo((Integer) a.get("minutes")));

        return Map.of(
                "totalMinutes", breakdown.stream().mapToInt(m -> (int) m.get("minutes")).sum(),
                "breakdown", breakdown
        );
    }

    private int sumMinutesInRange(Long userId, LocalDate start, LocalDate end) {
        int total = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            var records = timeRecordMapper.findByUserAndDate(userId, d);
            total += records.stream()
                    .mapToInt(r -> r.getDurationMin() != null ? r.getDurationMin() : 0)
                    .sum();
        }
        return total;
    }

    private Map<String, Object> aggregateStats(List<TimeRecord> records) {
        int totalMinutes = records.stream()
                .mapToInt(r -> r.getDurationMin() != null ? r.getDurationMin() : 0)
                .sum();
        int recordCount = records.size();

        Map<Long, Integer> categoryBreakdown = new HashMap<>();
        for (var r : records) {
            if (r.getCategoryId() != null) {
                categoryBreakdown.merge(r.getCategoryId(), r.getDurationMin() != null ? r.getDurationMin() : 0, Integer::sum);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalMinutes", totalMinutes);
        result.put("recordCount", recordCount);
        result.put("categoryBreakdown", categoryBreakdown);
        return result;
    }
}
