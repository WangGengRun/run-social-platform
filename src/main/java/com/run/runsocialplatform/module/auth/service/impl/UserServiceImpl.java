package com.run.runsocialplatform.module.auth.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.run.runsocialplatform.common.constant.ResultCode;
import com.run.runsocialplatform.common.exception.BusinessException;
import com.run.runsocialplatform.module.auth.mapper.UserMapper;
import com.run.runsocialplatform.module.auth.model.dto.LoginDTO;
import com.run.runsocialplatform.module.auth.model.dto.RegisterDTO;
import com.run.runsocialplatform.module.auth.model.entity.UserEntity;
import com.run.runsocialplatform.module.auth.model.vo.LoginResultVO;
import com.run.runsocialplatform.module.auth.model.vo.UserInfoVO;
import com.run.runsocialplatform.module.auth.service.CaptchaService;
import com.run.runsocialplatform.module.auth.service.UserService;
import com.run.runsocialplatform.security.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    private final UserMapper userMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final CaptchaService captchaService;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO.getUsername());

        // 验证两次密码是否一致
        if (!StrUtil.equals(registerDTO.getPassword(), registerDTO.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "两次输入的密码不一致");
        }

        // 验证验证码（如果提供了）
        if (StrUtil.isNotBlank(registerDTO.getCaptcha()) &&
                StrUtil.isNotBlank(registerDTO.getCaptchaKey())) {

            if (!captchaService.verifyCaptcha(registerDTO.getCaptchaKey(), registerDTO.getCaptcha())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "验证码错误或已过期");
            }
        }

        // 检查用户名是否存在
        if (checkUsernameExists(registerDTO.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 检查邮箱是否存在
        if (StrUtil.isNotBlank(registerDTO.getEmail()) && checkEmailExists(registerDTO.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        // 检查手机号是否存在
        if (StrUtil.isNotBlank(registerDTO.getPhone()) && checkPhoneExists(registerDTO.getPhone())) {
            throw new BusinessException(ResultCode.PHONE_EXISTS);
        }

        // 创建用户实体
        UserEntity user = new UserEntity();
        BeanUtils.copyProperties(registerDTO, user);

        // 加密密码
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));

        // 设置默认角色和状态
        user.setRole("ALUMNI");
        user.setStatus(1);

        // 保存用户
        userMapper.insert(user);

        log.info("用户注册成功，用户ID: {}", user.getId());
        return user.getId();
    }


    @Override
    public LoginResultVO login(LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getUsername());

        // 验证验证码（如果提供了）
        if (StrUtil.isNotBlank(loginDTO.getCaptcha()) &&
                StrUtil.isNotBlank(loginDTO.getCaptchaKey())) {

            if (!captchaService.verifyCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptcha())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "验证码错误或已过期");
            }
        }

        // 获取用户
        UserEntity user = getByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 更新最后登录时间
        updateLastLoginTime(user.getId());

        // 生成JWT Token
        String token = generateToken(user);

        // 构建登录结果
        LoginResultVO resultVO = new LoginResultVO();
        resultVO.setToken(token);
        resultVO.setTokenType("Bearer");
        resultVO.setExpiresIn(jwtUtil.getExpirationFromToken(token));
        resultVO.setUserId(user.getId());
        resultVO.setUsername(user.getUsername());
        resultVO.setRole(user.getRole());
        resultVO.setUserInfo(convertToUserInfoVO(user));

        log.info("用户登录成功: {}, Token已生成", user.getUsername());
        return resultVO;
    }

    /**
     * 生成JWT Token
     */
    private String generateToken(UserEntity user) {
        // 构建Spring Security的UserDetails
        UserDetails userDetails = User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority(user.getRole())
                ))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

        // 使用JwtUtil生成Token
        return jwtUtil.generateToken(userDetails);
    }

    /**
     * 从Token获取过期时间（秒）
     */
    private Long getExpirationFromToken(String token) {
        try {
            java.util.Date expiration = jwtUtil.getExpirationDateFromToken(token);
            long currentTime = System.currentTimeMillis();
            return (expiration.getTime() - currentTime) / 1000;
        } catch (Exception e) {
            log.error("获取Token过期时间失败", e);
            return 86400L; // 默认24小时
        }
    }

    /**
     * 转换用户信息到VO
     */
    private UserInfoVO convertToUserInfoVO(UserEntity user) {
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole(user.getRole());
        userInfo.setLastLoginTime(user.getLastLoginTime());
        userInfo.setCreatedAt(user.getCreatedAt());

        // TODO: 可以在这里查询校友认证状态并设置
        userInfo.setAlumniVerifyStatus(0); // 默认为未认证

        return userInfo;
    }



    @Override
    public UserEntity getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public UserEntity getByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public UserEntity getByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        Long count = userMapper.countByUsername(username);
        return count != null && count > 0;
    }

    @Override
    public boolean checkEmailExists(String email) {
        Long count = userMapper.countByEmail(email);
        return count != null && count > 0;
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        Long count = userMapper.countByPhone(phone);
        return count != null && count > 0;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        UserEntity user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证旧密码
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 更新密码
        user.setPassword(BCrypt.hashpw(newPassword));
        baseMapper.updateById(user);

        log.info("用户修改密码成功，用户ID: {}", userId);
    }

    @Override
    public void resetPassword(String email, String newPassword, String captcha) {
        // 验证验证码已经在Controller层完成

        // 获取用户
        UserEntity user = getByEmail(email);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 更新密码
        user.setPassword(BCrypt.hashpw(newPassword));
        userMapper.updateById(user);

        log.info("用户重置密码成功，邮箱: {}", email);
    }

    @Override
    public void updateLastLoginTime(Long userId) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        baseMapper.updateById(user);
    }

    @Override
    public UserEntity findByUsername(String username) {
        return lambdaQuery()
                .eq(UserEntity::getUsername, username)
                .one();
    }

    /**
     * 验证验证码
     */
    private void validateCaptcha(String captchaKey, String captcha) {
        String cacheCaptcha = redisTemplate.opsForValue().get("captcha:" + captchaKey);
        if (StrUtil.isBlank(cacheCaptcha)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "验证码已过期");
        }

        if (!StrUtil.equalsIgnoreCase(cacheCaptcha, captcha)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "验证码错误");
        }

        // 验证成功后删除验证码
        redisTemplate.delete("captcha:" + captchaKey);
    }
}