package com.university.asset.common.aspect;

import com.university.asset.common.annotation.Log;
import com.university.asset.entity.SysLog;
import com.university.asset.mapper.SysLogMapper;
import com.university.asset.security.LoginUser;
import com.university.asset.security.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final SysLogMapper logMapper;

    @Around("@annotation(log)")
    public Object around(ProceedingJoinPoint joinPoint, Log log) throws Throwable {
        SysLog sysLog = new SysLog();
        LoginUser loginUser = PermissionService.getLoginUser();
        if (loginUser != null) {
            sysLog.setUserId(loginUser.getUserId());
            sysLog.setUsername(loginUser.getUsername());
        }
        sysLog.setOperation(log.value());
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        sysLog.setMethod(sig.getDeclaringTypeName() + "." + sig.getName());

        HttpServletRequest request = null;
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            request = attrs.getRequest();
            sysLog.setRequestUrl(request.getRequestURI());
            sysLog.setRequestIp(getIp(request));
        }

        sysLog.setCreateTime(LocalDateTime.now());
        if (sysLog.getUserId() == null) {
            sysLog.setUserId(0L);
            sysLog.setUsername("anonymous");
        }
        if (sysLog.getRequestUrl() == null) {
            sysLog.setRequestUrl("-");
        }

        try {
            Object result = joinPoint.proceed();
            sysLog.setStatus(0);
            return result;
        } catch (Throwable e) {
            sysLog.setStatus(1);
            sysLog.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            try { logMapper.insert(sysLog); } catch (Exception ignored) {}
        }
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
