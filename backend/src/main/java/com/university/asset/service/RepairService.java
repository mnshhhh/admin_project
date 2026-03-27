package com.university.asset.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.common.annotation.Log;
import com.university.asset.common.exception.ServiceException;
import com.university.asset.dto.RepairApplyDTO;
import com.university.asset.entity.AssetRepair;
import com.university.asset.entity.SysAsset;
import com.university.asset.mapper.AssetRepairMapper;
import com.university.asset.mapper.SysAssetMapper;
import com.university.asset.security.LoginUser;
import com.university.asset.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final AssetRepairMapper repairMapper;
    private final SysAssetMapper assetMapper;

    @Log("提交报修申请")
    @Transactional
    public void applyRepair(RepairApplyDTO dto) {
        LoginUser loginUser = PermissionService.getLoginUser();
        SysAsset asset = assetMapper.selectById(dto.getAssetId());
        if (asset == null) throw new ServiceException("资产不存在");

        AssetRepair repair = new AssetRepair();
        repair.setRepairNo("REP-" + DateUtil.format(java.util.Date.from(
                LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant()), "yyyyMMddHHmmssSSS"));
        repair.setAssetId(dto.getAssetId());
        repair.setAssetCode(asset.getAssetCode());
        repair.setAssetName(asset.getAssetName());
        repair.setReporterId(loginUser.getUserId());
        repair.setDeptId(loginUser.getDeptId());
        repair.setDescription(dto.getDescription());
        repair.setImages(dto.getImages());
        repair.setStatus("PENDING");
        repairMapper.insert(repair);

        asset.setStatus("REPAIR");
        assetMapper.updateById(asset);
    }

    public IPage<AssetRepair> getRepairPage(int pageNum, int pageSize, String status, boolean onlyMine) {
        Page<AssetRepair> page = new Page<>(pageNum, pageSize);
        LoginUser loginUser = PermissionService.getLoginUser();
        Long repairManId = onlyMine ? loginUser.getUserId() : null;
        return repairMapper.selectRepairPage(page, status, repairManId);
    }

    @Log("接单维修")
    @Transactional
    public void acceptRepair(Long repairId) {
        LoginUser loginUser = PermissionService.getLoginUser();
        AssetRepair repair = repairMapper.selectById(repairId);
        if (repair == null) throw new ServiceException("工单不存在");
        if (!"PENDING".equals(repair.getStatus())) throw new ServiceException("工单已被处理");
        repair.setRepairManId(loginUser.getUserId());
        repair.setStatus("IN_PROGRESS");
        repairMapper.updateById(repair);
    }

    @Log("完成维修")
    @Transactional
    public void completeRepair(Long repairId, BigDecimal cost, BigDecimal hours, String remark) {
        AssetRepair repair = repairMapper.selectById(repairId);
        if (repair == null) throw new ServiceException("工单不存在");
        repair.setStatus("COMPLETED");
        repair.setRepairCost(cost);
        repair.setRepairHours(hours);
        repair.setRepairRemark(remark);
        repair.setCompleteTime(LocalDateTime.now());
        repairMapper.updateById(repair);

        SysAsset asset = assetMapper.selectById(repair.getAssetId());
        if (asset != null) {
            asset.setStatus("IDLE");
            assetMapper.updateById(asset);
        }
    }
}
