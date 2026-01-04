package com.run.runsocialplatform.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证码类型枚举
 */
@Getter
@AllArgsConstructor
public enum CaptchaTypeEnum {

    SMS("SMS", "短信验证码"),
    EMAIL("EMAIL", "邮件验证码");

    private final String code;
    private final String desc;

    public static CaptchaTypeEnum getByCode(String code) {
        for (CaptchaTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return SMS; // 默认返回短信验证码
    }
}