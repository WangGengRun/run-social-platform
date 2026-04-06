package com.run.runsocialplatform.module.activity.schedule;

import com.run.runsocialplatform.module.activity.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 定时将「进行中且 end_time 已过」的活动更新为已结束（status=2），与查询入口的 {@link ActivityService#autoFinishExpired} 一致。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityEndedStateScheduler {

    private final ActivityService activityService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void syncEndedState() {
        try {
            activityService.autoFinishExpired(LocalDateTime.now());
        } catch (Exception e) {
            log.warn("定时同步活动结束状态失败: {}", e.getMessage());
        }
    }
}
