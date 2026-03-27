package com.university.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.asset.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
    void deleteRoleMenuByRoleId(@Param("roleId") Long roleId);
    void insertRoleMenuBatch(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
    void deleteUserRoleByUserId(@Param("userId") Long userId);
    void insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
