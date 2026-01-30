package com.run.runsocialplatform.module.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserManageVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;
    private String realName;
    private String studentId;
    private Integer verifyStatus;
}
