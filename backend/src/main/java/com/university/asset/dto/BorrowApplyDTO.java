package com.university.asset.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BorrowApplyDTO {
    @NotNull(message = "资产ID不能为空")
    private Long assetId;
    private String purpose;
    @NotNull(message = "预计归还时间不能为空")
    private LocalDateTime expectedReturnTime;
}
