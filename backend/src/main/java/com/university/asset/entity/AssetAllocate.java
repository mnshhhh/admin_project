package com.university.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("asset_allocate")
public class AssetAllocate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String allocateNo;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long fromDeptId;
    private Long toDeptId;
    private Long applicantId;
    private String reason;
    private String status;
    private Long fromConfirmId;
    private LocalDateTime fromConfirmTime;
    private Long toConfirmId;
    private LocalDateTime toConfirmTime;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
