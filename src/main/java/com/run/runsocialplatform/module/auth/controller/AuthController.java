package com.run.runsocialplatform.module.auth.controller;


import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.lang.UUID;

import com.run.runsocialplatform.common.constant.ResultCode;
import com.run.runsocialplatform.common.exception.BusinessException;
import com.run.runsocialplatform.common.result.Result;
import com.run.runsocialplatform.module.auth.model.dto.LoginDTO;
import com.run.runsocialplatform.module.auth.model.dto.RegisterDTO;
import com.run.runsocialplatform.module.auth.model.vo.CaptchaVO;
import com.run.runsocialplatform.module.auth.model.vo.LoginResultVO;
import com.run.runsocialplatform.module.auth.service.CaptchaService;
import com.run.runsocialplatform.module.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final UserService userService;
    private final CaptchaService captchaService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Long> register(@Valid @RequestBody RegisterDTO registerDTO) {
//        // 验证验证码
//        validateCaptcha(registerDTO.getCaptchaKey(), registerDTO.getCaptcha());

        Long userId = userService.register(registerDTO);
        return Result.success(userId);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResultVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        // 验证验证码（如果需要）
        if (loginDTO.getCaptcha() != null && loginDTO.getCaptchaKey() != null) {
            validateCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptcha());
        }

        LoginResultVO resultVO = userService.login(loginDTO);
        return Result.success(resultVO);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> logout() {
        // 登出逻辑由Spring Security处理
        return Result.success();
    }

    @PostMapping("/captcha/send")
    @Operation(summary = "发送验证码")
    public Result<CaptchaVO> sendCaptcha(@RequestParam String target,
                                         @RequestParam(defaultValue = "SMS") String type,
                                         @RequestParam(required = false) Integer length) {

        // 生成验证码Key
        String captchaKey = UUID.fastUUID().toString();

        // 生成验证码
        String captcha = captchaService.generateCaptcha(captchaKey, target, length, 300);

        // 发送验证码
        boolean sent = false;
        if ("SMS".equalsIgnoreCase(type)) {
            sent = captchaService.sendSmsCaptcha(target, captcha);
        } else if ("EMAIL".equalsIgnoreCase(type)) {
            sent = captchaService.sendEmailCaptcha(target, captcha);
        }

        // 构建响应
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaKey(captchaKey);
        captchaVO.setCaptchaLength(length != null ? length : 6);
        captchaVO.setCaptchaType(type);
        captchaVO.setTarget(target);
        captchaVO.setExpiresIn(300);
        captchaVO.setSent(sent);

        return Result.success(captchaVO);
    }

    @GetMapping("/captcha/check")
    @Operation(summary = "检查验证码发送频率")
    public Result<Boolean> checkCaptcha(@RequestParam String target) {
        boolean needCaptcha = captchaService.needCaptcha(target);
        return Result.success(!needCaptcha);
    }

    @PostMapping("/password/change")
    @Operation(summary = "修改密码")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> changePassword(@RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        // TODO: 从SecurityContext中获取当前用户ID
        Long currentUserId = 1L;
        userService.changePassword(currentUserId, oldPassword, newPassword);
        return Result.success();
    }

    @PostMapping("/password/reset")
    @Operation(summary = "重置密码")
    public Result<Void> resetPassword(@RequestParam String email,
                                      @RequestParam String newPassword,
                                      @RequestParam String captcha,
                                      @RequestParam String captchaKey) {
        // 验证验证码
        validateCaptcha(captchaKey, captcha);

        userService.resetPassword(email, newPassword, captcha);
        return Result.success();
    }

    /**
     * 验证验证码
     */
    private void validateCaptcha(String captchaKey, String captcha) {
        if (!captchaService.verifyCaptcha(captchaKey, captcha)) {
            throw new BusinessException(
                    ResultCode.PARAM_ERROR, "验证码错误或已过期"
            );
        }
    }
}