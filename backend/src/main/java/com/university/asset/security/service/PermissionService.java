package com.university.asset.security.service;

import com.university.asset.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Set;

@Service("ss")
public class PermissionService {

    public boolean hasPermi(String permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser loginUser)) {
            return false;
        }
        Set<String> permissions = loginUser.getPermissions();
        if (CollectionUtils.isEmpty(permissions)) {
            return false;
        }
        return permissions.contains("*:*:*") || permissions.contains(permission);
    }

    public boolean hasAnyPermi(String... permissions) {
        for (String perm : permissions) {
            if (hasPermi(perm)) return true;
        }
        return false;
    }

    public static LoginUser getLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        return null;
    }
}
