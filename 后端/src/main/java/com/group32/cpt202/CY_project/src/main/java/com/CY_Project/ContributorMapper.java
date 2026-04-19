package com.CY_Project;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ContributorMapper {
    @Select("SELECT * FROM contributor_applications")
    List<ContributorApplications> getAllApplications();
}