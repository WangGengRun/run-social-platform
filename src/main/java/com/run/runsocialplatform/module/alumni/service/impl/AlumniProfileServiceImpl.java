package com.run.runsocialplatform.module.alumni.service.impl;


import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.run.runsocialplatform.common.constant.ResultCode;
import com.run.runsocialplatform.common.exception.BusinessException;
import com.run.runsocialplatform.module.alumni.mapper.AlumniProfileMapper;
import com.run.runsocialplatform.module.alumni.model.entity.AlumniInfo;
import com.run.runsocialplatform.module.alumni.model.dto.AlumniProfileDTO;
import com.run.runsocialplatform.module.alumni.model.dto.AlumniSearchDTO;
import com.run.runsocialplatform.module.alumni.model.vo.AlumniListItemVO;
import com.run.runsocialplatform.module.alumni.model.vo.AlumniProfileVO;
import com.run.runsocialplatform.module.alumni.service.AlumniProfileService;
import com.run.runsocialplatform.module.auth.model.entity.UserEntity;
import com.run.runsocialplatform.module.auth.service.UserService;
import com.run.runsocialplatform.security.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Slf4j
@Service
@RequiredArgsConstructor
public class AlumniProfileServiceImpl extends ServiceImpl<AlumniProfileMapper, AlumniInfo>
        implements AlumniProfileService {

    private final AlumniProfileMapper alumniProfileMapper;
    private final UserService userService;

    @Override
    public AlumniProfileVO getCurrentUserProfile() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        // 直接按 userId 查询，不再自动创建“待审核”默认记录，避免未提交却显示审核中
        return getAlumniProfileByUserId(currentUserId);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentUserProfile(AlumniProfileDTO profileDTO) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        AlumniInfo alumniInfo = getByUserId(currentUserId);

        if (alumniInfo == null) {
            alumniInfo = new AlumniInfo();
            alumniInfo.setUserId(currentUserId);
            alumniInfo.setVerifyStatus(-1);
        }
        // 与“校友认证”解耦：编辑资料只允许更新非认证字段，不触发重新审核，也不改角色。
        alumniInfo.setCompany(profileDTO.getCompany());
        alumniInfo.setPosition(profileDTO.getPosition());
        alumniInfo.setCity(profileDTO.getCity());
        alumniInfo.setBio(profileDTO.getBio());

        if (alumniInfo.getId() == null) {
            // 新增
            save(alumniInfo);
        } else {
            // 更新
            updateById(alumniInfo);
        }

        // 同步更新用户基础信息（与校友认证解耦，不触发审核）
        String email = StrUtil.trim(profileDTO.getEmail());
        if (StrUtil.isBlank(email)) email = null;
        String phone = StrUtil.trim(profileDTO.getPhone());
        if (StrUtil.isBlank(phone)) phone = null;

        if (StrUtil.isNotBlank(email)) {
            long emailCount = userService.lambdaQuery()
                    .eq(UserEntity::getEmail, email)
                    .ne(UserEntity::getId, currentUserId)
                    .count();
            if (emailCount > 0) {
                throw new BusinessException(ResultCode.EMAIL_EXISTS, "邮箱已被其他用户使用");
            }
        }
        if (StrUtil.isNotBlank(phone)) {
            long phoneCount = userService.lambdaQuery()
                    .eq(UserEntity::getPhone, phone)
                    .ne(UserEntity::getId, currentUserId)
                    .count();
            if (phoneCount > 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "手机号已被其他用户使用");
            }
        }

        UserEntity user = new UserEntity();
        user.setId(currentUserId);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAvatar(profileDTO.getAvatar());
        userService.updateById(user);

        log.info("校友档案更新成功，用户ID: {}", currentUserId);
    }

    @Override
    public AlumniProfileVO getAlumniProfile(Long alumniId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        AlumniProfileVO profile = alumniProfileMapper.selectAlumniDetail(alumniId, currentUserId);
        if (profile == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "校友信息不存在");
        }

        return profile;
    }

    @Override
    public AlumniProfileVO getAlumniProfileByUserId(Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        AlumniProfileVO profile = alumniProfileMapper.selectAlumniDetailByUserId(userId, currentUserId);
        if (profile == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }
        return profile;
    }


    @Override
    public Page<AlumniListItemVO> searchAlumni(AlumniSearchDTO searchDTO) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Page<AlumniListItemVO> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());

        // 执行搜索
        IPage<AlumniListItemVO> result = alumniProfileMapper.searchAlumniList(
                page, searchDTO, currentUserId);

        return (Page<AlumniListItemVO>) result;
    }

    @Override
    public Page<AlumniListItemVO> getAlumniList(Page<AlumniListItemVO> page) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        AlumniSearchDTO searchDTO = new AlumniSearchDTO();
        searchDTO.setVerifiedOnly(true); // 默认只显示已认证校友

        IPage<AlumniListItemVO> result = alumniProfileMapper.searchAlumniList(
                page, searchDTO, currentUserId);

        return (Page<AlumniListItemVO>) result;
    }

    @Override
    public boolean validateProfileComplete(Long userId) {
        AlumniInfo alumniInfo = getByUserId(userId);
        if (alumniInfo == null) {
            return false;
        }

        // 检查必填字段是否完整
        return StrUtil.isNotBlank(alumniInfo.getRealName())
                && StrUtil.isNotBlank(alumniInfo.getStudentId())
                && alumniInfo.getAdmissionYear() != null
                && alumniInfo.getVerifyStatus() == 1; // 且已通过验证
    }

    @Override
    public Page<AlumniListItemVO> getHotAlumni(Page<AlumniListItemVO> page) {
        // 热门校友推荐逻辑：按照关注数、动态数从高到低排序
        Long currentUserId = SecurityUtil.getCurrentUserId();
        AlumniSearchDTO searchDTO = new AlumniSearchDTO();
        searchDTO.setVerifiedOnly(true); // 只显示已认证校友

        IPage<AlumniListItemVO> result = alumniProfileMapper.searchHotAlumniList(
                page, searchDTO, currentUserId);

        return (Page<AlumniListItemVO>) result;
    }

    /**
     * 根据用户ID获取校友信息
     */
    private AlumniInfo getByUserId(Long userId) {
        return lambdaQuery()
                .eq(AlumniInfo::getUserId, userId)
                .one();
    }

}