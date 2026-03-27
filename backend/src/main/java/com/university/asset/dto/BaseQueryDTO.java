package com.university.asset.dto;

import lombok.Data;

@Data
public class BaseQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String dataScope;
}
