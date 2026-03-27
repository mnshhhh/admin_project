package com.university.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetApplicationDTO {
    @NotBlank(message = "资产名称不能为空")
    private String assetName;
    private String specification;
    @NotNull(message = "数量不能为空")
    private Integer quantity;
    private BigDecimal estimatedAmount;
    private String reason;
}
