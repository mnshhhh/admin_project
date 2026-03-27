package com.university.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("asset_borrow")
public class AssetBorrow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String borrowNo;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long applicantId;
    private Long deptId;
    private String purpose;
    private LocalDateTime borrowTime;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime;
    private String status;
    private Long approverId;
    private LocalDateTime approveTime;
    private String approveRemark;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
