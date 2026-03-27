package com.university.asset.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private Set<String> permissions;
    private List<String> roles;
    private List<RouterVO> routers;
}
