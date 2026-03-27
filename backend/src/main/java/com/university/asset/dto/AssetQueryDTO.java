package com.university.asset.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AssetQueryDTO extends BaseQueryDTO {
    private String assetCode;
    private String assetName;
    private Long categoryId;
    private Long deptId;
    private String status;
    private String keyword;
}
