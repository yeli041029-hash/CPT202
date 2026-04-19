package com.CY_Project.mapper;

import com.CY_Project.HeritageItem;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface HeritageItemMapper {

    @Select("SELECT * FROM heritage_item WHERE status='PENDING'")
    List<HeritageItem> selectPending();

    @Update("UPDATE heritage_item SET status=#{status} WHERE id=#{id}")
    int updateById(HeritageItem item);

    @Select("SELECT * FROM heritage_item WHERE contributor_id=#{userId} AND status='REJECTED'")
    List<HeritageItem> selectRejectedByUserId(Long userId);
}
