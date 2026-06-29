package com.timeweaver.service;

import com.timeweaver.entity.Goal;
import com.timeweaver.entity.TimeRecord;
import com.timeweaver.mapper.GoalMapper;
import com.timeweaver.mapper.TimeRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private TimeRecordMapper timeRecordMapper;

    private GoalService goalService;

    @BeforeEach
    void setUp() {
        goalService = new GoalService(goalMapper, timeRecordMapper);
    }

    @Test
    void create_ShouldSetDefaults() {
        Long userId = 1L;
        Goal input = new Goal();
        input.setName("测试目标");
        input.setGoalType("monthly");
        input.setCategoryId(1L);
        input.setTargetValue(3000);

        when(goalMapper.insert(any(Goal.class))).thenAnswer(invocation -> {
            Goal g = invocation.getArgument(0);
            g.setId(1L);
            return 1;
        });
        // Prevent recalculateProgress from throwing due to unstubbed calls
        lenient().when(timeRecordMapper.findByUserAndDate(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of());

        Goal result = goalService.create(userId, input);

        assertEquals(userId, result.getUserId());
        assertEquals("测试目标", result.getName());
        assertEquals("monthly", result.getGoalType());
        assertEquals(Integer.valueOf(0), result.getCurrentValue());
        assertEquals(Integer.valueOf(0), result.getStatus());
        assertNotNull(result.getStartDate());
    }

    @Test
    void create_ShouldCalculateInitialProgress() {
        Long userId = 1L;
        Goal input = new Goal();
        input.setName("本月编程");
        input.setGoalType("monthly");
        input.setCategoryId(1L);
        input.setTargetValue(3000);
        input.setStartDate(LocalDate.of(2026, 6, 1));
        input.setEndDate(LocalDate.of(2026, 6, 30));

        when(goalMapper.insert(any(Goal.class))).thenAnswer(invocation -> {
            Goal g = invocation.getArgument(0);
            g.setId(1L);
            return 1;
        });
        when(goalMapper.updateById(any(Goal.class))).thenReturn(1);

        lenient().when(timeRecordMapper.findByUserAndDate(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of());
        when(timeRecordMapper.findByUserAndDate(eq(userId), eq(LocalDate.of(2026, 6, 24))))
                .thenReturn(List.of(createRecord(1L, 60)));
        when(timeRecordMapper.findByUserAndDate(eq(userId), eq(LocalDate.of(2026, 6, 25))))
                .thenReturn(List.of(createRecord(1L, 120)));

        Goal result = goalService.create(userId, input);

        assertNotNull(result.getId());
    }

    @Test
    void getById_ShouldThrowIfNotOwner() {
        Long userId = 1L;
        Goal goal = new Goal();
        goal.setId(1L);
        goal.setUserId(2L);

        when(goalMapper.selectById(1L)).thenReturn(goal);

        assertThrows(Exception.class, () -> goalService.getById(userId, 1L));
    }

    @Test
    void getById_ShouldReturnIfOwner() {
        Long userId = 1L;
        Goal goal = new Goal();
        goal.setId(1L);
        goal.setUserId(1L);

        when(goalMapper.selectById(1L)).thenReturn(goal);

        assertDoesNotThrow(() -> {
            Goal result = goalService.getById(userId, 1L);
            assertEquals(1L, result.getId());
        });
    }

    private TimeRecord createRecord(Long categoryId, int durationMin) {
        TimeRecord r = new TimeRecord();
        r.setCategoryId(categoryId);
        r.setDurationMin(durationMin);
        return r;
    }
}
