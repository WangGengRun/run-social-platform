package com.run.runsocialplatform.module.auth.model.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("alumni_info")
public class AlumniInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("real_name")
    private String realName;

    @TableField("student_id")
    private String studentId;

    @TableField("admission_year")
    private Integer admissionYear;

    @TableField("graduation_year")
    private Integer graduationYear;

    @TableField("college")
    private String college;

    @TableField("major")
    private String major;

    @TableField("company")
    private String company;

    @TableField("position")
    private String position;

    @TableField("city")
    private String city;

    @TableField("bio")
    private String bio;

    @TableField("verify_status")
    private Integer verifyStatus;

    @TableField("verify_notes")
    private String verifyNotes;

    @TableField("verify_time")
    private LocalDateTime verifyTime;

    @TableField("verify_admin_id")
    private Long verifyAdminId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}