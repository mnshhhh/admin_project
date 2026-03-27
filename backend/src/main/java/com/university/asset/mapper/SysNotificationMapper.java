package com.university.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.asset.entity.SysNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysNotificationMapper extends BaseMapper<SysNotification> {
    IPage<SysNotification> selectNotificationPage(Page<SysNotification> page, @Param("userId") Long userId, @Param("isRead") Integer isRead);
}
