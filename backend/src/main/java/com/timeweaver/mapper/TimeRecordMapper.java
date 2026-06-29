package com.timeweaver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timeweaver.entity.TimeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TimeRecordMapper extends BaseMapper<TimeRecord> {

    @Select("SELECT * FROM t_time_record WHERE user_id = #{userId} AND record_date = #{date} ORDER BY start_time ASC")
    List<TimeRecord> findByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT * FROM t_time_record WHERE user_id = #{userId} AND record_date >= #{start} AND record_date <= #{end} ORDER BY start_time ASC")
    List<TimeRecord> findByUserAndDateRange(@Param("userId") Long userId,
                                            @Param("start") LocalDate start,
                                            @Param("end") LocalDate end);

    @Select("SELECT * FROM t_time_record WHERE user_id = #{userId} AND end_time IS NULL LIMIT 1")
    TimeRecord findOngoingRecord(@Param("userId") Long userId);
}
