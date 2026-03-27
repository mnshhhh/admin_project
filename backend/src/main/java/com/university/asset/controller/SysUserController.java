package com.university.asset.controller;

import com.university.asset.common.annotation.Log;
import com.university.asset.common.result.PageResult;
import com.university.asset.common.result.R;
import com.university.asset.dto.BaseQueryDTO;
import com.university.asset.dto.UserSaveDTO;
import com.university.asset.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('sys:user:list')")
    public R<PageResult<?>> list(BaseQueryDTO query,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Long deptId,
                                 @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(userService.getUserPage(query, keyword, deptId, status)));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('sys:user:add')")
    @Log("新增用户")
    public R<Void> add(@Valid @RequestBody UserSaveDTO dto) {
        userService.saveUser(dto);
        return R.ok();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('sys:user:edit')")
    @Log("编辑用户")
    public R<Void> edit(@Valid @RequestBody UserSaveDTO dto) {
        userService.saveUser(dto);
        return R.ok();
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("@ss.hasPermi('sys:user:remove')")
    @Log("删除用户")
    public R<Void> delete(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return R.ok();
    }

    @PutMapping("/resetPwd")
    @PreAuthorize("@ss.hasPermi('sys:user:edit')")
    public R<Void> resetPwd(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String newPwd = body.get("password").toString();
        userService.resetPassword(userId, newPwd);
        return R.ok();
    }
}
