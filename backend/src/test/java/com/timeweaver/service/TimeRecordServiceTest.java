package com.timeweaver.service;

import com.timeweaver.common.constant.ApiCode;
import com.timeweaver.common.exception.BusinessException;
import com.timeweaver.entity.TimeRecord;
import com.timeweaver.mapper.TimeRecordMapper;
import com.timeweaver.dto.request.RecordCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeRecordServiceTest {

    @Mock
    private TimeRecordMapper timeRecordMapper;

    private TimeRecordService timeRecordService;

    @BeforeEach
    void setUp() {
        timeRecordService = new TimeRecordService(timeRecordMapper);
    }

    @Test
    void getRecordsByDate_ShouldReturnList() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 6, 28);

        when(timeRecordMapper.findByUserAndDate(userId, date))
                .thenReturn(List.of(new TimeRecord(), new TimeRecord()));

        List<TimeRecord> records = timeRecordService.getRecordsByDate(userId, date);

        assertEquals(2, records.size());
    }

    @Test
    void startRecord_ShouldThrowIfOngoing() {
        Long userId = 1L;
        when(timeRecordMapper.findOngoingRecord(userId)).thenReturn(new TimeRecord());

        RecordCreateRequest request = new RecordCreateRequest();
        request.setCategoryId(1L);

        assertThrows(BusinessException.class, () -> timeRecordService.startRecord(userId, request));
    }

    @Test
    void startRecord_ShouldCreateNewRecord() {
        Long userId = 1L;
        when(timeRecordMapper.findOngoingRecord(userId)).thenReturn(null);
        when(timeRecordMapper.insert(any(TimeRecord.class))).thenReturn(1);

        RecordCreateRequest request = new RecordCreateRequest();
        request.setCategoryId(1L);
        request.setDescription("测试记录");

        TimeRecord result = timeRecordService.startRecord(userId, request);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("测试记录", result.getDescription());
    }

    @Test
    void stopRecord_ShouldSetEndTime() {
        Long userId = 1L;
        TimeRecord record = new TimeRecord();
        record.setId(1L);
        record.setUserId(userId);
        record.setStartTime(LocalDateTime.now().minusHours(1));

        when(timeRecordMapper.selectById(1L)).thenReturn(record);
        when(timeRecordMapper.updateById(any(TimeRecord.class))).thenReturn(1);

        TimeRecord result = timeRecordService.stopRecord(userId, 1L);

        assertNotNull(result.getEndTime());
        assertNotNull(result.getDurationMin());
    }

    @Test
    void stopRecord_ShouldThrowIfNotOwner() {
        TimeRecord record = new TimeRecord();
        record.setId(1L);
        record.setUserId(2L);

        when(timeRecordMapper.selectById(1L)).thenReturn(record);

        assertThrows(BusinessException.class, () -> timeRecordService.stopRecord(1L, 1L));
    }

    @Test
    void deleteRecord_ShouldDeleteIfOwner() {
        Long userId = 1L;
        TimeRecord record = new TimeRecord();
        record.setId(1L);
        record.setUserId(userId);

        when(timeRecordMapper.selectById(1L)).thenReturn(record);
        when(timeRecordMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> timeRecordService.deleteRecord(userId, 1L));
        verify(timeRecordMapper).deleteById(1L);
    }
}
