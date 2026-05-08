package com.group32.cpt202.CY_project.mapper;

import com.group32.cpt202.CY_project.entity.ContributorApplication;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ContributorApplyMapper {
    @Insert("INSERT INTO contributor_applications(user_id, application_reason, status, created_at) " +
            "VALUES(#{userId}, #{applicationReason}, #{status}, #{createdAt})")
    int insert(ContributorApplication app);

    @Select("SELECT * FROM contributor_applications WHERE status = 'PENDING' ORDER BY created_at DESC")
    List<ContributorApplication> selectPending();

    @Select("SELECT * FROM contributor_applications WHERE id = #{id}")
    ContributorApplication selectById(Long id);

    @Select("SELECT * FROM contributor_applications WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ContributorApplication> selectByUserId(Long userId);

    @Update("UPDATE contributor_applications SET status = #{status}, feedback = #{feedback}, reviewed_by = #{reviewedBy}, reviewed_at = #{reviewedAt} WHERE id = #{id}")
    int updateById(ContributorApplication app);

    @Update("UPDATE contributor_applications SET status = #{status} WHERE user_id = #{userId}")
    int updateStatusByUser(@Param("userId") Long userId, @Param("status") String status);
}
