package com.university.asset.common.aspect;

import com.university.asset.common.annotation.DataScope;
import com.university.asset.entity.SysRole;
import com.university.asset.entity.SysUser;
import com.university.asset.mapper.SysRoleMapper;
import com.university.asset.security.LoginUser;
import com.university.asset.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class DataScopeAspect {

    private final SysRoleMapper roleMapper;

    private static final String DATA_SCOPE_ALL = "1";
    private static final String DATA_SCOPE_DEPT_AND_CHILD = "2";
    private static final String DATA_SCOPE_DEPT = "3";
    private static final String DATA_SCOPE_SELF = "4";

    @Before("@annotation(dataScope)")
    public void doBefore(JoinPoint joinPoint, DataScope dataScope) {
        LoginUser loginUser = PermissionService.getLoginUser();
        if (loginUser == null) return;

        SysUser user = loginUser.getUser();
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getUserId());
        if (roles == null || roles.isEmpty()) return;

        StringBuilder sqlConditions = new StringBuilder();
        String deptAlias = dataScope.deptAlias();
        String userAlias = dataScope.userAlias();

        for (SysRole role : roles) {
            String scope = role.getDataScope();
            if (DATA_SCOPE_ALL.equals(scope)) {
                sqlConditions = new StringBuilder();
                break;
            } else if (DATA_SCOPE_DEPT_AND_CHILD.equals(scope)) {
                sqlConditions.append(String.format(
                        " OR %s.dept_id IN (SELECT dept_id FROM sys_dept WHERE FIND_IN_SET(%d, ancestors) OR dept_id = %d)",
                        deptAlias, user.getDeptId(), user.getDeptId()));
            } else if (DATA_SCOPE_DEPT.equals(scope)) {
                sqlConditions.append(String.format(" OR %s.dept_id = %d", deptAlias, user.getDeptId()));
            } else if (DATA_SCOPE_SELF.equals(scope)) {
                sqlConditions.append(String.format(" OR %s.dept_id = %d", deptAlias, user.getDeptId()));
            }
        }

        if (!sqlConditions.isEmpty()) {
            String dataScopeSql = " AND (" + sqlConditions.toString().replaceFirst("^ OR ", "") + ")";
            injectDataScope(joinPoint, dataScopeSql);
        }
    }

    private void injectDataScope(JoinPoint joinPoint, String sql) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof com.university.asset.dto.BaseQueryDTO baseQuery) {
                baseQuery.setDataScope(sql);
            }
        }
    }
}
