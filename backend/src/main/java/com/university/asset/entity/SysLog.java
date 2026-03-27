package com.university.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class SysLog {
    @TableId(type = IdType.AUTO)
    private Long logId;
    private Long userId;
    private String username;
    private String operation;
    private String method;
    private String requestUrl;
    private String requestIp;
    private Integer status;
    private String errorMsg;
    private LocalDateTime createTime;
}
