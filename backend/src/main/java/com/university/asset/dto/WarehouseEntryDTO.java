package com.university.asset.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WarehouseEntryDTO {
    @NotNull(message = "采购单ID不能为空")
    private Long purchaseOrderId;
    private String assetName;
    private String specification;
    @NotNull(message = "入库数量不能为空")
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private LocalDate entryDate;
    private String remark;
}
