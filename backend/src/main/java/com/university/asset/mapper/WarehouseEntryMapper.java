package com.university.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.entity.WarehouseEntry;
import com.university.asset.vo.WarehouseEntryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WarehouseEntryMapper extends BaseMapper<WarehouseEntry> {
    IPage<WarehouseEntryVO> selectWarehouseEntryPage(Page<WarehouseEntryVO> page, @Param("status") String status);
    WarehouseEntryVO selectWarehouseEntryVOById(@Param("id") Long id);
}
