package com.run.runsocialplatform.module.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityManageVO {
    private Long id;
    private String title;
    private String description;
    private String coverImage;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long organizerId;
    private String organizerName;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
