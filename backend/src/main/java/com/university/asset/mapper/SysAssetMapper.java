package com.university.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.dto.AssetQueryDTO;
import com.university.asset.entity.SysAsset;
import com.university.asset.vo.AssetVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysAssetMapper extends BaseMapper<SysAsset> {
    IPage<AssetVO> selectAssetPage(Page<AssetVO> page, @Param("query") AssetQueryDTO query);
    AssetVO selectAssetVOById(@Param("id") Long id);
    List<Map<String, Object>> selectStatusStatistics();
    List<Map<String, Object>> selectCategoryStatistics();
}
