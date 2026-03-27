package com.university.asset.service;

import com.university.asset.common.exception.ServiceException;
import com.university.asset.entity.SysDept;
import com.university.asset.mapper.SysDeptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDeptService {

    private final SysDeptMapper deptMapper;

    public List<SysDept> getDeptTree() {
        List<SysDept> list = deptMapper.selectDeptList("0");
        return buildTree(list, 0L);
    }

    private List<SysDept> buildTree(List<SysDept> all, Long parentId) {
        List<SysDept> result = new ArrayList<>();
        for (SysDept dept : all) {
            if (dept.getParentId().equals(parentId)) {
                dept.setChildren(buildTree(all, dept.getDeptId()));
                result.add(dept);
            }
        }
        return result;
    }

    @Transactional
    public void saveDept(SysDept dept) {
        if (dept.getDeptId() == null) {
            String ancestors = buildAncestors(dept.getParentId());
            dept.setAncestors(ancestors);
            deptMapper.insert(dept);
        } else {
            deptMapper.updateById(dept);
        }
    }

    private String buildAncestors(Long parentId) {
        if (parentId == 0) return "0";
        SysDept parent = deptMapper.selectById(parentId);
        if (parent == null) throw new ServiceException("父部门不存在");
        return parent.getAncestors() + "," + parentId;
    }

    @Transactional
    public void deleteDept(Long deptId) {
        List<SysDept> children = deptMapper.selectChildrenById(deptId);
        if (children.size() > 1) throw new ServiceException("存在下级部门，不能删除");
        deptMapper.deleteById(deptId);
    }
}
