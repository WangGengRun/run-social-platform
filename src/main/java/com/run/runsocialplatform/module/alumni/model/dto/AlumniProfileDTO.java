package com.run.runsocialplatform.module.alumni.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "校友档案更新DTO")
public class AlumniProfileDTO {

    @Schema(description = "真实姓名")
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Schema(description = "学号")
//    @NotBlank(message = "学号不能为空")
    private String studentId;

    @Schema(description = "入学年份")
    @NotNull(message = "入学年份不能为空")
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
}