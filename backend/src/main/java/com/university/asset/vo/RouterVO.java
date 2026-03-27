package com.university.asset.vo;

import lombok.Data;

import java.util.List;

@Data
public class RouterVO {
    private String name;
    private String path;
    private String component;
    private RouterMeta meta;
    private List<RouterVO> children;

    @Data
    public static class RouterMeta {
        private String title;
        private String icon;
        private boolean hidden;
    }
}
