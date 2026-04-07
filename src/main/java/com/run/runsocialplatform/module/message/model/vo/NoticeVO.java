package com.run.runsocialplatform.module.message.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息中心通知项
 */
@Data
@Schema(description = "消息中心通知项")
public class NoticeVO {

    @Schema(description = "来源记录ID（点赞/评论为 post_interaction.id，关注为 follow.id）")
    private Long sourceId;

    @Schema(description = "通知类型：LIKE/COMMENT/FOLLOW")
    private String noticeType;

    @Schema(description = "触发用户ID")
    private Long fromUserId;

    @Schema(description = "触发用户名")
    private String fromUsername;

    @Schema(description = "触发用户真实姓名")
    private String fromRealName;

    @Schema(description = "触发用户头像（MinIO objectName 或 URL）")
    private String fromAvatar;

    @Schema(description = "关联动态ID（关注通知为空）")
    private Long postId;

    @Schema(description = "动态内容摘要（关注通知为空）")
    private String postContent;

    @Schema(description = "评论内容（仅 COMMENT 有）")
    private String commentContent;

    @Schema(description = "通知时间")
    private LocalDateTime createdAt;

    @Schema(description = "是否已读（由服务端根据最后阅读时间计算）")
    private Boolean isRead;
}

