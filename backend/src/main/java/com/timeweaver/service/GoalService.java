package com.timeweaver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.timeweaver.common.constant.ApiCode;
import com.timeweaver.common.exception.BusinessException;
import com.timeweaver.entity.Goal;
import com.timeweaver.mapper.GoalMapper;
import com.timeweaver.mapper.TimeRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalMapper goalMapper;
    private final TimeRecordMapper timeRecordMapper;

    public List<Goal> listGoals(Long userId, Boolean activeOnly) {
        LambdaQueryWrapper<Goal> wrapper = new LambdaQueryWrapper<Goal>()
                .eq(Goal::getUserId, userId);
        if (Boolean.TRUE.equals(activeOnly)) {
            wrapper.eq(Goal::getStatus, 0);
        }
        wrapper.orderByDesc(Goal::getCreatedAt);
        return goalMapper.selectList(wrapper);
    }

    public Goal getById(Long userId, Long id) {
        Goal goal = goalMapper.selectById(id);
        if (goal == null) throw new BusinessException(ApiCode.NOT_FOUND, "目标不存在");
        if (!goal.getUserId().equals(userId)) throw new BusinessException(ApiCode.FORBIDDEN);
        return goal;
    }

    @Transactional
    public Goal create(Long userId, Goal goal) {
        goal.setId(null);
        goal.setUserId(userId);
        goal.setCurrentValue(0);
        if (goal.getStatus() == null) goal.setStatus(0);
        if (goal.getStartDate() == null) goal.setStartDate(LocalDate.now());
        goalMapper.insert(goal);
        recalculateProgress(goal);
        return goal;
    }

    @Transactional
    public Goal update(Long userId, Long id, Goal update) {
        Goal goal = getById(userId, id);
        if (update.getName() != null) goal.setName(update.getName());
        if (update.getGoalType() != null) goal.setGoalType(update.getGoalType());
        if (update.getCategoryId() != null) goal.setCategoryId(update.getCategoryId());
        if (update.getTargetValue() != null) goal.setTargetValue(update.getTargetValue());
        if (update.getUnit() != null) goal.setUnit(update.getUnit());
        if (update.getStartDate() != null) goal.setStartDate(update.getStartDate());
        if (update.getEndDate() != null) goal.setEndDate(update.getEndDate());
        if (update.getStatus() != null) goal.setStatus(update.getStatus());
        goalMapper.updateById(goal);
        return goal;
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Goal goal = getById(userId, id);
        goalMapper.deleteById(goal.getId());
    }

    @Transactional
    public Goal updateStatus(Long userId, Long id, Integer status) {
        Goal goal = getById(userId, id);
        goal.setStatus(status);
        goalMapper.updateById(goal);
        return goal;
    }

    @Transactional
    public Goal recalculateProgress(Long userId, Long id) {
        Goal goal = getById(userId, id);
        recalculateProgress(goal);
        return goal;
    }

    private void recalculateProgress(Goal goal) {
        LocalDate start = goal.getStartDate() != null ? goal.getStartDate() : LocalDate.now().withDayOfMonth(1);
        LocalDate end = goal.getEndDate() != null ? goal.getEndDate() : LocalDate.now();

        // Sum minutes from time records in the goal's date range
        // If a category is specified, filter by that category
        int total = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            var records = timeRecordMapper.findByUserAndDate(goal.getUserId(), d);
            for (var r : records) {
                if (r.getDurationMin() != null
                        && (goal.getCategoryId() == null || goal.getCategoryId().equals(r.getCategoryId()))) {
                    total += r.getDurationMin();
                }
            }
        }

        goal.setCurrentValue(total);
        goalMapper.updateById(goal);
    }

    public Map<String, Object> getSummary(Long userId) {
        List<Goal> activeGoals = goalMapper.findActiveGoals(userId);
        int total = activeGoals.size();
        int completed = (int) activeGoals.stream()
                .filter(g -> g.getTargetValue() != null && g.getTargetValue() > 0
                        && g.getCurrentValue() != null && g.getCurrentValue() >= g.getTargetValue())
                .count();
        return Map.of("activeCount", total, "completedCount", completed);
    }
}
