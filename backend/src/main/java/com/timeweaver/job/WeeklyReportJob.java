package com.timeweaver.job;

import com.timeweaver.mapper.UserMapper;
import com.timeweaver.entity.User;
import com.timeweaver.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyReportJob {

    private final UserMapper userMapper;
    private final ReportService reportService;

    /**
     * 每周日 20:00 自动为所有活跃用户生成周报
     */
    @Scheduled(cron = "0 0 20 * * SUN")
    public void generateWeeklyReports() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int week = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);

        log.info("WeeklyReportJob started: generating reports for {} week {}", year, week);

        List<User> activeUsers = userMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getStatus, 1));

        int success = 0;
        for (User user : activeUsers) {
            try {
                reportService.generateReport(user.getId(), year, week);
                success++;
            } catch (Exception e) {
                log.error("Failed to generate weekly report for userId={}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("WeeklyReportJob completed: {}/{} reports generated", success, activeUsers.size());
    }
}
