package com.run.runsocialplatform.module.auth.service;


public interface CaptchaService {

    /**
     * 生成验证码
     */
    String generateCaptcha(String captchaKey, String target, Integer length, Integer expireSeconds);

    /**
     * 验证验证码
     */
    boolean verifyCaptcha(String captchaKey, String captcha);

    /**
     * 发送短信验证码
     */
    boolean sendSmsCaptcha(String phone, String captcha);

    /**
     * 发送邮箱验证码
     */
    boolean sendEmailCaptcha(String email, String captcha);

    /**
     * 删除验证码
     */
    void deleteCaptcha(String captchaKey);

    /**
     * 检查是否需要验证码（用于防止频繁请求）
     */
    boolean needCaptcha(String target);
}