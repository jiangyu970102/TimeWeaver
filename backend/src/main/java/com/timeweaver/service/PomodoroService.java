package com.timeweaver.service;

import com.timeweaver.common.constant.ApiCode;
import com.timeweaver.common.exception.BusinessException;
import com.timeweaver.entity.Category;
import com.timeweaver.entity.Pomodoro;
import com.timeweaver.entity.TimeRecord;
import com.timeweaver.mapper.CategoryMapper;
import com.timeweaver.mapper.PomodoroMapper;
import com.timeweaver.mapper.TimeRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PomodoroService {

    private final PomodoroMapper pomodoroMapper;
    private final TimeRecordMapper timeRecordMapper;
    private final CategoryMapper categoryMapper;

    private Long getDefaultCategoryId(Long userId) {
        // Look for system preset "工作·开发" category, fallback to any system category, then any category
        List<Category> categories = categoryMapper.findAvailableCategories(userId);
        return categories.stream()
                .filter(c -> "工作·开发".equals(c.getName()) && c.getIsSystem() == 1)
                .findFirst()
                .or(() -> categories.stream().filter(c -> c.getIsSystem() == 1).findFirst())
                .or(() -> categories.stream().findFirst())
                .map(Category::getId)
                .orElse(1L);
    }

    @Transactional
    public Pomodoro start(Long userId, String taskName, Integer durationMin) {
        int duration = durationMin != null ? durationMin : 25;

        Pomodoro pomodoro = new Pomodoro();
        pomodoro.setUserId(userId);
        pomodoro.setStartTime(LocalDateTime.now());
        pomodoro.setDurationMin(duration);
        pomodoro.setTaskName(taskName);
        pomodoro.setCompleted(0);
        pomodoro.setTomatoCount(1);
        pomodoroMapper.insert(pomodoro);
        return pomodoro;
    }

    @Transactional
    public Pomodoro complete(Long userId, Long id) {
        Pomodoro pomodoro = pomodoroMapper.selectById(id);
        if (pomodoro == null) throw new BusinessException(ApiCode.NOT_FOUND, "番茄钟不存在");
        if (!pomodoro.getUserId().equals(userId)) throw new BusinessException(ApiCode.FORBIDDEN);

        pomodoro.setEndTime(LocalDateTime.now());
        pomodoro.setCompleted(1);
        int actualDuration = (int) ChronoUnit.MINUTES.between(pomodoro.getStartTime(), pomodoro.getEndTime());
        pomodoro.setDurationMin(Math.max(1, actualDuration));
        pomodoroMapper.updateById(pomodoro);

        // Auto-create time record for completed pomodoro
        TimeRecord record = new TimeRecord();
        record.setUserId(userId);
        record.setCategoryId(getDefaultCategoryId(userId));
        record.setStartTime(pomodoro.getStartTime());
        record.setEndTime(pomodoro.getEndTime());
        record.setDurationMin(pomodoro.getDurationMin());
        record.setDescription(pomodoro.getTaskName() != null ? "🍅 " + pomodoro.getTaskName() : "🍅 番茄钟");
        record.setSource("pomodoro");
        record.setRecordDate(LocalDate.now());
        timeRecordMapper.insert(record);

        return pomodoro;
    }

    @Transactional
    public Pomodoro cancel(Long userId, Long id) {
        Pomodoro pomodoro = pomodoroMapper.selectById(id);
        if (pomodoro == null) throw new BusinessException(ApiCode.NOT_FOUND, "番茄钟不存在");
        if (!pomodoro.getUserId().equals(userId)) throw new BusinessException(ApiCode.FORBIDDEN);

        pomodoro.setEndTime(LocalDateTime.now());
        pomodoro.setCompleted(0);
        pomodoroMapper.updateById(pomodoro);
        return pomodoro;
    }

    public Map<String, Object> getStats(Long userId) {
        LocalDate today = LocalDate.now();

        // Today's completed count
        List<Pomodoro> todayPomodoros = pomodoroMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Pomodoro>()
                        .eq(Pomodoro::getUserId, userId)
                        .eq(Pomodoro::getCompleted, 1)
                        .apply("DATE(start_time) = {0}", today));

        int todayCount = todayPomodoros.size();

        // Streak (consecutive days with at least 1 pomodoro)
        int streak = 0;
        LocalDate check = today.minusDays(1);
        while (true) {
            Long count = pomodoroMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Pomodoro>()
                            .eq(Pomodoro::getUserId, userId)
                            .eq(Pomodoro::getCompleted, 1)
                            .apply("DATE(start_time) = {0}", check));
            if (count > 0) {
                streak++;
                check = check.minusDays(1);
            } else break;
        }

        // Total all-time count
        long totalCount = pomodoroMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Pomodoro>()
                        .eq(Pomodoro::getUserId, userId)
                        .eq(Pomodoro::getCompleted, 1));

        Map<String, Object> stats = new HashMap<>();
        stats.put("todayCount", todayCount);
        stats.put("streak", streak);
        stats.put("totalCount", totalCount);
        stats.put("durationMin", 25);
        stats.put("breakMin", 5);
        return stats;
    }
}
