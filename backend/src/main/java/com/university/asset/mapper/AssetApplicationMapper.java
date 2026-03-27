package com.university.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.dto.BaseQueryDTO;
import com.university.asset.entity.AssetApplication;
import com.university.asset.vo.AssetApplicationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssetApplicationMapper extends BaseMapper<AssetApplication> {
    IPage<AssetApplicationVO> selectApplicationPage(Page<AssetApplicationVO> page, @Param("query") BaseQueryDTO query, @Param("applicantId") Long applicantId, @Param("status") String status);
    IPage<AssetApplicationVO> selectPendingForAuditor(Page<AssetApplicationVO> page, @Param("auditorDeptId") Long auditorDeptId);
    AssetApplicationVO selectApplicationVOById(@Param("id") Long id);
}
