package com.timeweaver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timeweaver.entity.Goal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GoalMapper extends BaseMapper<Goal> {

    @Select("SELECT * FROM t_goal WHERE user_id = #{userId} AND status = 0 ORDER BY created_at DESC")
    List<Goal> findActiveGoals(@Param("userId") Long userId);
}
