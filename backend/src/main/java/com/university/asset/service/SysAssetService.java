package com.university.asset.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.common.annotation.DataScope;
import com.university.asset.common.annotation.Log;
import com.university.asset.common.exception.ServiceException;
import com.university.asset.dto.AssetQueryDTO;
import com.university.asset.dto.AssetSaveDTO;
import com.university.asset.dto.BorrowApplyDTO;
import com.university.asset.entity.AssetBorrow;
import com.university.asset.entity.SysAsset;
import com.university.asset.mapper.AssetBorrowMapper;
import com.university.asset.mapper.SysAssetMapper;
import com.university.asset.security.LoginUser;
import com.university.asset.security.service.PermissionService;
import com.university.asset.utils.QrCodeUtils;
import com.university.asset.vo.AssetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysAssetService {

    private final SysAssetMapper assetMapper;
    private final AssetBorrowMapper borrowMapper;
    private final QrCodeUtils qrCodeUtils;

    @DataScope(deptAlias = "a")
    public IPage<AssetVO> getAssetPage(AssetQueryDTO query) {
        Page<AssetVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return assetMapper.selectAssetPage(page, query);
    }

    public AssetVO getAssetDetail(Long id) {
        AssetVO vo = assetMapper.selectAssetVOById(id);
        if (vo == null) throw new ServiceException("资产不存在");
        return vo;
    }

    public AssetVO getAssetByCode(String assetCode) {
        SysAsset asset = assetMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysAsset>()
                        .eq(SysAsset::getAssetCode, assetCode)
                        .eq(SysAsset::getDeleted, 0)
        );
        if (asset == null) throw new ServiceException("资产编码不存在: " + assetCode);
        return assetMapper.selectAssetVOById(asset.getId());
    }

    @Log("新增/编辑资产")
    @Transactional
    public SysAsset saveAsset(AssetSaveDTO dto) {
        SysAsset asset;
        if (dto.getId() == null) {
            asset = new SysAsset();
            String code = generateAssetCode();
            asset.setAssetCode(code);
            asset.setStatus("IDLE");
            BeanUtils.copyProperties(dto, asset, "id");
            assetMapper.insert(asset);
            String qrContent = "ASSET:" + asset.getAssetCode();
            String qrBase64 = qrCodeUtils.generateQrCodeBase64(qrContent, 300, 300);
            asset.setQrCodeUrl(qrBase64);
            assetMapper.updateById(asset);
        } else {
            asset = assetMapper.selectById(dto.getId());
            if (asset == null) throw new ServiceException("资产不存在");
            BeanUtils.copyProperties(dto, asset, "id", "assetCode", "status", "qrCodeUrl");
            assetMapper.updateById(asset);
        }
        return asset;
    }

    @Log("删除资产")
    public void deleteAsset(Long id) {
        SysAsset asset = assetMapper.selectById(id);
        if (asset == null) throw new ServiceException("资产不存在");
        if ("IN_USE".equals(asset.getStatus())) throw new ServiceException("资产在用中，不能删除");
        assetMapper.deleteById(id);
    }

    @Log("发起借用申请")
    @Transactional
    public void applyBorrow(BorrowApplyDTO dto) {
        LoginUser loginUser = PermissionService.getLoginUser();
        SysAsset asset = assetMapper.selectById(dto.getAssetId());
        if (asset == null) throw new ServiceException("资产不存在");
        if (!"IDLE".equals(asset.getStatus())) throw new ServiceException("资产当前状态不可借用，状态: " + asset.getStatus());

        AssetBorrow borrow = new AssetBorrow();
        borrow.setBorrowNo("BRW-" + DateUtil.format(java.util.Date.from(
                LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant()), "yyyyMMddHHmmssSSS"));
        borrow.setAssetId(dto.getAssetId());
        borrow.setAssetCode(asset.getAssetCode());
        borrow.setAssetName(asset.getAssetName());
        borrow.setApplicantId(loginUser.getUserId());
        borrow.setDeptId(loginUser.getDeptId());
        borrow.setPurpose(dto.getPurpose());
        borrow.setExpectedReturnTime(dto.getExpectedReturnTime());
        borrow.setStatus("PENDING");
        borrowMapper.insert(borrow);
    }

    @Log("归还资产")
    @Transactional
    public void returnAsset(Long borrowId) {
        AssetBorrow borrow = borrowMapper.selectById(borrowId);
        if (borrow == null) throw new ServiceException("借用记录不存在");
        if (!"BORROWED".equals(borrow.getStatus())) throw new ServiceException("借用单状态不正确");

        borrow.setStatus("RETURNED");
        borrow.setActualReturnTime(LocalDateTime.now());
        borrowMapper.updateById(borrow);

        SysAsset asset = assetMapper.selectById(borrow.getAssetId());
        asset.setStatus("IDLE");
        assetMapper.updateById(asset);
    }

    public List<Map<String, Object>> getStatusStatistics() {
        return assetMapper.selectStatusStatistics();
    }

    public List<Map<String, Object>> getCategoryStatistics() {
        return assetMapper.selectCategoryStatistics();
    }

    private String generateAssetCode() {
        String prefix = "AST-" + DateUtil.thisYear() + "-";
        long count = assetMapper.selectCount(null) + 1;
        return prefix + String.format("%06d", count);
    }
}
