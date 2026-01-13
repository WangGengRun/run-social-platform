package com.run.runsocialplatform.module.message.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未读消息数VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "未读消息统计")
public class UnreadCountVO {

    @Schema(description = "总未读消息数")
    private Long totalUnreadCount;

    @Schema(description = "与指定用户的未读消息数（查询会话详情时使用）")
    private Long userUnreadCount;
}

