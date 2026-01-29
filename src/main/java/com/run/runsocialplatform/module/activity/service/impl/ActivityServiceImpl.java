package com.run.runsocialplatform.module.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.run.runsocialplatform.common.constant.ResultCode;
import com.run.runsocialplatform.common.exception.BusinessException;
import com.run.runsocialplatform.module.activity.mapper.ActivityMapper;
import com.run.runsocialplatform.module.activity.mapper.ActivityParticipationMapper;
import com.run.runsocialplatform.module.activity.model.dto.ActivityCreateDTO;
import com.run.runsocialplatform.module.activity.model.dto.ActivityUpdateDTO;
import com.run.runsocialplatform.module.activity.model.entity.Activity;
import com.run.runsocialplatform.module.activity.model.entity.ActivityParticipation;
import com.run.runsocialplatform.module.activity.model.vo.ActivityDetailVO;
import com.run.runsocialplatform.module.activity.model.vo.ActivityListVO;
import com.run.runsocialplatform.module.activity.model.vo.ParticipantVO;
import com.run.runsocialplatform.module.activity.service.ActivityService;
import com.run.runsocialplatform.module.auth.model.entity.UserEntity;
import com.run.runsocialplatform.module.auth.service.UserService;
import com.run.runsocialplatform.security.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    private final ActivityParticipationMapper participationMapper;
    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ActivityCreateDTO dto) {
        validateTime(dto.getStartTime(), dto.getEndTime());

        Activity activity = new Activity();
        BeanUtils.copyProperties(dto, activity);
        activity.setOrganizerId(SecurityUtil.getCurrentUserId());
        activity.setState(0); // 待发布
        activity.setCurrentParticipants(0);
        save(activity);
        log.info("创建活动成功，ID: {}", activity.getId());
        return activity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long activityId, ActivityUpdateDTO dto) {
        Activity activity = getById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "活动不存在");
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!activity.getOrganizerId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权修改该活动");
        }
        if (activity.getState() == 2 || activity.getState() == 3) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "活动已结束或已取消，无法修改");
        }
        validateTime(dto.getStartTime(), dto.getEndTime());

        BeanUtils.copyProperties(dto, activity);
        updateById(activity);
        log.info("更新活动成功，ID: {}", activityId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long activityId) {
        Activity activity = getById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "活动不存在");
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!activity.getOrganizerId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权发布该活动");
        }
        if (activity.getState() != 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "只有待发布的活动可发布");
        }
        activity.setState(1);
        updateById(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long activityId) {
        Activity activity = getById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "活动不存在");
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!activity.getOrganizerId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权取消该活动");
        }
        if (activity.getState() == 2 || activity.getState() == 3) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "活动已结束或已取消");
        }
        activity.setState(3);
        updateById(activity);
    }

    @Override
    public ActivityDetailVO detail(Long activityId) {
        Activity activity = getById(activityId);
        if (activity == null || activity.getState() == 3) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "活动不存在");
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        boolean joined = participationMapper.selectCount(new LambdaQueryWrapper<ActivityParticipation>()
                .eq(ActivityParticipation::getActivityId, activityId)
                .eq(ActivityParticipation::getUserId, currentUserId)
                .eq(ActivityParticipation::getState, 1)) > 0;

        ActivityDetailVO vo = new ActivityDetailVO();
        BeanUtils.copyProperties(activity, vo);
        vo.setJoined(joined);
        vo.setStatus(activity.getState());

        UserEntity organizer = userService.getById(activity.getOrganizerId());
        if (organizer != null) {
            vo.setOrganizerName(organizer.getUsername());
            vo.setOrganizerAvatar(organizer.getAvatar());
        }
        return vo;
    }

    @Override
    public Page<ActivityListVO> listActivities(Integer pageNum, Integer pageSize, Integer status, String keyword) {
        Page<Activity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Activity::getState, status);
        } else {
            wrapper.ne(Activity::getState, 3); // 默认不展示已取消
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Activity::getTitle, keyword)
                    .or().like(Activity::getDescription, keyword)
                    .or().like(Activity::getLocation, keyword));
        }
        wrapper.orderByDesc(Activity::getCreatedAt);
        page(page, wrapper);

        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<Long> joinedIds = participationMapper.selectList(new LambdaQueryWrapper<ActivityParticipation>()
                        .eq(ActivityParticipation::getUserId, currentUserId)
                        .eq(ActivityParticipation::getState, 1))
                .stream().map(ActivityParticipation::getActivityId).collect(Collectors.toList());

        Page<ActivityListVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        List<ActivityListVO> records = page.getRecords().stream().map(a -> {
            ActivityListVO vo = new ActivityListVO();
            BeanUtils.copyProperties(a, vo);
            vo.setJoined(joinedIds.contains(a.getId()));
            vo.setStatus(a.getState());
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public Page<ActivityListVO> listMyOrganized(Integer pageNum, Integer pageSize) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Page<Activity> page = page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getOrganizerId, currentUserId)
                        .orderByDesc(Activity::getCreatedAt));
        Page<ActivityListVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        List<ActivityListVO> records = page.getRecords().stream().map(a -> {
            ActivityListVO vo = new ActivityListVO();
            BeanUtils.copyProperties(a, vo);
            vo.setJoined(true); // 自己组织的默认视为已加入
            vo.setStatus(a.getState());
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public Page<ParticipantVO> listParticipants(Long activityId, Integer pageNum, Integer pageSize) {
        Page<ActivityParticipation> page = new Page<>(pageNum, pageSize);
        participationMapper.selectPage(page, new LambdaQueryWrapper<ActivityParticipation>()
                .eq(ActivityParticipation::getActivityId, activityId)
                .in(ActivityParticipation::getState, List.of(1, 2))
                .orderByAsc(ActivityParticipation::getCreatedAt));

        List<Long> userIds = page.getRecords().stream()
                .map(ActivityParticipation::getUserId)
                .toList();
        List<UserEntity> users = userIds.isEmpty() ? List.of() : userService.listByIds(userIds);
        Page<ParticipantVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        List<ParticipantVO> records = page.getRecords().stream().map(p -> {
            ParticipantVO vo = new ParticipantVO();
            vo.setUserId(p.getUserId());
            vo.setStatus(p.getState());
            vo.setCheckinTime(p.getCheckinTime());
            vo.setCreatedAt(p.getCreatedAt());
            users.stream().filter(u -> u.getId().equals(p.getUserId())).findFirst().ifPresent(u -> {
                vo.setUsername(u.getUsername());
                vo.setAvatar(u.getAvatar());
            });
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signup(Long activityId) {
        Activity activity = getById(activityId);
        if (activity == null || activity.getState() == 3) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "活动不存在");
        }
        if (activity.getState() == 2) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "活动已结束");
        }
        if (activity.getState() == 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "活动未发布");
        }

        Long currentUserId = SecurityUtil.getCurrentUserId();
        ActivityParticipation exist = participationMapper.selectOne(new LambdaQueryWrapper<ActivityParticipation>()
                .eq(ActivityParticipation::getActivityId, activityId)
                .eq(ActivityParticipation::getUserId, currentUserId));
        if (exist != null && exist.getState() == 1) {
            throw new BusinessException(ResultCode.DATA_EXISTS, "已报名该活动");
        }
        if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0
                && activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "名额已满");
        }

        if (exist == null) {
            ActivityParticipation participation = new ActivityParticipation();
            participation.setActivityId(activityId);
            participation.setUserId(currentUserId);
            participation.setState(1);
            participationMapper.insert(participation);
        } else {
            exist.setState(1);
            exist.setCheckinTime(null);
            participationMapper.updateById(exist);
        }

        // 更新人数
        activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
        updateById(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSignup(Long activityId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ActivityParticipation participation = participationMapper.selectOne(new LambdaQueryWrapper<ActivityParticipation>()
                .eq(ActivityParticipation::getActivityId, activityId)
                .eq(ActivityParticipation::getUserId, currentUserId)
                .eq(ActivityParticipation::getState, 1));
        if (participation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "未报名该活动");
        }
        participation.setState(3);
        participationMapper.updateById(participation);

        Activity activity = getById(activityId);
        if (activity != null && activity.getCurrentParticipants() > 0) {
            activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
            updateById(activity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long activityId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ActivityParticipation participation = participationMapper.selectOne(new LambdaQueryWrapper<ActivityParticipation>()
                .eq(ActivityParticipation::getActivityId, activityId)
                .eq(ActivityParticipation::getUserId, currentUserId)
                .eq(ActivityParticipation::getState, 1));
        if (participation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "未报名该活动");
        }
        participation.setState(2);
        participation.setCheckinTime(LocalDateTime.now());
        participationMapper.updateById(participation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoFinishExpired(LocalDateTime now) {
        List<Activity> needFinish = list(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getState, 1)
                .lt(Activity::getEndTime, now));
        if (needFinish.isEmpty()) {
            return;
        }
        needFinish.forEach(a -> a.setState(2));
        updateBatchById(needFinish);
    }

    private void validateTime(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "结束时间需晚于开始时间");
        }
    }
}

