package com.university.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("asset_repair")
public class AssetRepair {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String repairNo;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long reporterId;
    private Long deptId;
    private String description;
    private String images;
    private Long repairManId;
    private BigDecimal repairCost;
    private BigDecimal repairHours;
    private String status;
    private LocalDateTime completeTime;
    private String repairRemark;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
