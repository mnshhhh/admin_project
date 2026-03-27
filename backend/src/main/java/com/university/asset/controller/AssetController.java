package com.university.asset.controller;

import com.university.asset.common.annotation.Log;
import com.university.asset.common.result.PageResult;
import com.university.asset.common.result.R;
import com.university.asset.dto.AssetQueryDTO;
import com.university.asset.dto.AssetSaveDTO;
import com.university.asset.dto.BorrowApplyDTO;
import com.university.asset.service.SysAssetService;
import com.university.asset.vo.AssetVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/asset")
@RequiredArgsConstructor
public class AssetController {

    private final SysAssetService assetService;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('asset:list')")
    public R<PageResult<AssetVO>> list(AssetQueryDTO query) {
        return R.ok(PageResult.of(assetService.getAssetPage(query)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('asset:list')")
    public R<AssetVO> detail(@PathVariable Long id) {
        return R.ok(assetService.getAssetDetail(id));
    }

    @GetMapping("/code/{assetCode}")
    @PreAuthorize("@ss.hasPermi('asset:list')")
    public R<AssetVO> getByCode(@PathVariable String assetCode) {
        return R.ok(assetService.getAssetByCode(assetCode));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('asset:add')")
    @Log("新增资产")
    public R<Void> add(@Valid @RequestBody AssetSaveDTO dto) {
        assetService.saveAsset(dto);
        return R.ok();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('asset:edit')")
    @Log("编辑资产")
    public R<Void> edit(@Valid @RequestBody AssetSaveDTO dto) {
        assetService.saveAsset(dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('asset:remove')")
    @Log("删除资产")
    public R<Void> delete(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return R.ok();
    }

    @PostMapping("/borrow")
    @PreAuthorize("@ss.hasPermi('asset:borrow')")
    public R<Void> borrow(@Valid @RequestBody BorrowApplyDTO dto) {
        assetService.applyBorrow(dto);
        return R.ok();
    }

    @PutMapping("/return/{borrowId}")
    @PreAuthorize("@ss.hasPermi('asset:return')")
    public R<Void> returnAsset(@PathVariable Long borrowId) {
        assetService.returnAsset(borrowId);
        return R.ok();
    }

    @GetMapping("/statistics/status")
    @PreAuthorize("@ss.hasPermi('report:asset')")
    public R<List<Map<String, Object>>> statusStats() {
        return R.ok(assetService.getStatusStatistics());
    }

    @GetMapping("/statistics/category")
    @PreAuthorize("@ss.hasPermi('report:asset')")
    public R<List<Map<String, Object>>> categoryStats() {
        return R.ok(assetService.getCategoryStatistics());
    }
}
