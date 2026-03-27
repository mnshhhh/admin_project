package com.university.asset.vo;

import com.university.asset.entity.AssetApplication;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AssetApplicationVO extends AssetApplication {
    private String applicantName;
    private String deptName;
    private String approverName;
}
