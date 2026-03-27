package com.university.asset.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RepairApplyDTO {
    @NotNull(message = "资产ID不能为空")
    private Long assetId;
    private String description;
    private String images;
}
