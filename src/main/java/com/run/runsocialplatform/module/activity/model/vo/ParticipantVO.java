package com.run.runsocialplatform.module.activity.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "活动参与者信息")
public class ParticipantVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "状态：1-报名中,2-已参加,3-已取消")
    private Integer status;

    @Schema(description = "报名时间")
    private LocalDateTime createdAt;

    @Schema(description = "签到时间")
    private LocalDateTime checkinTime;
}

