package com.run.runsocialplatform.module.message.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送私信DTO
 */
@Data
@Schema(description = "发送私信请求参数")
public class MessageSendDTO {

    @NotNull(message = "接收者ID不能为空")
    @Schema(description = "接收者用户ID")
    private Long receiverId;

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型：1-文本, 2-图片, 3-文件", example = "1")
    private Integer messageType = 1;
}

