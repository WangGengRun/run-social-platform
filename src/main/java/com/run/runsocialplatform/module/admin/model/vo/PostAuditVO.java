package com.run.runsocialplatform.module.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostAuditVO {
    private Long id;
    private Long userId;
    private String username;
    private String content;
    private String imageUrls;
    private Integer visibility;
    private Integer likeCount;
    private Integer commentCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
