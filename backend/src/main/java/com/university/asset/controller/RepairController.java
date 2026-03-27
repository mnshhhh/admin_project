package com.university.asset.controller;

import com.university.asset.common.result.PageResult;
import com.university.asset.common.result.R;
import com.university.asset.dto.RepairApplyDTO;
import com.university.asset.service.RepairService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/repair")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;

    @PostMapping("/apply")
    @PreAuthorize("@ss.hasPermi('repair:add')")
    public R<Void> apply(@Valid @RequestBody RepairApplyDTO dto) {
        repairService.applyRepair(dto);
        return R.ok();
    }

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('repair:manage')")
    public R<PageResult<?>> list(@RequestParam(defaultValue = "1") int pageNum,
                                 @RequestParam(defaultValue = "10") int pageSize,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(defaultValue = "false") boolean onlyMine) {
        return R.ok(PageResult.of(repairService.getRepairPage(pageNum, pageSize, status, onlyMine)));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("@ss.hasPermi('repair:manage')")
    public R<Void> accept(@PathVariable Long id) {
        repairService.acceptRepair(id);
        return R.ok();
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("@ss.hasPermi('repair:manage')")
    public R<Void> complete(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal cost = body.get("cost") != null ? new BigDecimal(body.get("cost").toString()) : null;
        BigDecimal hours = body.get("hours") != null ? new BigDecimal(body.get("hours").toString()) : null;
        String remark = body.get("remark") != null ? body.get("remark").toString() : null;
        repairService.completeRepair(id, cost, hours, remark);
        return R.ok();
    }
}
