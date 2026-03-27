package com.university.asset.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.common.exception.ServiceException;
import com.university.asset.dto.BaseQueryDTO;
import com.university.asset.dto.UserSaveDTO;
import com.university.asset.entity.SysUser;
import com.university.asset.mapper.SysRoleMapper;
import com.university.asset.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public IPage<SysUser> getUserPage(BaseQueryDTO query, String keyword, Long deptId, String status) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        return userMapper.selectUserPage(page, query, keyword, deptId, status);
    }

    @Transactional
    public void saveUser(UserSaveDTO dto) {
        SysUser existing = userMapper.selectByUsername(dto.getUsername());
        if (dto.getUserId() == null) {
            if (existing != null) throw new ServiceException("用户名已存在");
            SysUser user = new SysUser();
            copyDtoToUser(dto, user);
            user.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
            userMapper.insert(user);
            if (!CollectionUtils.isEmpty(dto.getRoleIds())) {
                roleMapper.insertUserRoles(user.getUserId(), dto.getRoleIds());
            }
        } else {
            SysUser user = userMapper.selectById(dto.getUserId());
            if (user == null) throw new ServiceException("用户不存在");
            copyDtoToUser(dto, user);
            userMapper.updateById(user);
            roleMapper.deleteUserRoleByUserId(dto.getUserId());
            if (!CollectionUtils.isEmpty(dto.getRoleIds())) {
                roleMapper.insertUserRoles(dto.getUserId(), dto.getRoleIds());
            }
        }
    }

    private void copyDtoToUser(UserSaveDTO dto, SysUser user) {
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setDeptId(dto.getDeptId());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
    }

    @Transactional
    public void deleteUser(Long userId) {
        userMapper.deleteById(userId);
        roleMapper.deleteUserRoleByUserId(userId);
    }

    public void resetPassword(Long userId, String newPassword) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
