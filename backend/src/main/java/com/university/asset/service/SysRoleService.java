package com.university.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.asset.common.exception.ServiceException;
import com.university.asset.entity.SysRole;
import com.university.asset.mapper.SysMenuMapper;
import com.university.asset.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    public List<SysRole> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().eq(SysRole::getDeleted, 0));
    }

    @Transactional
    public void saveRole(SysRole role, List<Long> menuIds) {
        if (role.getRoleId() == null) {
            roleMapper.insert(role);
        } else {
            roleMapper.updateById(role);
        }
        roleMapper.deleteRoleMenuByRoleId(role.getRoleId());
        if (!CollectionUtils.isEmpty(menuIds)) {
            roleMapper.insertRoleMenuBatch(role.getRoleId(), menuIds);
        }
    }

    public void deleteRole(Long roleId) {
        roleMapper.deleteById(roleId);
        roleMapper.deleteRoleMenuByRoleId(roleId);
    }

    public List<Long> getRoleMenuIds(Long roleId) {
        return menuMapper.selectMenuIdsByRoleId(roleId);
    }

    public List<Long> getUserRoleIds(Long userId) {
        return roleMapper.selectRoleIdsByUserId(userId);
    }
}
