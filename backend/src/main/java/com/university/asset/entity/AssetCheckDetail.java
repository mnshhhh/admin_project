package com.university.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("asset_check_detail")
public class AssetCheckDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long expectedDeptId;
    private String checkResult;
    private Long checkerId;
    private LocalDateTime checkTime;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
