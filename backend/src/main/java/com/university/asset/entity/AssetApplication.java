package com.university.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("asset_application")
public class AssetApplication {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String applyNo;
    private Long applicantId;
    private Long deptId;
    private String assetName;
    private String specification;
    private Integer quantity;
    private BigDecimal estimatedAmount;
    private String reason;
    private String status;
    private Long approverId;
    private LocalDateTime approveTime;
    private String approveRemark;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
