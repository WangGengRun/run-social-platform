package com.run.runsocialplatform.module.auth.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "校友身份验证请求参数")
public class AlumniVerifyDTO {

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @NotBlank(message = "学号不能为空")
    @Schema(description = "学号", example = "201801001")
    private String studentId;

    @NotNull(message = "入学年份不能为空")
    @Schema(description = "入学年份", example = "2018")
    private Integer admissionYear;

    @Schema(description = "毕业年份", example = "2022")
    private Integer graduationYear;

    @Schema(description = "学院", example = "计算机学院")
    private String college;

    @Schema(description = "专业", example = "计算机科学与技术")
    private String major;

    @Schema(description = "公司", example = "阿里巴巴")
    private String company;

    @Schema(description = "职位", example = "高级工程师")
    private String position;

    @Schema(description = "城市", example = "杭州")
    private String city;

    @Size(max = 500, message = "个人简介不能超过500字")
    @Schema(description = "个人简介", example = "热爱技术，喜欢分享")
    private String bio;
}