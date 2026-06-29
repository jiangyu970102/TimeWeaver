package com.timeweaver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timeweaver.entity.GitRepoConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GitRepoConfigMapper extends BaseMapper<GitRepoConfig> {
}
