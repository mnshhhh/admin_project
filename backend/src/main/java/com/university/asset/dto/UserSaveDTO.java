package com.university.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserSaveDTO {
    private Long userId;
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String nickname;
    private String password;
    @NotNull(message = "部门不能为空")
    private Long deptId;
    private String phone;
    private String email;
    private String status;
    private List<Long> roleIds;
}
