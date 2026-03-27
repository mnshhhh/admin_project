package com.university.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.asset.common.annotation.Log;
import com.university.asset.common.exception.ServiceException;
import com.university.asset.entity.AssetCheckDetail;
import com.university.asset.entity.AssetCheckTask;
import com.university.asset.entity.SysAsset;
import com.university.asset.mapper.AssetCheckDetailMapper;
import com.university.asset.mapper.AssetCheckTaskMapper;
import com.university.asset.mapper.SysAssetMapper;
import com.university.asset.security.LoginUser;
import com.university.asset.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetCheckService {

    private final AssetCheckTaskMapper taskMapper;
    private final AssetCheckDetailMapper detailMapper;
    private final SysAssetMapper assetMapper;

    @Log("创建盘点任务")
    @Transactional
    public AssetCheckTask createTask(String taskName, Long deptId, String remark) {
        LoginUser loginUser = PermissionService.getLoginUser();
        AssetCheckTask task = new AssetCheckTask();
        task.setTaskNo("CHK-" + System.currentTimeMillis());
        task.setTaskName(taskName);
        task.setDeptId(deptId);
        task.setCreatorId(loginUser.getUserId());
        task.setStatus("DRAFT");
        task.setRemark(remark);
        taskMapper.insert(task);

        LambdaQueryWrapper<SysAsset> wrapper = new LambdaQueryWrapper<SysAsset>()
                .eq(SysAsset::getDeleted, 0);
        if (deptId != null) wrapper.eq(SysAsset::getDeptId, deptId);
        List<SysAsset> assets = assetMapper.selectList(wrapper);

        for (SysAsset asset : assets) {
            AssetCheckDetail detail = new AssetCheckDetail();
            detail.setTaskId(task.getId());
            detail.setAssetId(asset.getId());
            detail.setAssetCode(asset.getAssetCode());
            detail.setAssetName(asset.getAssetName());
            detail.setExpectedDeptId(asset.getDeptId());
            detail.setCreateTime(LocalDateTime.now());
            detailMapper.insert(detail);
        }
        return task;
    }

    @Log("扫码盘点")
    @Transactional
    public void scanCheck(Long taskId, String assetCode) {
        LoginUser loginUser = PermissionService.getLoginUser();
        List<AssetCheckDetail> details = detailMapper.selectByTaskId(taskId);
        AssetCheckDetail target = details.stream()
                .filter(d -> d.getAssetCode().equals(assetCode))
                .findFirst()
                .orElse(null);
        if (target != null) {
            target.setCheckResult("NORMAL");
            target.setCheckerId(loginUser.getUserId());
            target.setCheckTime(LocalDateTime.now());
            detailMapper.updateById(target);
        } else {
            AssetCheckTask task = taskMapper.selectById(taskId);
            SysAsset asset = assetMapper.selectOne(
                    new LambdaQueryWrapper<SysAsset>().eq(SysAsset::getAssetCode, assetCode).eq(SysAsset::getDeleted, 0));
            if (asset != null) {
                AssetCheckDetail surplus = new AssetCheckDetail();
                surplus.setTaskId(taskId);
                surplus.setAssetId(asset.getId());
                surplus.setAssetCode(assetCode);
                surplus.setAssetName(asset.getAssetName());
                surplus.setExpectedDeptId(asset.getDeptId());
                surplus.setCheckResult("SURPLUS");
                surplus.setCheckerId(loginUser.getUserId());
                surplus.setCheckTime(LocalDateTime.now());
                surplus.setCreateTime(LocalDateTime.now());
                detailMapper.insert(surplus);
            }
        }
    }

    @Log("完成盘点任务")
    @Transactional
    public void completeTask(Long taskId) {
        AssetCheckTask task = taskMapper.selectById(taskId);
        if (task == null) throw new ServiceException("盘点任务不存在");
        task.setStatus("COMPLETED");
        task.setEndTime(LocalDateTime.now());
        taskMapper.updateById(task);

        List<AssetCheckDetail> details = detailMapper.selectByTaskId(taskId);
        for (AssetCheckDetail detail : details) {
            if (detail.getCheckResult() == null) {
                detail.setCheckResult("DEFICIT");
                detailMapper.updateById(detail);
            }
        }
    }

    public List<AssetCheckDetail> getTaskDetails(Long taskId) {
        return detailMapper.selectByTaskId(taskId);
    }

    public List<AssetCheckTask> getAllTasks() {
        return taskMapper.selectList(new LambdaQueryWrapper<AssetCheckTask>()
                .eq(AssetCheckTask::getDeleted, 0).orderByDesc(AssetCheckTask::getCreateTime));
    }
}
