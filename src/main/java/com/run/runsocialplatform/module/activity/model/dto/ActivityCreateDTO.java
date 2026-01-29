package com.run.runsocialplatform.module.activity.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "创建活动请求")
public class ActivityCreateDTO {

    @NotBlank(message = "活动标题不能为空")
    @Schema(description = "活动标题", example = "校友交流沙龙")
    private String title;

    @Schema(description = "活动描述")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImage;

    @NotBlank(message = "活动地点不能为空")
    @Schema(description = "活动地点，如“线上”或具体地点")
    private String location;

    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间需晚于当前时间")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间需晚于当前时间")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "最大参与人数，0表示不限制", example = "0")
    private Integer maxParticipants = 0;
}

