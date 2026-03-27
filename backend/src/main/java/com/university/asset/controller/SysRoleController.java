package com.university.asset.controller;

import com.university.asset.common.annotation.Log;
import com.university.asset.common.result.R;
import com.university.asset.entity.SysRole;
import com.university.asset.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('sys:role:manage')")
    public R<List<SysRole>> list() {
        return R.ok(roleService.listAll());
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('sys:role:manage')")
    @Log("新增角色")
    public R<Void> add(@RequestBody Map<String, Object> body) {
        SysRole role = parseRole(body);
        List<Long> menuIds = parseMenuIds(body);
        roleService.saveRole(role, menuIds);
        return R.ok();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('sys:role:manage')")
    @Log("编辑角色")
    public R<Void> edit(@RequestBody Map<String, Object> body) {
        SysRole role = parseRole(body);
        List<Long> menuIds = parseMenuIds(body);
        roleService.saveRole(role, menuIds);
        return R.ok();
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("@ss.hasPermi('sys:role:manage')")
    @Log("删除角色")
    public R<Void> delete(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return R.ok();
    }

    @GetMapping("/{roleId}/menuIds")
    @PreAuthorize("@ss.hasPermi('sys:role:manage')")
    public R<List<Long>> getMenuIds(@PathVariable Long roleId) {
        return R.ok(roleService.getRoleMenuIds(roleId));
    }

    @SuppressWarnings("unchecked")
    private List<Long> parseMenuIds(Map<String, Object> body) {
        Object menuIds = body.get("menuIds");
        if (menuIds instanceof List) {
            return ((List<?>) menuIds).stream().map(id -> Long.valueOf(id.toString())).collect(java.util.stream.Collectors.toList());
        }
        return List.of();
    }

    private SysRole parseRole(Map<String, Object> body) {
        SysRole role = new SysRole();
        if (body.get("roleId") != null) role.setRoleId(Long.valueOf(body.get("roleId").toString()));
        if (body.get("roleName") != null) role.setRoleName(body.get("roleName").toString());
        if (body.get("roleKey") != null) role.setRoleKey(body.get("roleKey").toString());
        if (body.get("dataScope") != null) role.setDataScope(body.get("dataScope").toString());
        if (body.get("remark") != null) role.setRemark(body.get("remark").toString());
        return role;
    }
}
