package com.university.asset.controller;

import com.university.asset.common.annotation.Log;
import com.university.asset.common.result.R;
import com.university.asset.entity.SysDept;
import com.university.asset.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @GetMapping("/tree")
    public R<List<SysDept>> tree() {
        return R.ok(deptService.getDeptTree());
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('sys:dept:manage')")
    @Log("新增部门")
    public R<Void> add(@RequestBody SysDept dept) {
        deptService.saveDept(dept);
        return R.ok();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('sys:dept:manage')")
    @Log("编辑部门")
    public R<Void> edit(@RequestBody SysDept dept) {
        deptService.saveDept(dept);
        return R.ok();
    }

    @DeleteMapping("/{deptId}")
    @PreAuthorize("@ss.hasPermi('sys:dept:manage')")
    @Log("删除部门")
    public R<Void> delete(@PathVariable Long deptId) {
        deptService.deleteDept(deptId);
        return R.ok();
    }
}
