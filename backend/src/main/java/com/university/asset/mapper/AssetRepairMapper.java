package com.university.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.entity.AssetRepair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssetRepairMapper extends BaseMapper<AssetRepair> {
    IPage<AssetRepair> selectRepairPage(Page<AssetRepair> page, @Param("status") String status, @Param("repairManId") Long repairManId);
}
