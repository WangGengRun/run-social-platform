package com.run.runsocialplatform.module.auth.model.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "验证码响应")
public class CaptchaVO {

    @Schema(description = "验证码Key，用于后续验证", example = "uuid")
    private String captchaKey;

    @Schema(description = "验证码长度", example = "4")
    private Integer captchaLength;

    @Schema(description = "验证码类型", example = "SMS", allowableValues = {"SMS", "EMAIL"})
    private String captchaType;

    @Schema(description = "目标（手机号或邮箱）", example = "13800138000")
    private String target;

    @Schema(description = "有效期（秒）", example = "300")
    private Integer expiresIn;

    @Schema(description = "发送状态", example = "true")
    private Boolean sent;
}