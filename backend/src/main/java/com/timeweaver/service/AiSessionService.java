package com.timeweaver.service;

import com.timeweaver.common.constant.ApiCode;
import com.timeweaver.common.exception.BusinessException;
import com.timeweaver.entity.AiSession;
import com.timeweaver.mapper.AiSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiSessionService {

    private final AiSessionMapper aiSessionMapper;

    public List<AiSession> getSessions(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return aiSessionMapper.findByUserAndTimeRange(userId, start, end);
    }

    public List<AiSession> getSessionsByRange(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        return aiSessionMapper.findByUserAndTimeRange(userId, start, end);
    }

    public AiSession getById(Long userId, Long id) {
        AiSession session = aiSessionMapper.selectById(id);
        if (session == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "AI 会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ApiCode.FORBIDDEN, "无权访问此会话");
        }
        return session;
    }

    @Transactional
    public AiSession createSession(Long userId, AiSession session) {
        session.setId(null);
        session.setUserId(userId);
        if (session.getStartTime() == null) {
            session.setStartTime(LocalDateTime.now());
        }
        if (session.getSource() == null) {
            session.setSource("manual");
        }
        // Calculate duration if end_time is set
        if (session.getEndTime() != null && session.getDurationSec() == null) {
            session.setDurationSec((int) java.time.Duration.between(session.getStartTime(), session.getEndTime()).getSeconds());
        }
        aiSessionMapper.insert(session);
        return session;
    }

    @Transactional
    public AiSession updateSession(Long userId, Long id, AiSession updates) {
        AiSession existing = getById(userId, id);
        if (updates.getToolName() != null) existing.setToolName(updates.getToolName());
        if (updates.getModelName() != null) existing.setModelName(updates.getModelName());
        if (updates.getStartTime() != null) existing.setStartTime(updates.getStartTime());
        if (updates.getEndTime() != null) existing.setEndTime(updates.getEndTime());
        if (updates.getDurationSec() != null) existing.setDurationSec(updates.getDurationSec());
        if (updates.getTokenCount() != null) existing.setTokenCount(updates.getTokenCount());
        if (updates.getCostEstimate() != null) existing.setCostEstimate(updates.getCostEstimate());
        if (updates.getSessionMeta() != null) existing.setSessionMeta(updates.getSessionMeta());
        if (updates.getRecordId() != null) existing.setRecordId(updates.getRecordId());
        // Recalculate duration if end_time changed
        if (updates.getEndTime() != null && existing.getStartTime() != null && updates.getDurationSec() == null) {
            existing.setDurationSec((int) java.time.Duration.between(existing.getStartTime(), existing.getEndTime()).getSeconds());
        }
        aiSessionMapper.updateById(existing);
        return existing;
    }

    @Transactional
    public void deleteSession(Long userId, Long id) {
        AiSession existing = getById(userId, id);
        aiSessionMapper.deleteById(existing.getId());
    }

    public Map<String, Object> getStats(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        Map<String, Object> result = new HashMap<>();
        result.put("summary", aiSessionMapper.getSummaryStats(userId, start, end));
        result.put("toolStats", aiSessionMapper.getToolStats(userId, start, end));
        result.put("modelStats", aiSessionMapper.getModelStats(userId, start, end));
        result.put("dailyTrend", aiSessionMapper.getDailyTrend(userId, start, end));
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        return result;
    }
}
