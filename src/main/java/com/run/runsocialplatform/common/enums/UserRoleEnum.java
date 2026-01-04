package com.run.runsocialplatform.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    ADMIN("ADMIN", "管理员"),
    ALUMNI("ALUMNI", "校友");

    private final String code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static UserRoleEnum getByCode(String code) {
        for (UserRoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return ALUMNI; // 默认返回校友
    }
}