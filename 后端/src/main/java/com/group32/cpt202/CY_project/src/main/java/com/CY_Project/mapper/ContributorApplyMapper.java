package com.CY_Project.mapper;

import com.CY_Project.entity.ContributorApplication;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ContributorApplyMapper {
    @Insert("INSERT INTO contributor_applications(user_id,application_reason,status,created_at) " +
            "VALUES(#{userId},#{applicationReason},#{status},#{createdAt})")
    int insert(ContributorApplication app);

    @Select("SELECT * FROM contributor_applications WHERE status='PENDING'")
    List<ContributorApplication> selectPending();

    @Update("UPDATE contributor_applications SET status=#{status},feedback=#{feedback}," +
            "reviewed_by=#{reviewedBy},reviewed_at=#{reviewedAt} WHERE id=#{id}")
    int updateById(ContributorApplication app);

    @Select("SELECT * FROM contributor_applications WHERE user_id=#{userId}")
    List<ContributorApplication> selectByUserId(Long userId);

    @Update("UPDATE contributor_applications SET status=#{status} WHERE user_id=#{userId}")
    int updateStatusByUser(@Param("userId") Long userId, @Param("status") String status);
}