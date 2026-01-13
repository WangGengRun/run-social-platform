package com.run.runsocialplatform.module.message.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话VO
 */
@Data
@Schema(description = "会话信息")
public class ConversationVO {

    @Schema(description = "对方用户ID")
    private Long userId;

    @Schema(description = "对方用户名")
    private String username;

    @Schema(description = "对方真实姓名")
    private String realName;

    @Schema(description = "对方头像")
    private String avatar;

    @Schema(description = "最后一条消息内容")
    private String lastMessage;

    @Schema(description = "最后一条消息类型")
    private Integer lastMessageType;

    @Schema(description = "未读消息数")
    private Long unreadCount;

    @Schema(description = "最后消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "是否已读")
    private Boolean isRead;
}

