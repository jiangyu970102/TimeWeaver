package com.timeweaver.service;

import com.timeweaver.common.constant.ApiCode;
import com.timeweaver.common.exception.BusinessException;
import com.timeweaver.dto.request.RecordCreateRequest;
import com.timeweaver.entity.TimeRecord;
import com.timeweaver.mapper.TimeRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeRecordService {

    private final TimeRecordMapper timeRecordMapper;

    public List<TimeRecord> getRecordsByDate(Long userId, LocalDate date) {
        return timeRecordMapper.findByUserAndDate(userId, date);
    }

    public TimeRecord getCurrentRecord(Long userId) {
        return timeRecordMapper.findOngoingRecord(userId);
    }

    @Transactional
    public TimeRecord startRecord(Long userId, RecordCreateRequest request) {
        // Check no ongoing record
        if (timeRecordMapper.findOngoingRecord(userId) != null) {
            throw new BusinessException(ApiCode.RECORD_ONGOING_EXISTS);
        }

        TimeRecord record = new TimeRecord();
        record.setUserId(userId);
        record.setCategoryId(request.getCategoryId());
        record.setDescription(request.getDescription());
        record.setTags(request.getTags());
        record.setSource(request.getSource() != null ? request.getSource() : "manual");
        record.setStartTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now());
        record.setRecordDate(LocalDate.now());

        timeRecordMapper.insert(record);
        return record;
    }

    @Transactional
    public TimeRecord stopRecord(Long userId, Long recordId) {
        TimeRecord record = timeRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ApiCode.RECORD_NOT_FOUND);
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ApiCode.FORBIDDEN);
        }
        if (record.getEndTime() != null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "记录已结束");
        }

        record.setEndTime(LocalDateTime.now());
        record.setDurationMin((int) ChronoUnit.MINUTES.between(record.getStartTime(), record.getEndTime()));
        timeRecordMapper.updateById(record);
        return record;
    }

    @Transactional
    public TimeRecord createManualRecord(Long userId, RecordCreateRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "手动记录需要开始和结束时间");
        }

        TimeRecord record = new TimeRecord();
        record.setUserId(userId);
        record.setCategoryId(request.getCategoryId());
        record.setDescription(request.getDescription());
        record.setTags(request.getTags());
        record.setSource("manual");
        record.setStartTime(request.getStartTime());
        record.setEndTime(request.getEndTime());
        record.setDurationMin((int) ChronoUnit.MINUTES.between(request.getStartTime(), request.getEndTime()));
        record.setRecordDate(request.getStartTime().toLocalDate());

        timeRecordMapper.insert(record);
        return record;
    }

    @Transactional
    public void deleteRecord(Long userId, Long recordId) {
        TimeRecord record = timeRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ApiCode.RECORD_NOT_FOUND);
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ApiCode.FORBIDDEN);
        }
        timeRecordMapper.deleteById(recordId);
    }

    @Transactional
    public TimeRecord updateRecord(Long userId, Long recordId, RecordCreateRequest request) {
        TimeRecord record = timeRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ApiCode.RECORD_NOT_FOUND);
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ApiCode.FORBIDDEN);
        }

        if (request.getCategoryId() != null) record.setCategoryId(request.getCategoryId());
        if (request.getDescription() != null) record.setDescription(request.getDescription());
        if (request.getTags() != null) record.setTags(request.getTags());
        if (request.getStartTime() != null) record.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) {
            record.setEndTime(request.getEndTime());
            record.setDurationMin((int) ChronoUnit.MINUTES.between(record.getStartTime(), record.getEndTime()));
        }

        timeRecordMapper.updateById(record);
        return record;
    }
}
