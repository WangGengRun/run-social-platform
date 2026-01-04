package com.run.runsocialplatform.module.auth.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
@Schema(description = "登录请求参数")
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "验证码", example = "123456")
    private String captcha;

    @Schema(description = "验证码Key", example = "uuid")
    private String captchaKey;

    @Schema(description = "设备ID（用于多端登录管理）", example = "web-001")
    private String deviceId;

    @Schema(description = "设备类型", example = "web", allowableValues = {"web", "android", "ios"})
    private String deviceType = "web";
}