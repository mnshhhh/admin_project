package com.university.asset.vo;

import com.university.asset.entity.PurchaseOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderVO extends PurchaseOrder {
    private String applyNo;
    private String assetName;
    private String applicantName;
}
