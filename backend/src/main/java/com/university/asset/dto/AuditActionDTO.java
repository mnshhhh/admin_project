package com.university.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditActionDTO {
    @NotNull(message = "业务ID不能为空")
    private Long bizId;
    @NotBlank(message = "业务类型不能为空")
    private String bizType;
    @NotBlank(message = "操作不能为空")
    private String action;
    private String remark;
}
