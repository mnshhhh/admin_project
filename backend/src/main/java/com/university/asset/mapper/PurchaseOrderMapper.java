package com.university.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.entity.PurchaseOrder;
import com.university.asset.vo.PurchaseOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {
    IPage<PurchaseOrderVO> selectPurchaseOrderPage(Page<PurchaseOrderVO> page, @Param("status") String status);
    PurchaseOrderVO selectPurchaseOrderVOById(@Param("id") Long id);
}
