package com.university.asset.vo;

import com.university.asset.entity.WarehouseEntry;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WarehouseEntryVO extends WarehouseEntry {
    private String orderNo;
    private String applyNo;
    private String approverName;
}
