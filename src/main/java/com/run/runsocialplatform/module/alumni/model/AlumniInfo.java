package com.run.runsocialplatform.module.alumni.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("alumni_info")
public class AlumniInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String realName;

    private String studentId;

    private Integer admissionYear;

    private Integer graduationYear;

    private String college;

    private String major;

    private String company;

    private String position;

    private String city;

    private String bio;

    private Integer verifyStatus;

    private String verifyNotes;

    private LocalDateTime verifyTime;

    private Long verifyAdminId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联的用户信息（非数据库字段）
    @TableField(exist = false)
    private String avatar;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String email;
}