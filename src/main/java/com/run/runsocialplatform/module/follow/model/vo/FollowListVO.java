package com.run.runsocialplatform.module.follow.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 关注列表项VO
 */
@Data
@Schema(description = "关注列表项")
public class FollowListVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "校友ID")
    private Long alumniId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "入学年份")
    private Integer admissionYear;

    @Schema(description = "毕业年份")
    private Integer graduationYear;

    @Schema(description = "学院")
    private String college;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "公司")
    private String company;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "认证状态")
    private Integer verifyStatus;

    @Schema(description = "是否互相关注")
    private Boolean isMutualFollow = false;

    @Schema(description = "关注时间")
    private LocalDateTime followTime;
}

