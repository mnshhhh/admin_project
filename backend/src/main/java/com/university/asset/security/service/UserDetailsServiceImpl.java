package com.university.asset.security.service;

import com.university.asset.entity.SysUser;
import com.university.asset.mapper.SysMenuMapper;
import com.university.asset.mapper.SysUserMapper;
import com.university.asset.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if ("1".equals(user.getStatus())) {
            throw new UsernameNotFoundException("账号已停用");
        }
        Set<String> permissions = menuMapper.selectPermsByUserId(user.getUserId());
        return new LoginUser(user, permissions);
    }
}
