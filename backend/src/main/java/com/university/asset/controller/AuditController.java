package com.university.asset.controller;

import com.university.asset.common.result.PageResult;
import com.university.asset.common.result.R;
import com.university.asset.dto.AuditActionDTO;
import com.university.asset.dto.BaseQueryDTO;
import com.university.asset.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/pending")
    @PreAuthorize("@ss.hasPermi('audit:approve')")
    public R<PageResult<?>> pending(BaseQueryDTO query) {
        return R.ok(PageResult.of(auditService.getPendingBorrows(query)));
    }

    @GetMapping("/myBorrows")
    @PreAuthorize("@ss.hasPermi('audit:view')")
    public R<PageResult<?>> myBorrows(BaseQueryDTO query,
                                      @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(auditService.getMyBorrows(query, status)));
    }

    @PostMapping("/action")
    @PreAuthorize("@ss.hasPermi('audit:approve')")
    public R<Void> action(@Valid @RequestBody AuditActionDTO dto) {
        auditService.audit(dto);
        return R.ok();
    }

    @GetMapping("/history")
    @PreAuthorize("@ss.hasPermi('audit:view')")
    public R<?> history(@RequestParam String bizType, @RequestParam Long bizId) {
        return R.ok(auditService.getAuditHistory(bizType, bizId));
    }
}
