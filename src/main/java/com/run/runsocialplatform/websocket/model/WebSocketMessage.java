package com.run.runsocialplatform.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket消息模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    /**
     * 消息类型：
     * - connected: 连接成功
     * - ping: 心跳
     * - pong: 心跳响应
     * - new_message: 新私信
     * - message_read: 消息已读
     * - error: 错误
     */
    private String type;

    /**
     * 消息数据（JSON字符串或对象）
     */
    private Object data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public WebSocketMessage(String type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
}

