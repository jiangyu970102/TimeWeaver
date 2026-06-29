package com.timeweaver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timeweaver.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM t_user WHERE username = #{username} LIMIT 1")
    User selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM t_user WHERE email = #{email} LIMIT 1")
    User selectByEmail(@Param("email") String email);
}
