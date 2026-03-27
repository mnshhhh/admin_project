package com.university.asset.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.common.annotation.Log;
import com.university.asset.common.exception.ServiceException;
import com.university.asset.dto.AuditActionDTO;
import com.university.asset.dto.BaseQueryDTO;
import com.university.asset.entity.*;
import com.university.asset.mapper.*;
import com.university.asset.security.LoginUser;
import com.university.asset.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AssetBorrowMapper borrowMapper;
    private final AssetAllocateMapper allocateMapper;
    private final SysAssetMapper assetMapper;
    private final AuditRecordMapper auditRecordMapper;

    public IPage<AssetBorrow> getPendingBorrows(BaseQueryDTO query) {
        LoginUser loginUser = PermissionService.getLoginUser();
        Page<AssetBorrow> page = new Page<>(query.getPageNum(), query.getPageSize());
        return borrowMapper.selectPendingForAuditor(page, loginUser.getDeptId());
    }

    public IPage<AssetBorrow> getMyBorrows(BaseQueryDTO query, String status) {
        LoginUser loginUser = PermissionService.getLoginUser();
        Page<AssetBorrow> page = new Page<>(query.getPageNum(), query.getPageSize());
        return borrowMapper.selectBorrowPage(page, query, loginUser.getUserId(), status);
    }

    @Log("审批操作")
    @Transactional
    public void audit(AuditActionDTO dto) {
        LoginUser loginUser = PermissionService.getLoginUser();
        boolean approved = "APPROVE".equals(dto.getAction());

        switch (dto.getBizType()) {
            case "BORROW" -> auditBorrow(dto.getBizId(), approved, dto.getRemark(), loginUser);
            case "ALLOCATE" -> auditAllocate(dto.getBizId(), approved, dto.getRemark(), loginUser);
            default -> throw new ServiceException("不支持的业务类型: " + dto.getBizType());
        }

        AuditRecord record = new AuditRecord();
        record.setBizType(dto.getBizType());
        record.setBizId(dto.getBizId());
        record.setAuditorId(loginUser.getUserId());
        record.setAction(dto.getAction());
        record.setRemark(dto.getRemark());
        record.setCreateTime(LocalDateTime.now());
        auditRecordMapper.insert(record);
    }

    private void auditBorrow(Long borrowId, boolean approved, String remark, LoginUser loginUser) {
        AssetBorrow borrow = borrowMapper.selectById(borrowId);
        if (borrow == null) throw new ServiceException("借用单不存在");
        if (!"PENDING".equals(borrow.getStatus())) throw new ServiceException("借用单状态不可审批");

        borrow.setApproverId(loginUser.getUserId());
        borrow.setApproveTime(LocalDateTime.now());
        borrow.setApproveRemark(remark);

        if (approved) {
            borrow.setStatus("BORROWED");
            borrow.setBorrowTime(LocalDateTime.now());
            SysAsset asset = assetMapper.selectById(borrow.getAssetId());
            if (asset != null) {
                asset.setStatus("IN_USE");
                assetMapper.updateById(asset);
            }
        } else {
            borrow.setStatus("REJECTED");
        }
        borrowMapper.updateById(borrow);
    }

    private void auditAllocate(Long allocateId, boolean approved, String remark, LoginUser loginUser) {
        AssetAllocate allocate = allocateMapper.selectById(allocateId);
        if (allocate == null) throw new ServiceException("调拨单不存在");

        if ("PENDING".equals(allocate.getStatus())) {
            if (approved) {
                allocate.setStatus("FROM_CONFIRMED");
                allocate.setFromConfirmId(loginUser.getUserId());
                allocate.setFromConfirmTime(LocalDateTime.now());
            } else {
                allocate.setStatus("REJECTED");
            }
        } else if ("FROM_CONFIRMED".equals(allocate.getStatus())) {
            if (approved) {
                allocate.setStatus("COMPLETED");
                allocate.setToConfirmId(loginUser.getUserId());
                allocate.setToConfirmTime(LocalDateTime.now());
                SysAsset asset = assetMapper.selectById(allocate.getAssetId());
                if (asset != null) {
                    asset.setDeptId(allocate.getToDeptId());
                    assetMapper.updateById(asset);
                }
            } else {
                allocate.setStatus("REJECTED");
            }
        }
        allocateMapper.updateById(allocate);
    }

    public List<AuditRecord> getAuditHistory(String bizType, Long bizId) {
        return auditRecordMapper.selectByBiz(bizType, bizId);
    }
}
