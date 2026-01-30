package com.run.runsocialplatform.module.admin.model.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private String username;
    private String email;
    private String phone;
    private String role;
    private Integer status;
    private String realName;
    private String studentId;
}
