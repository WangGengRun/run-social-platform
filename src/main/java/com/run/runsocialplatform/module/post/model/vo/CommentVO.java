package com.run.runsocialplatform.module.post.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO
 */
@Data
@Schema(description = "评论信息")
public class CommentVO {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "动态ID")
    private Long postId;

    @Schema(description = "评论者用户ID")
    private Long userId;

    @Schema(description = "评论者用户名")
    private String username;

    @Schema(description = "评论者真实姓名")
    private String realName;

    @Schema(description = "评论者头像")
    private String avatar;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID，0表示直接评论动态")
    private Long parentId;

    @Schema(description = "父评论信息（回复时显示）")
    private CommentVO parentComment;

    @Schema(description = "回复列表")
    private List<CommentVO> replies;

    @Schema(description = "是否是本人评论")
    private Boolean isSelf = false;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}

