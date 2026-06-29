package com.timeweaver.service;

import com.timeweaver.common.constant.ApiCode;
import com.timeweaver.common.exception.BusinessException;
import com.timeweaver.entity.Category;
import com.timeweaver.entity.Pomodoro;
import com.timeweaver.entity.TimeRecord;
import com.timeweaver.mapper.CategoryMapper;
import com.timeweaver.mapper.PomodoroMapper;
import com.timeweaver.mapper.TimeRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PomodoroServiceTest {

    @Mock
    private PomodoroMapper pomodoroMapper;

    @Mock
    private TimeRecordMapper timeRecordMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Captor
    private ArgumentCaptor<TimeRecord> recordCaptor;

    private PomodoroService pomodoroService;

    @BeforeEach
    void setUp() {
        pomodoroService = new PomodoroService(pomodoroMapper, timeRecordMapper, categoryMapper);
    }

    @Test
    void start_ShouldCreateNewPomodoro() {
        Long userId = 1L;
        when(pomodoroMapper.insert(any(Pomodoro.class))).thenAnswer(invocation -> {
            Pomodoro p = invocation.getArgument(0);
            p.setId(1L);
            return 1;
        });

        Pomodoro result = pomodoroService.start(userId, "写代码", 25);

        assertEquals(userId, result.getUserId());
        assertEquals("写代码", result.getTaskName());
        assertEquals(Integer.valueOf(25), result.getDurationMin());
        assertEquals(Integer.valueOf(0), result.getCompleted());
        assertNotNull(result.getStartTime());
    }

    @Test
    void start_ShouldUseDefaultDuration() {
        Long userId = 1L;
        when(pomodoroMapper.insert(any(Pomodoro.class))).thenAnswer(invocation -> {
            Pomodoro p = invocation.getArgument(0);
            p.setId(2L);
            return 1;
        });

        Pomodoro result = pomodoroService.start(userId, null, null);

        assertEquals(Integer.valueOf(25), result.getDurationMin());
    }

    @Test
    void complete_ShouldCreateTimeRecord() {
        Long userId = 1L;
        Pomodoro pomodoro = new Pomodoro();
        pomodoro.setId(1L);
        pomodoro.setUserId(userId);
        pomodoro.setStartTime(java.time.LocalDateTime.now().minusMinutes(25));
        pomodoro.setDurationMin(25);
        pomodoro.setCompleted(0);
        pomodoro.setTaskName("写代码");

        when(pomodoroMapper.selectById(1L)).thenReturn(pomodoro);
        when(pomodoroMapper.updateById(any(Pomodoro.class))).thenReturn(1);
        when(timeRecordMapper.insert(any(TimeRecord.class))).thenReturn(1);

        // Mock category lookup for default category
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("工作·开发");
        cat.setIsSystem(1);
        when(categoryMapper.findAvailableCategories(userId)).thenReturn(List.of(cat));

        Pomodoro result = pomodoroService.complete(userId, 1L);

        assertEquals(Integer.valueOf(1), result.getCompleted());
        verify(timeRecordMapper).insert(recordCaptor.capture());
        TimeRecord record = recordCaptor.getValue();
        assertEquals(userId, record.getUserId());
        assertTrue(record.getDescription().contains("写代码"));
    }

    @Test
    void complete_ShouldThrowIfNotOwner() {
        Pomodoro pomodoro = new Pomodoro();
        pomodoro.setId(1L);
        pomodoro.setUserId(2L);

        when(pomodoroMapper.selectById(1L)).thenReturn(pomodoro);

        assertThrows(BusinessException.class, () -> pomodoroService.complete(1L, 1L));
    }

    @Test
    void cancel_ShouldSetCompletedToZero() {
        Long userId = 1L;
        Pomodoro pomodoro = new Pomodoro();
        pomodoro.setId(1L);
        pomodoro.setUserId(userId);
        pomodoro.setCompleted(1);

        when(pomodoroMapper.selectById(1L)).thenReturn(pomodoro);
        when(pomodoroMapper.updateById(any(Pomodoro.class))).thenReturn(1);

        Pomodoro result = pomodoroService.cancel(userId, 1L);

        assertEquals(Integer.valueOf(0), result.getCompleted());
    }

}
