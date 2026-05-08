package com.group32.cpt202.CY_project.mapper;

import com.group32.cpt202.CY_project.entity.HeritageItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface HeritageItemMapper {
    @Select("SELECT * FROM heritage_item WHERE status = 'PENDING' ORDER BY updated_at DESC")
    List<HeritageItem> selectPending();

    @Select("SELECT * FROM heritage_item WHERE id = #{id}")
    HeritageItem selectById(Long id);

    @Update("UPDATE heritage_item SET status = #{status} WHERE id = #{id}")
    int updateStatusById(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT * FROM heritage_item WHERE contributor_id = #{userId} AND status = 'REJECTED' ORDER BY updated_at DESC")
    List<HeritageItem> selectRejectedByUserId(Long userId);

    @Update("UPDATE heritage_item SET title = #{title}, description = #{description}, category = #{category}, location = #{location}, image_url = #{imageUrl}, status = #{status} WHERE id = #{id}")
    int updateForResubmit(HeritageItem item);
}
