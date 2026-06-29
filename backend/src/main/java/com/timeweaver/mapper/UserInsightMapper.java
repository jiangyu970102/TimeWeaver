package com.timeweaver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timeweaver.entity.UserInsight;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserInsightMapper extends BaseMapper<UserInsight> {

    @Select("SELECT * FROM t_user_insight WHERE user_id = #{userId} ORDER BY generated_at DESC LIMIT #{limit}")
    List<UserInsight> findLatest(@Param("userId") Long userId, @Param("limit") int limit);
}
