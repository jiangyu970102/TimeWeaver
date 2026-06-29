package com.timeweaver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timeweaver.entity.WeeklyReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WeeklyReportMapper extends BaseMapper<WeeklyReport> {

    @Select("SELECT * FROM t_weekly_report WHERE user_id = #{userId} AND year = #{year} AND week = #{week} LIMIT 1")
    WeeklyReport findByUserAndWeek(@Param("userId") Long userId, @Param("year") Integer year, @Param("week") Integer week);

    @Select("SELECT * FROM t_weekly_report WHERE user_id = #{userId} ORDER BY year DESC, week DESC LIMIT #{limit}")
    List<WeeklyReport> findRecent(@Param("userId") Long userId, @Param("limit") int limit);
}
