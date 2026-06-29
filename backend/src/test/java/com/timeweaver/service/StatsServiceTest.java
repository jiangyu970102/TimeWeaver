package com.timeweaver.service;

import com.timeweaver.entity.TimeRecord;
import com.timeweaver.mapper.CategoryMapper;
import com.timeweaver.mapper.TimeRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private TimeRecordMapper timeRecordMapper;

    @Mock
    private CategoryMapper categoryMapper;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(timeRecordMapper, categoryMapper);
    }

    @Test
    void getTrendData_ShouldReturnDailyMinutes() {
        Long userId = 1L;
        LocalDate start = LocalDate.of(2026, 6, 22);
        LocalDate end = LocalDate.of(2026, 6, 24);

        when(timeRecordMapper.findByUserAndDate(eq(userId), eq(LocalDate.of(2026, 6, 22))))
                .thenReturn(List.of(createRecord(60), createRecord(30)));
        when(timeRecordMapper.findByUserAndDate(eq(userId), eq(LocalDate.of(2026, 6, 23))))
                .thenReturn(List.of(createRecord(120)));
        when(timeRecordMapper.findByUserAndDate(eq(userId), eq(LocalDate.of(2026, 6, 24))))
                .thenReturn(List.of());

        List<Map<String, Object>> trend = statsService.getTrendData(userId, start, end);

        assertEquals(3, trend.size());
        assertEquals(90, trend.get(0).get("minutes"));
        assertEquals("2026-06-22", trend.get(0).get("date"));
        assertEquals(120, trend.get(1).get("minutes"));
        assertEquals(0, trend.get(2).get("minutes"));
    }

    @Test
    void getComparisonData_ShouldCalcCorrectly() {
        Long userId = 1L;
        LocalDate currentStart = LocalDate.of(2026, 6, 22);
        LocalDate currentEnd = LocalDate.of(2026, 6, 28);

        // Stub all dates that could be queried (current + previous period)
        for (int i = 15; i <= 28; i++) {
            lenient().when(timeRecordMapper.findByUserAndDate(eq(userId), eq(LocalDate.of(2026, 6, i))))
                    .thenReturn(List.of());
        }
        // Current period: one record on Jun 24 (60min)
        when(timeRecordMapper.findByUserAndDate(eq(userId), eq(LocalDate.of(2026, 6, 24))))
                .thenReturn(List.of(createRecord(60)));
        // Previous period: one record on Jun 20 (30min)
        when(timeRecordMapper.findByUserAndDate(eq(userId), eq(LocalDate.of(2026, 6, 20))))
                .thenReturn(List.of(createRecord(30)));

        Map<String, Object> comparison = statsService.getComparisonData(userId, currentStart, currentEnd);

        assertEquals(60, comparison.get("currentTotal"));
        assertEquals(30, comparison.get("previousTotal"));
        assertEquals(30, comparison.get("change"));
        assertEquals(100, comparison.get("changePercent"));
    }

    @Test
    void getComparisonData_PreviousZero_ShouldReturn100Percent() {
        Long userId = 1L;
        LocalDate currentStart = LocalDate.of(2026, 6, 22);
        LocalDate currentEnd = LocalDate.of(2026, 6, 28);

        // No previous data, some current data
        lenient().when(timeRecordMapper.findByUserAndDate(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of());
        when(timeRecordMapper.findByUserAndDate(eq(userId), eq(LocalDate.of(2026, 6, 24))))
                .thenReturn(List.of(createRecord(60)));

        Map<String, Object> comparison = statsService.getComparisonData(userId, currentStart, currentEnd);

        assertEquals(100, comparison.get("changePercent"));
    }

    private TimeRecord createRecord(int durationMin) {
        TimeRecord r = new TimeRecord();
        r.setDurationMin(durationMin);
        r.setCategoryId(1L);
        return r;
    }
}
