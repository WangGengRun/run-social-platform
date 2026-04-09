package com.run.runsocialplatform.module.alumni.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "校友档案更新DTO")
public class AlumniProfileDTO {

    @Schema(description = "公司")
    private String company;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;
}