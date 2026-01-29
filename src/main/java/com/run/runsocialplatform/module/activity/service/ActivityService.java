package com.run.runsocialplatform.module.activity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.run.runsocialplatform.module.activity.model.dto.ActivityCreateDTO;
import com.run.runsocialplatform.module.activity.model.dto.ActivityUpdateDTO;
import com.run.runsocialplatform.module.activity.model.entity.Activity;
import com.run.runsocialplatform.module.activity.model.vo.ActivityDetailVO;
import com.run.runsocialplatform.module.activity.model.vo.ActivityListVO;
import com.run.runsocialplatform.module.activity.model.vo.ParticipantVO;

import java.time.LocalDateTime;

public interface ActivityService extends IService<Activity> {

    Long create(ActivityCreateDTO dto);

    void update(Long activityId, ActivityUpdateDTO dto);

    void publish(Long activityId);

    void cancel(Long activityId);

    ActivityDetailVO detail(Long activityId);

    Page<ActivityListVO> listActivities(Integer pageNum, Integer pageSize, Integer status, String keyword);

    Page<ActivityListVO> listMyOrganized(Integer pageNum, Integer pageSize);

    Page<ParticipantVO> listParticipants(Long activityId, Integer pageNum, Integer pageSize);

    void signup(Long activityId);

    void cancelSignup(Long activityId);

    void checkIn(Long activityId);

    void autoFinishExpired(LocalDateTime now);
}

