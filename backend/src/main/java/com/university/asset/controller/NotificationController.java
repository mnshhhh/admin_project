package com.university.asset.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.asset.common.result.PageResult;
import com.university.asset.common.result.R;
import com.university.asset.dto.BaseQueryDTO;
import com.university.asset.entity.SysNotification;
import com.university.asset.mapper.SysNotificationMapper;
import com.university.asset.security.LoginUser;
import com.university.asset.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final SysNotificationMapper notificationMapper;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('notification:list')")
    public R<PageResult<SysNotification>> list(BaseQueryDTO query,
                                                @RequestParam(required = false) Integer isRead) {
        LoginUser loginUser = PermissionService.getLoginUser();
        Page<SysNotification> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<SysNotification> result = notificationMapper.selectNotificationPage(page, loginUser.getUserId(), isRead);
        return R.ok(PageResult.of(result));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("@ss.hasPermi('notification:list')")
    public R<Long> unreadCount() {
        LoginUser loginUser = PermissionService.getLoginUser();
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, loginUser.getUserId())
               .eq(SysNotification::getIsRead, 0);
        Long count = notificationMapper.selectCount(wrapper);
        return R.ok(count);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("@ss.hasPermi('notification:list')")
    public R<Void> markAsRead(@PathVariable Long id) {
        SysNotification notification = notificationMapper.selectById(id);
        if (notification != null && notification.getUserId().equals(PermissionService.getLoginUser().getUserId())) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
        return R.ok();
    }

    @PutMapping("/read-all")
    @PreAuthorize("@ss.hasPermi('notification:list')")
    public R<Void> markAllAsRead() {
        LoginUser loginUser = PermissionService.getLoginUser();
        SysNotification update = new SysNotification();
        update.setIsRead(1);
        update.setReadTime(LocalDateTime.now());
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, loginUser.getUserId())
               .eq(SysNotification::getIsRead, 0);
        notificationMapper.update(update, wrapper);
        return R.ok();
    }
}
