package com.run.runsocialplatform.module.activity.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "活动详情")
public class ActivityDetailVO {

    @Schema(description = "活动ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "地点")
    private String location;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "组织者ID")
    private Long organizerId;

    @Schema(description = "组织者姓名")
    private String organizerName;

    @Schema(description = "组织者头像")
    private String organizerAvatar;

    @Schema(description = "最大人数，0表示不限制")
    private Integer maxParticipants;

    @Schema(description = "当前人数")
    private Integer currentParticipants;

    @Schema(description = "状态：0-待发布,1-进行中,2-已结束,3-已取消")
    private Integer status;

    @Schema(description = "是否已报名")
    private Boolean joined = false;
}

