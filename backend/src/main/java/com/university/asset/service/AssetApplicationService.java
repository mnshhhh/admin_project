package com.university.asset.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.common.annotation.Log;
import com.university.asset.common.exception.ServiceException;
import com.university.asset.dto.AssetApplicationDTO;
import com.university.asset.dto.BaseQueryDTO;
import com.university.asset.dto.PurchaseOrderDTO;
import com.university.asset.dto.WarehouseEntryDTO;
import com.university.asset.entity.AssetApplication;
import com.university.asset.entity.AuditRecord;
import com.university.asset.entity.PurchaseOrder;
import com.university.asset.entity.WarehouseEntry;
import com.university.asset.mapper.AssetApplicationMapper;
import com.university.asset.mapper.AuditRecordMapper;
import com.university.asset.mapper.PurchaseOrderMapper;
import com.university.asset.mapper.WarehouseEntryMapper;
import com.university.asset.security.LoginUser;
import com.university.asset.security.service.PermissionService;
import com.university.asset.vo.AssetApplicationVO;
import com.university.asset.vo.PurchaseOrderVO;
import com.university.asset.vo.WarehouseEntryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssetApplicationService {

    private final AssetApplicationMapper applicationMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final WarehouseEntryMapper warehouseEntryMapper;
    private final AuditRecordMapper auditRecordMapper;

    @Log("提交资产申请")
    @Transactional
    public void applyAsset(AssetApplicationDTO dto) {
        LoginUser loginUser = PermissionService.getLoginUser();
        AssetApplication application = new AssetApplication();
        BeanUtils.copyProperties(dto, application);
        application.setApplyNo("APP-" + DateUtil.format(new java.util.Date(), "yyyyMMddHHmmssSSS"));
        application.setApplicantId(loginUser.getUserId());
        application.setDeptId(loginUser.getDeptId());
        application.setStatus("PENDING");
        applicationMapper.insert(application);
    }

    public IPage<AssetApplicationVO> getMyApplications(BaseQueryDTO query, String status) {
        LoginUser loginUser = PermissionService.getLoginUser();
        Page<AssetApplicationVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return applicationMapper.selectApplicationPage(page, query, loginUser.getUserId(), status);
    }

    public IPage<AssetApplicationVO> getPendingApplications(BaseQueryDTO query) {
        LoginUser loginUser = PermissionService.getLoginUser();
        Page<AssetApplicationVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return applicationMapper.selectPendingForAuditor(page, loginUser.getDeptId());
    }

    public AssetApplicationVO getApplicationDetail(Long id) {
        AssetApplicationVO vo = applicationMapper.selectApplicationVOById(id);
        if (vo == null) throw new ServiceException("申请单不存在");
        return vo;
    }

    @Log("审批资产申请")
    @Transactional
    public void approveApplication(Long id, String action, String remark) {
        LoginUser loginUser = PermissionService.getLoginUser();
        AssetApplication application = applicationMapper.selectById(id);
        if (application == null) throw new ServiceException("申请单不存在");
        if (!"PENDING".equals(application.getStatus())) throw new ServiceException("申请单状态不可审批");

        boolean approved = "APPROVE".equals(action);
        application.setApproverId(loginUser.getUserId());
        application.setApproveTime(LocalDateTime.now());
        application.setApproveRemark(remark);
        application.setStatus(approved ? "APPROVED" : "REJECTED");
        applicationMapper.updateById(application);

        AuditRecord record = new AuditRecord();
        record.setBizType("APPLICATION");
        record.setBizId(id);
        record.setAuditorId(loginUser.getUserId());
        record.setAction(action);
        record.setRemark(remark);
        record.setCreateTime(LocalDateTime.now());
        auditRecordMapper.insert(record);
    }

    @Log("生成采购单")
    @Transactional
    public void createPurchaseOrder(PurchaseOrderDTO dto) {
        AssetApplication application = applicationMapper.selectById(dto.getApplicationId());
        if (application == null) throw new ServiceException("申请单不存在");
        if (!"APPROVED".equals(application.getStatus())) throw new ServiceException("申请单未审批通过，不能生成采购单");

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo("PO-" + DateUtil.format(new java.util.Date(), "yyyyMMddHHmmssSSS"));
        order.setApplicationId(dto.getApplicationId());
        order.setExternalPartner(dto.getExternalPartner());
        order.setContactInfo(dto.getContactInfo());
        order.setTotalAmount(dto.getTotalAmount());
        order.setExpectedDate(dto.getExpectedDate());
        order.setRemark(dto.getRemark());
        order.setStatus("PENDING");
        purchaseOrderMapper.insert(order);

        application.setStatus("PROCUREMENT");
        applicationMapper.updateById(application);
    }

    @Log("更新采购单")
    @Transactional
    public void updatePurchaseOrder(PurchaseOrderDTO dto) {
        PurchaseOrder order = purchaseOrderMapper.selectById(dto.getId());
        if (order == null) throw new ServiceException("采购单不存在");

        if (dto.getExternalPartner() != null) order.setExternalPartner(dto.getExternalPartner());
        if (dto.getContactInfo() != null) order.setContactInfo(dto.getContactInfo());
        if (dto.getTotalAmount() != null) order.setTotalAmount(dto.getTotalAmount());
        if (dto.getExpectedDate() != null) order.setExpectedDate(dto.getExpectedDate());
        if (dto.getActualDate() != null) order.setActualDate(dto.getActualDate());
        if (dto.getRemark() != null) order.setRemark(dto.getRemark());
        order.setStatus("ORDERED");
        purchaseOrderMapper.updateById(order);
    }

    public IPage<PurchaseOrderVO> getPurchaseOrders(BaseQueryDTO query, String status) {
        Page<PurchaseOrderVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return purchaseOrderMapper.selectPurchaseOrderPage(page, status);
    }

    public PurchaseOrderVO getPurchaseOrderDetail(Long id) {
        PurchaseOrderVO vo = purchaseOrderMapper.selectPurchaseOrderVOById(id);
        if (vo == null) throw new ServiceException("采购单不存在");
        return vo;
    }

    @Log("提交入库单")
    @Transactional
    public void submitWarehouseEntry(WarehouseEntryDTO dto) {
        PurchaseOrder order = purchaseOrderMapper.selectById(dto.getPurchaseOrderId());
        if (order == null) throw new ServiceException("采购单不存在");

        WarehouseEntry entry = new WarehouseEntry();
        entry.setEntryNo("WE-" + DateUtil.format(new java.util.Date(), "yyyyMMddHHmmssSSS"));
        entry.setPurchaseOrderId(dto.getPurchaseOrderId());
        entry.setApplicationId(order.getApplicationId());
        entry.setAssetName(dto.getAssetName());
        entry.setSpecification(dto.getSpecification());
        entry.setQuantity(dto.getQuantity());
        entry.setUnitPrice(dto.getUnitPrice());
        entry.setTotalAmount(dto.getTotalAmount());
        entry.setEntryDate(dto.getEntryDate());
        entry.setRemark(dto.getRemark());
        entry.setStatus("PENDING");
        warehouseEntryMapper.insert(entry);

        order.setStatus("DELIVERED");
        purchaseOrderMapper.updateById(order);
    }

    public IPage<WarehouseEntryVO> getWarehouseEntries(BaseQueryDTO query, String status) {
        Page<WarehouseEntryVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return warehouseEntryMapper.selectWarehouseEntryPage(page, status);
    }

    public WarehouseEntryVO getWarehouseEntryDetail(Long id) {
        WarehouseEntryVO vo = warehouseEntryMapper.selectWarehouseEntryVOById(id);
        if (vo == null) throw new ServiceException("入库单不存在");
        return vo;
    }

    @Log("审批入库单")
    @Transactional
    public void approveWarehouseEntry(Long id, String action, String remark) {
        LoginUser loginUser = PermissionService.getLoginUser();
        WarehouseEntry entry = warehouseEntryMapper.selectById(id);
        if (entry == null) throw new ServiceException("入库单不存在");
        if (!"PENDING".equals(entry.getStatus())) throw new ServiceException("入库单状态不可审批");

        boolean approved = "APPROVE".equals(action);
        entry.setApproverId(loginUser.getUserId());
        entry.setApproveTime(LocalDateTime.now());
        entry.setApproveRemark(remark);
        entry.setStatus(approved ? "APPROVED" : "REJECTED");
        warehouseEntryMapper.updateById(entry);

        AuditRecord record = new AuditRecord();
        record.setBizType("WAREHOUSE_ENTRY");
        record.setBizId(id);
        record.setAuditorId(loginUser.getUserId());
        record.setAction(action);
        record.setRemark(remark);
        record.setCreateTime(LocalDateTime.now());
        auditRecordMapper.insert(record);

        if (approved) {
            PurchaseOrder order = purchaseOrderMapper.selectById(entry.getPurchaseOrderId());
            if (order != null) {
                order.setStatus("COMPLETED");
                purchaseOrderMapper.updateById(order);
            }

            AssetApplication application = applicationMapper.selectById(entry.getApplicationId());
            if (application != null) {
                application.setStatus("COMPLETED");
                applicationMapper.updateById(application);
            }
        }
    }
}
