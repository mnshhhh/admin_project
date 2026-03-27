package com.university.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.dto.BaseQueryDTO;
import com.university.asset.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    SysUser selectByUsername(@Param("username") String username);
    IPage<SysUser> selectUserPage(Page<SysUser> page, @Param("query") BaseQueryDTO query, @Param("keyword") String keyword, @Param("deptId") Long deptId, @Param("status") String status);
}
