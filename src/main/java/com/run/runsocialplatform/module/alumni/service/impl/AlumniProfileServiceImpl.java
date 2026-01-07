package com.run.runsocialplatform.module.alumni.service.impl;


import cn.hutool.core.bean.BeanUtil;
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
        AlumniInfo alumniInfo = getByUserId(currentUserId);

        if (alumniInfo == null) {
            // 如果还没有校友信息，创建默认信息
            alumniInfo = createDefaultAlumniInfo(currentUserId);
        }

        // 查询完整详情
        AlumniProfileVO profile = alumniProfileMapper.selectAlumniDetail(
                alumniInfo.getId(), currentUserId);

        return profile;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentUserProfile(AlumniProfileDTO profileDTO) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        AlumniInfo alumniInfo = getByUserId(currentUserId);

        if (alumniInfo == null) {
            alumniInfo = new AlumniInfo();
            alumniInfo.setUserId(currentUserId);
            alumniInfo.setVerifyStatus(0); // 更新信息后需要重新审核
        } else {
            // 如果修改了关键信息，需要重新审核
            boolean needReverify = !alumniInfo.getStudentId().equals(profileDTO.getStudentId())
                    || !alumniInfo.getRealName().equals(profileDTO.getRealName())
                    || !alumniInfo.getAdmissionYear().equals(profileDTO.getAdmissionYear());

            if (needReverify) {
                alumniInfo.setVerifyStatus(0);
            }
        }

        // 检查学号是否已被其他用户使用
        if (alumniInfo.getId() == null ||
                !alumniInfo.getStudentId().equals(profileDTO.getStudentId())) {
            int count = alumniProfileMapper.countByStudentIdExcludeUser(
                    profileDTO.getStudentId(), currentUserId);
            if (count > 0) {
                throw new BusinessException(ResultCode.STUDENT_ID_EXISTS, "学号已被其他用户使用");
            }
        }

        // 复制属性
        BeanUtil.copyProperties(profileDTO, alumniInfo, "id", "userId", "verifyStatus");

        if (alumniInfo.getId() == null) {
            // 新增
            save(alumniInfo);
        } else {
            // 更新
            updateById(alumniInfo);
        }

        log.info("校友档案更新成功，用户ID: {}", currentUserId);
    }

    @Override
    public AlumniProfileVO getAlumniProfile(Long alumniId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        AlumniProfileVO profile = alumniProfileMapper.selectAlumniDetail(alumniId, currentUserId);
        if (profile == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "校友信息不存在");
        }

        // 如果不是本人，检查可见性（这里可以扩展隐私设置）
        if (!profile.getIsSelf()) {
            // 检查校友是否已验证
            if (profile.getVerifyStatus() != 1) {
                throw new BusinessException(ResultCode.ALUMNI_NOT_VERIFIED, "该校友信息未通过验证");
            }
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
        // 简单的热门校友推荐逻辑
        // 可以根据关注数、动态数等排序，这里先按注册时间倒序
        Long currentUserId = SecurityUtil.getCurrentUserId();
        AlumniSearchDTO searchDTO = new AlumniSearchDTO();
        searchDTO.setVerifiedOnly(true);
        searchDTO.setSortField("createTime");
        searchDTO.setSortOrder("desc");

        IPage<AlumniListItemVO> result = alumniProfileMapper.searchAlumniList(
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

    /**
     * 创建默认校友信息
     */
    private AlumniInfo createDefaultAlumniInfo(Long userId) {
        UserEntity user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        AlumniInfo alumniInfo = new AlumniInfo();
        alumniInfo.setUserId(userId);
        alumniInfo.setRealName("未设置");
        alumniInfo.setStudentId("TEMP_" + userId);
        alumniInfo.setAdmissionYear(2000);
        alumniInfo.setVerifyStatus(0);

        save(alumniInfo);
        return alumniInfo;
    }
}