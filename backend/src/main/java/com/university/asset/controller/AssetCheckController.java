package com.university.asset.controller;

import com.university.asset.common.result.R;
import com.university.asset.entity.AssetCheckDetail;
import com.university.asset.entity.AssetCheckTask;
import com.university.asset.service.AssetCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/asset/check")
@RequiredArgsConstructor
public class AssetCheckController {

    private final AssetCheckService checkService;

    @GetMapping("/tasks")
    @PreAuthorize("@ss.hasPermi('asset:check')")
    public R<List<AssetCheckTask>> tasks() {
        return R.ok(checkService.getAllTasks());
    }

    @PostMapping("/task")
    @PreAuthorize("@ss.hasPermi('asset:check')")
    public R<AssetCheckTask> createTask(@RequestBody Map<String, Object> body) {
        String taskName = body.get("taskName").toString();
        Long deptId = body.get("deptId") != null ? Long.valueOf(body.get("deptId").toString()) : null;
        String remark = body.get("remark") != null ? body.get("remark").toString() : null;
        return R.ok(checkService.createTask(taskName, deptId, remark));
    }

    @PostMapping("/task/{taskId}/scan")
    @PreAuthorize("@ss.hasPermi('asset:check')")
    public R<Void> scan(@PathVariable Long taskId, @RequestBody Map<String, String> body) {
        checkService.scanCheck(taskId, body.get("assetCode"));
        return R.ok();
    }

    @PutMapping("/task/{taskId}/complete")
    @PreAuthorize("@ss.hasPermi('asset:check')")
    public R<Void> complete(@PathVariable Long taskId) {
        checkService.completeTask(taskId);
        return R.ok();
    }

    @GetMapping("/task/{taskId}/details")
    @PreAuthorize("@ss.hasPermi('asset:check')")
    public R<List<AssetCheckDetail>> details(@PathVariable Long taskId) {
        return R.ok(checkService.getTaskDetails(taskId));
    }
}
