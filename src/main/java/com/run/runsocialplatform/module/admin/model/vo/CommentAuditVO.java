package com.run.runsocialplatform.module.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentAuditVO {
    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String content;
    private Long parentId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
