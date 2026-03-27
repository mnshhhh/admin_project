package com.university.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.entity.AssetAllocate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssetAllocateMapper extends BaseMapper<AssetAllocate> {
    IPage<AssetAllocate> selectAllocatePage(Page<AssetAllocate> page, @Param("deptId") Long deptId, @Param("status") String status);
}
