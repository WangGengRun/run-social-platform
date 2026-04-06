package com.run.runsocialplatform.module.auth.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.run.runsocialplatform.common.constant.ResultCode;
import com.run.runsocialplatform.common.exception.BusinessException;
import com.run.runsocialplatform.module.alumni.mapper.AlumniProfileMapper;
import com.run.runsocialplatform.module.alumni.model.entity.AlumniInfo;
import com.run.runsocialplatform.module.auth.mapper.UserMapper;
import com.run.runsocialplatform.module.auth.model.dto.AlumniVerifyDTO;
import com.run.runsocialplatform.module.auth.model.dto.LoginDTO;
import com.run.runsocialplatform.module.auth.model.dto.RegisterDTO;
import com.run.runsocialplatform.module.auth.model.entity.UserEntity;
import com.run.runsocialplatform.module.auth.model.vo.AlumniVerifyStatusVO;
import com.run.runsocialplatform.module.auth.model.vo.LoginResultVO;
import com.run.runsocialplatform.module.auth.model.vo.UserInfoVO;
import com.run.runsocialplatform.module.auth.service.CaptchaService;
import com.run.runsocialplatform.module.auth.service.UserService;
import com.run.runsocialplatform.security.utils.JwtUtil;
import com.run.runsocialplatform.security.utils.SecurityUtil;
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
    private final AlumniProfileMapper alumniProfileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO.getUsername());

        // 验证两次密码是否一致
        if (!StrUtil.equals(registerDTO.getPassword(), registerDTO.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "两次输入的密码不一致");
        }

        // 检查用户名是否存在
        if (checkUsernameExists(registerDTO.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 检查邮箱是否存在
        if (StrUtil.isNotBlank(registerDTO.getEmail()) && checkEmailExists(registerDTO.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        // 创建用户实体
        UserEntity user = new UserEntity();
        BeanUtils.copyProperties(registerDTO, user);

        // 加密密码
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));

        // 设置默认角色和状态
        user.setRole("USER");
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

        // 确保角色与认证状态一致（历史数据纠偏：认证通过即ALUMNI）
        AlumniInfo alumniInfo = alumniProfileMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AlumniInfo>()
                        .eq(AlumniInfo::getUserId, user.getId())
        );
        String normalizedRole = normalizeRoleByVerifyStatus(user, alumniInfo);

        // 构建登录结果
        LoginResultVO resultVO = new LoginResultVO();
        resultVO.setToken(token);
        resultVO.setTokenType("Bearer");
        resultVO.setExpiresIn(jwtUtil.getExpirationFromToken(token));
        resultVO.setUserId(user.getId());
        resultVO.setUsername(user.getUsername());
        resultVO.setRole(normalizedRole);
        resultVO.setUserInfo(convertToUserInfoVO(user, normalizedRole));

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
    private UserInfoVO convertToUserInfoVO(UserEntity user, String role) {
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole(role);
        userInfo.setLastLoginTime(user.getLastLoginTime());
        userInfo.setCreatedAt(user.getCreatedAt());

        AlumniInfo alumniInfo = alumniProfileMapper.selectWithUserInfo(user.getId());
        userInfo.setAlumniVerifyStatus(alumniInfo == null ? -1 : alumniInfo.getVerifyStatus());

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

    @Override
    public void setAvatar(String objectName) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        UserEntity user = baseMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        // 更新密码
        user.setAvatar(objectName);
        baseMapper.updateById(user);

        log.info("用户修改头像成功，用户ID: {}", currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAlumniVerify(AlumniVerifyDTO verifyDTO) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        UserEntity user = baseMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "管理员无需提交校友认证");
        }

        AlumniInfo alumniInfo = alumniProfileMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AlumniInfo>()
                        .eq(AlumniInfo::getUserId, currentUserId)
        );
        if (alumniInfo == null) {
            alumniInfo = new AlumniInfo();
            alumniInfo.setUserId(currentUserId);
        }

        int count = alumniProfileMapper.countByStudentIdExcludeUser(verifyDTO.getStudentId(), currentUserId);
        if (count > 0) {
            throw new BusinessException(ResultCode.STUDENT_ID_EXISTS, "学号已被其他用户使用");
        }

        alumniInfo.setRealName(verifyDTO.getRealName());
        alumniInfo.setStudentId(verifyDTO.getStudentId());
        alumniInfo.setAdmissionYear(verifyDTO.getAdmissionYear());
        alumniInfo.setGraduationYear(verifyDTO.getGraduationYear());
        alumniInfo.setCollege(verifyDTO.getCollege());
        alumniInfo.setMajor(verifyDTO.getMajor());
        alumniInfo.setCompany(verifyDTO.getCompany());
        alumniInfo.setPosition(verifyDTO.getPosition());
        alumniInfo.setCity(verifyDTO.getCity());
        alumniInfo.setBio(verifyDTO.getBio());
        alumniInfo.setVerifyStatus(0);
        alumniInfo.setVerifyNotes(null);
        alumniInfo.setVerifyTime(null);
        alumniInfo.setVerifyAdminId(null);

        if (alumniInfo.getId() == null) {
            alumniProfileMapper.insert(alumniInfo);
        } else {
            alumniProfileMapper.updateById(alumniInfo);
        }
    }

    @Override
    public AlumniVerifyStatusVO getCurrentAlumniVerifyStatus() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        UserEntity user = baseMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        AlumniInfo alumniInfo = alumniProfileMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AlumniInfo>()
                        .eq(AlumniInfo::getUserId, currentUserId)
        );

        AlumniVerifyStatusVO vo = new AlumniVerifyStatusVO();
        String normalizedRole = normalizeRoleByVerifyStatus(user, alumniInfo);
        vo.setRole(normalizedRole);
        if (alumniInfo == null) {
            vo.setVerifyStatus(-1);
            return vo;
        }
        vo.setVerifyStatus(alumniInfo.getVerifyStatus());
        vo.setVerifyNotes(alumniInfo.getVerifyNotes());
        vo.setVerifyTime(alumniInfo.getVerifyTime());
        return vo;
    }

    /**
     * 角色与认证状态一致性处理：
     * 1) 管理员保持ADMIN，不参与校友角色自动切换
     * 2) 认证通过(verifyStatus=1) => ALUMNI
     * 3) 未通过/未提交 => USER
     */
    private String normalizeRoleByVerifyStatus(UserEntity user, AlumniInfo alumniInfo) {
        if ("ADMIN".equals(user.getRole())) {
            return "ADMIN";
        }
        String expectedRole = (alumniInfo != null && Integer.valueOf(1).equals(alumniInfo.getVerifyStatus()))
                ? "ALUMNI" : "USER";
        if (!expectedRole.equals(user.getRole())) {
            UserEntity update = new UserEntity();
            update.setId(user.getId());
            update.setRole(expectedRole);
            userMapper.updateById(update);
            user.setRole(expectedRole);
        }
        return expectedRole;
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