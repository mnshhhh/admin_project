package com.university.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.dto.BaseQueryDTO;
import com.university.asset.entity.AssetBorrow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssetBorrowMapper extends BaseMapper<AssetBorrow> {
    IPage<AssetBorrow> selectBorrowPage(Page<AssetBorrow> page, @Param("query") BaseQueryDTO query, @Param("applicantId") Long applicantId, @Param("status") String status);
    IPage<AssetBorrow> selectPendingForAuditor(Page<AssetBorrow> page, @Param("auditorDeptId") Long auditorDeptId);
}
