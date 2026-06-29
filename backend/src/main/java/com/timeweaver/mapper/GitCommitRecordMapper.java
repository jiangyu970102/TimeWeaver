package com.timeweaver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timeweaver.entity.GitCommitRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface GitCommitRecordMapper extends BaseMapper<GitCommitRecord> {

    @Select("SELECT * FROM t_git_commit_record WHERE user_id = #{userId} " +
            "AND committed_at >= #{start} AND committed_at < #{end} ORDER BY committed_at DESC")
    List<GitCommitRecord> findByUserAndDateRange(@Param("userId") Long userId,
                                                  @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end);

    @Select("SELECT * FROM t_git_commit_record WHERE user_id = #{userId} " +
            "AND repo_path = #{repoPath} AND committed_at >= #{start} AND committed_at < #{end} " +
            "ORDER BY committed_at DESC")
    List<GitCommitRecord> findByUserAndRepoAndDateRange(@Param("userId") Long userId,
                                                         @Param("repoPath") String repoPath,
                                                         @Param("start") LocalDateTime start,
                                                         @Param("end") LocalDateTime end);

    @Select("SELECT COUNT(1) FROM t_git_commit_record WHERE user_id = #{userId} " +
            "AND commit_hash = #{hash}")
    int countByHash(@Param("userId") Long userId, @Param("hash") String hash);
}
