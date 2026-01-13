package com.run.runsocialplatform.websocket.service;

import com.run.runsocialplatform.websocket.model.WebSocketMessage;

/**
 * WebSocket消息服务接口
 */
public interface WebSocketMessageService {

    /**
     * 发送私信消息给指定用户
     */
    void sendPrivateMessage(Long receiverId, Object messageData);

    /**
     * 发送消息给指定用户的所有会话
     */
    void sendToUser(Long userId, WebSocketMessage message);

    /**
     * 广播消息给所有在线用户
     */
    void broadcast(WebSocketMessage message);
}

