package com.university.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_asset")
public class SysAsset {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String assetCode;
    private String assetName;
    private Long categoryId;
    private String brand;
    private String model;
    private String serialNo;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private BigDecimal currentValue;
    private Integer depreciationYears;
    private Long deptId;
    private Long managerId;
    private String location;
    private String status;
    private String qrCodeUrl;
    private String remark;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
