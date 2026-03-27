package com.university.asset.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private long total;
    private List<T> rows;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRows(page.getRecords());
        return result;
    }

    public static <T> PageResult<T> of(long total, List<T> rows) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setRows(rows);
        return result;
    }
}
