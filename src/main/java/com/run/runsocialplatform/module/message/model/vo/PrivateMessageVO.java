package com.run.runsocialplatform.module.message.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 私信VO
 */
@Data
@Schema(description = "私信信息")
public class PrivateMessageVO {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "发送者用户名")
    private String senderUsername;

    @Schema(description = "发送者真实姓名")
    private String senderRealName;

    @Schema(description = "发送者头像")
    private String senderAvatar;

    @Schema(description = "接收者ID")
    private Long receiverId;

    @Schema(description = "接收者用户名")
    private String receiverUsername;

    @Schema(description = "接收者真实姓名")
    private String receiverRealName;

    @Schema(description = "接收者头像")
    private String receiverAvatar;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型：1-文本, 2-图片, 3-文件")
    private Integer messageType;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    @Schema(description = "是否是本人发送的")
    private Boolean isSelf = false;

    @Schema(description = "发送时间")
    private LocalDateTime createdAt;
}

