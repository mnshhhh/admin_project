package com.university.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.asset.entity.AssetCheckDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssetCheckDetailMapper extends BaseMapper<AssetCheckDetail> {
    List<AssetCheckDetail> selectByTaskId(@Param("taskId") Long taskId);
}
