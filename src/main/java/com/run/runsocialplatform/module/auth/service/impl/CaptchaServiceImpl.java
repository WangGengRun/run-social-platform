package com.run.runsocialplatform.module.auth.service.impl;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.run.runsocialplatform.common.constant.ResultCode;
import com.run.runsocialplatform.common.exception.BusinessException;
import com.run.runsocialplatform.module.auth.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis key前缀
    private static final String CAPTCHA_PREFIX = "auth:captcha:";
    private static final String CAPTCHA_LIMIT_PREFIX = "auth:captcha:limit:";

    // 默认配置
    private static final Integer DEFAULT_LENGTH = 6;
    private static final Integer DEFAULT_EXPIRE_SECONDS = 300; // 5分钟
    private static final Integer SEND_INTERVAL = 60; // 发送间隔60秒

    @Override
    public String generateCaptcha(String captchaKey, String target, Integer length, Integer expireSeconds) {
        if (length == null || length < 4 || length > 6) {
            length = DEFAULT_LENGTH;
        }
        if (expireSeconds == null || expireSeconds < 60) {
            expireSeconds = DEFAULT_EXPIRE_SECONDS;
        }

        // 检查发送频率
        if (needCaptcha(target)) {
            throw new BusinessException(ResultCode.REQUEST_LIMIT, "验证码发送过于频繁，请稍后再试");
        }

        // 生成数字验证码
        String captcha;
        if (length == 4) {
            captcha = RandomUtil.randomNumbers(4);
        } else if (length == 5) {
            captcha = RandomUtil.randomNumbers(5);
        } else {
            captcha = RandomUtil.randomNumbers(6);
        }

        // 存储验证码
        String redisKey = CAPTCHA_PREFIX + captchaKey;
        redisTemplate.opsForValue().set(redisKey, captcha, expireSeconds, TimeUnit.SECONDS);

        // 记录发送时间
        String limitKey = CAPTCHA_LIMIT_PREFIX + target;
        redisTemplate.opsForValue().set(limitKey, "1", SEND_INTERVAL, TimeUnit.SECONDS);

        log.info("生成验证码成功，目标: {}, 验证码: {}, 有效期: {}秒", target, captcha, expireSeconds);
        return captcha;
    }

    @Override
    public boolean verifyCaptcha(String captchaKey, String captcha) {
        if (StrUtil.isBlank(captchaKey) || StrUtil.isBlank(captcha)) {
            return false;
        }

        String redisKey = CAPTCHA_PREFIX + captchaKey;
        String storedCaptcha = (String) redisTemplate.opsForValue().get(redisKey);

        if (StrUtil.isBlank(storedCaptcha)) {
            return false;
        }

        boolean valid = storedCaptcha.equals(captcha);

        // 验证成功后删除验证码（一次性使用）
        if (valid) {
            redisTemplate.delete(redisKey);
            log.debug("验证码验证成功，key: {}", captchaKey);
        } else {
            log.warn("验证码验证失败，key: {}, 输入: {}, 存储: {}", captchaKey, captcha, storedCaptcha);
        }

        return valid;
    }

    @Override
    public boolean sendSmsCaptcha(String phone, String captcha) {
        // 这里模拟发送短信验证码
        // 实际项目中应该集成短信服务商（阿里云、腾讯云等）
        log.info("发送短信验证码到 {}: {}", phone, captcha);

        // TODO: 实际集成短信服务
        // 示例：调用短信服务API
        // smsService.send(phone, "【校友平台】您的验证码是：" + captcha + "，5分钟内有效");

        return true;
    }

    @Override
    public boolean sendEmailCaptcha(String email, String captcha) {
        // 这里模拟发送邮件验证码
        log.info("发送邮件验证码到 {}: {}", email, captcha);

        // TODO: 实际集成邮件服务
        // 示例：调用邮件服务API
        // emailService.send(email, "校友平台验证码",
        //     "您的验证码是：" + captcha + "，5分钟内有效");

        return true;
    }

    @Override
    public void deleteCaptcha(String captchaKey) {
        if (StrUtil.isNotBlank(captchaKey)) {
            String redisKey = CAPTCHA_PREFIX + captchaKey;
            redisTemplate.delete(redisKey);
        }
    }

    @Override
    public boolean needCaptcha(String target) {
        if (StrUtil.isBlank(target)) {
            return false;
        }

        String limitKey = CAPTCHA_LIMIT_PREFIX + target;
        Boolean exists = redisTemplate.hasKey(limitKey);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 获取剩余发送时间（秒）
     */
    public Long getRemainingTime(String target) {
        if (StrUtil.isBlank(target)) {
            return 0L;
        }

        String limitKey = CAPTCHA_LIMIT_PREFIX + target;
        Long expire = redisTemplate.getExpire(limitKey, TimeUnit.SECONDS);
        return expire != null ? expire : 0L;
    }
}