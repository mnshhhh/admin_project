package com.university.asset.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PurchaseOrderDTO {
    private Long id;
    @NotNull(message = "关联申请ID不能为空")
    private Long applicationId;
    private String externalPartner;
    private String contactInfo;
    private BigDecimal totalAmount;
    private LocalDate expectedDate;
    private LocalDate actualDate;
    private String remark;
}
