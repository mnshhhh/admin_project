package com.university.asset.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.asset.common.result.R;
import com.university.asset.entity.SysMenu;
import com.university.asset.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/sys/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuMapper menuMapper;

    @GetMapping("/tree")
    @PreAuthorize("@ss.hasPermi('sys:role:manage')")
    public R<List<SysMenu>> tree() {
        List<SysMenu> all = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, "0").orderByAsc(SysMenu::getOrderNum));
        return R.ok(buildTree(all, 0L));
    }

    private List<SysMenu> buildTree(List<SysMenu> all, Long parentId) {
        List<SysMenu> result = new ArrayList<>();
        for (SysMenu menu : all) {
            if (menu.getParentId().equals(parentId)) {
                menu.setChildren(buildTree(all, menu.getMenuId()));
                result.add(menu);
            }
        }
        return result;
    }
}
