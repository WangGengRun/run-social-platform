package com.run.runsocialplatform.module.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAlumniInfoVO {
    private Long id;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
