package com.university.asset.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.common.result.PageResult;
import com.university.asset.common.result.R;
import com.university.asset.mapper.SysLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/log")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLogMapper logMapper;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('sys:log:view')")
    public R<PageResult<?>> list(@RequestParam(defaultValue = "1") int pageNum,
                                 @RequestParam(defaultValue = "10") int pageSize,
                                 @RequestParam(required = false) String username,
                                 @RequestParam(required = false) String operation) {
        return R.ok(PageResult.of(logMapper.selectLogPage(new Page<>(pageNum, pageSize), username, operation)));
    }
}
