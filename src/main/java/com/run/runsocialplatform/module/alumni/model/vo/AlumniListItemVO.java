package com.run.runsocialplatform.module.alumni.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "校友列表项VO")
public class AlumniListItemVO {

    @Schema(description = "校友ID")
    private Long alumniId;

    @Schema(description = "用户ID")
    private Long userId;

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

    @Schema(description = "是否关注")
    private Boolean isFollowed = false;

    @Schema(description = "动态数量")
    private Integer postCount = 0;

    @Schema(description = "关注者数量")
    private Integer followerCount = 0;

    @Schema(description = "是否已毕业")
    public Boolean getIsGraduated() {
        return graduationYear != null && graduationYear > 0;
    }

    @Schema(description = "在校年数")
    public Integer getSchoolYears() {
        if (graduationYear != null && admissionYear != null) {
            return graduationYear - admissionYear;
        }
        return null;
    }
}