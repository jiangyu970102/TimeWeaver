package com.timeweaver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timeweaver.entity.AiSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AiSessionMapper extends BaseMapper<AiSession> {

    @Select("SELECT * FROM t_ai_session WHERE user_id = #{userId} AND start_time >= #{start} AND start_time < #{end} ORDER BY start_time DESC")
    List<AiSession> findByUserAndTimeRange(@Param("userId") Long userId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Select("SELECT tool_name AS name, COUNT(*) AS count, SUM(duration_sec) AS totalSeconds, SUM(token_count) AS totalTokens, SUM(cost_estimate) AS totalCost " +
            "FROM t_ai_session WHERE user_id = #{userId} AND start_time >= #{start} AND start_time < #{end} " +
            "GROUP BY tool_name ORDER BY totalSeconds DESC")
    List<Map<String, Object>> getToolStats(@Param("userId") Long userId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Select("SELECT model_name AS name, COUNT(*) AS count, SUM(duration_sec) AS totalSeconds, SUM(token_count) AS totalTokens, SUM(cost_estimate) AS totalCost " +
            "FROM t_ai_session WHERE user_id = #{userId} AND start_time >= #{start} AND start_time < #{end} " +
            "GROUP BY model_name ORDER BY totalSeconds DESC")
    List<Map<String, Object>> getModelStats(@Param("userId") Long userId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Select("SELECT DATE(start_time) AS date, tool_name, COUNT(*) AS count, SUM(duration_sec) AS totalSeconds, SUM(token_count) AS totalTokens " +
            "FROM t_ai_session WHERE user_id = #{userId} AND start_time >= #{start} AND start_time < #{end} " +
            "GROUP BY DATE(start_time), tool_name ORDER BY date")
    List<Map<String, Object>> getDailyTrend(@Param("userId") Long userId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Select("SELECT COALESCE(SUM(token_count), 0) AS totalTokens, COALESCE(SUM(cost_estimate), 0) AS totalCost, COUNT(*) AS totalSessions " +
            "FROM t_ai_session WHERE user_id = #{userId} AND start_time >= #{start} AND start_time < #{end}")
    Map<String, Object> getSummaryStats(@Param("userId") Long userId,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);
}
