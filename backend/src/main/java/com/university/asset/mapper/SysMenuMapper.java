package com.university.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.asset.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    Set<String> selectPermsByUserId(@Param("userId") Long userId);
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
