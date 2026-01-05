package com.run.runsocialplatform.module.alumni.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "校友档案VO")
public class AlumniProfileVO {

    @Schema(description = "校友ID")
    private Long alumniId;

    @Schema(description = "用户ID")
    private Long userId;

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

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "认证状态：0-待审核, 1-审核通过, 2-审核不通过")
    private Integer verifyStatus;

    @Schema(description = "是否关注该校友")
    private Boolean isFollowed = false;

    @Schema(description = "是否是本人")
    private Boolean isSelf = false;

    @Schema(description = "动态数量")
    private Integer postCount = 0;

    @Schema(description = "关注者数量")
    private Integer followerCount = 0;

    @Schema(description = "关注数量")
    private Integer followingCount = 0;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}