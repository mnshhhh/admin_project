package com.university.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssetSaveDTO {
    private Long id;
    @NotBlank(message = "资产名称不能为空")
    private String assetName;
    private Long categoryId;
    private String brand;
    private String model;
    private String serialNo;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private BigDecimal currentValue;
    private Integer depreciationYears;
    @NotNull(message = "归属部门不能为空")
    private Long deptId;
    private Long managerId;
    private String location;
    private String remark;
}
