package com.university.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("warehouse_entry")
public class WarehouseEntry {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String entryNo;
    private Long purchaseOrderId;
    private Long applicationId;
    private String assetName;
    private String specification;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private LocalDate entryDate;
    private String status;
    private Long approverId;
    private LocalDateTime approveTime;
    private String approveRemark;
    private String remark;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
