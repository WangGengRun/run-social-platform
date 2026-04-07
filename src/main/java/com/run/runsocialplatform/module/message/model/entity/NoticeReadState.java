package com.run.runsocialplatform.module.message.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息中心阅读状态（每用户一行）
 */
@Data
@TableName("notice_read_state")
public class NoticeReadState {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDateTime lastReadAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

