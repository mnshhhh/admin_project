package com.university.asset.service;

import com.university.asset.common.annotation.Log;
import com.university.asset.dto.LoginDTO;
import com.university.asset.entity.SysMenu;
import com.university.asset.entity.SysRole;
import com.university.asset.mapper.SysMenuMapper;
import com.university.asset.mapper.SysRoleMapper;
import com.university.asset.security.LoginUser;
import com.university.asset.utils.JwtUtils;
import com.university.asset.vo.LoginVO;
import com.university.asset.vo.RouterVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;

    @Log("用户登录")
    public LoginVO login(LoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = jwtUtils.generateToken(loginUser.getUserId(), loginUser.getUsername());

        List<SysRole> roles = roleMapper.selectRolesByUserId(loginUser.getUserId());
        List<String> roleKeys = roles.stream().map(SysRole::getRoleKey).collect(Collectors.toList());

        List<SysMenu> menus = menuMapper.selectMenusByUserId(loginUser.getUserId());
        List<RouterVO> routers = buildRouterTree(menus, 0L);

        return LoginVO.builder()
                .token(token)
                .userId(loginUser.getUserId())
                .username(loginUser.getUsername())
                .nickname(loginUser.getUser().getNickname())
                .permissions(loginUser.getPermissions())
                .roles(roleKeys)
                .routers(routers)
                .build();
    }

    private List<RouterVO> buildRouterTree(List<SysMenu> menus, Long parentId) {
        List<RouterVO> result = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId().equals(parentId) && !"F".equals(menu.getMenuType())) {
                RouterVO router = new RouterVO();
                router.setName(menu.getMenuName());
                router.setPath(menu.getPath());
                router.setComponent(menu.getComponent() != null ? menu.getComponent() : "Layout");
                RouterVO.RouterMeta meta = new RouterVO.RouterMeta();
                meta.setTitle(menu.getMenuName());
                meta.setIcon(menu.getIcon());
                meta.setHidden("1".equals(menu.getVisible()));
                router.setMeta(meta);
                List<RouterVO> children = buildRouterTree(menus, menu.getMenuId());
                if (!children.isEmpty()) {
                    router.setChildren(children);
                }
                result.add(router);
            }
        }
        return result;
    }
}
