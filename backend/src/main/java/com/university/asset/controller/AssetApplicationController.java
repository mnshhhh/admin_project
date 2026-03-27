package com.university.asset.controller;

import com.university.asset.common.annotation.Log;
import com.university.asset.common.result.PageResult;
import com.university.asset.common.result.R;
import com.university.asset.dto.AssetApplicationDTO;
import com.university.asset.dto.BaseQueryDTO;
import com.university.asset.dto.PurchaseOrderDTO;
import com.university.asset.dto.WarehouseEntryDTO;
import com.university.asset.service.AssetApplicationService;
import com.university.asset.vo.AssetApplicationVO;
import com.university.asset.vo.PurchaseOrderVO;
import com.university.asset.vo.WarehouseEntryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/asset/application")
@RequiredArgsConstructor
public class AssetApplicationController {

    private final AssetApplicationService applicationService;

    @PostMapping
    @PreAuthorize("@ss.hasPermi('asset:apply')")
    @Log("提交资产申请")
    public R<Void> apply(@Valid @RequestBody AssetApplicationDTO dto) {
        applicationService.applyAsset(dto);
        return R.ok();
    }

    @GetMapping("/my")
    @PreAuthorize("@ss.hasPermi('asset:apply')")
    public R<PageResult<AssetApplicationVO>> myApplications(BaseQueryDTO query,
                                                             @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(applicationService.getMyApplications(query, status)));
    }

    @GetMapping("/pending")
    @PreAuthorize("@ss.hasPermi('asset:apply:approve')")
    public R<PageResult<AssetApplicationVO>> pending(BaseQueryDTO query) {
        return R.ok(PageResult.of(applicationService.getPendingApplications(query)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('asset:apply')")
    public R<AssetApplicationVO> detail(@PathVariable Long id) {
        return R.ok(applicationService.getApplicationDetail(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("@ss.hasPermi('asset:apply:approve')")
    @Log("审批资产申请")
    public R<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        applicationService.approveApplication(id, body.get("action"), body.get("remark"));
        return R.ok();
    }

    @PostMapping("/purchase-order")
    @PreAuthorize("@ss.hasPermi('asset:po:manage')")
    @Log("生成采购单")
    public R<Void> createPurchaseOrder(@Valid @RequestBody PurchaseOrderDTO dto) {
        applicationService.createPurchaseOrder(dto);
        return R.ok();
    }

    @PutMapping("/purchase-order")
    @PreAuthorize("@ss.hasPermi('asset:po:manage')")
    @Log("更新采购单")
    public R<Void> updatePurchaseOrder(@Valid @RequestBody PurchaseOrderDTO dto) {
        applicationService.updatePurchaseOrder(dto);
        return R.ok();
    }

    @GetMapping("/purchase-order/list")
    @PreAuthorize("@ss.hasPermi('asset:po:manage')")
    public R<PageResult<PurchaseOrderVO>> purchaseOrders(BaseQueryDTO query,
                                                          @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(applicationService.getPurchaseOrders(query, status)));
    }

    @GetMapping("/purchase-order/{id}")
    @PreAuthorize("@ss.hasPermi('asset:po:manage')")
    public R<PurchaseOrderVO> purchaseOrderDetail(@PathVariable Long id) {
        return R.ok(applicationService.getPurchaseOrderDetail(id));
    }

    @PostMapping("/warehouse-entry")
    @PreAuthorize("@ss.hasPermi('asset:entry:manage')")
    @Log("提交入库单")
    public R<Void> submitWarehouseEntry(@Valid @RequestBody WarehouseEntryDTO dto) {
        applicationService.submitWarehouseEntry(dto);
        return R.ok();
    }

    @GetMapping("/warehouse-entry/list")
    @PreAuthorize("@ss.hasPermi('asset:entry:manage')")
    public R<PageResult<WarehouseEntryVO>> warehouseEntries(BaseQueryDTO query,
                                                             @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(applicationService.getWarehouseEntries(query, status)));
    }

    @GetMapping("/warehouse-entry/{id}")
    @PreAuthorize("@ss.hasPermi('asset:entry:manage')")
    public R<WarehouseEntryVO> warehouseEntryDetail(@PathVariable Long id) {
        return R.ok(applicationService.getWarehouseEntryDetail(id));
    }

    @PostMapping("/warehouse-entry/{id}/approve")
    @PreAuthorize("@ss.hasPermi('asset:entry:approve')")
    @Log("审批入库单")
    public R<Void> approveWarehouseEntry(@PathVariable Long id, @RequestBody Map<String, String> body) {
        applicationService.approveWarehouseEntry(id, body.get("action"), body.get("remark"));
        return R.ok();
    }
}
