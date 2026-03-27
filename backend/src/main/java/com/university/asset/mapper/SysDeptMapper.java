package com.university.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.asset.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {
    List<SysDept> selectDeptList(@Param("status") String status);
    List<SysDept> selectChildrenById(@Param("deptId") Long deptId);
}
