package com.university.asset.vo;

import com.university.asset.entity.SysAsset;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AssetVO extends SysAsset {
    private String deptName;
    private String managerName;
    private String categoryName;
}
