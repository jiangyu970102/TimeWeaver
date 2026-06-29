package com.timeweaver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timeweaver.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT * FROM t_category WHERE user_id = #{userId} OR is_system = 1 ORDER BY sort_order ASC")
    List<Category> findAvailableCategories(@Param("userId") Long userId);
}
