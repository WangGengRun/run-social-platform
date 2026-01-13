package com.run.runsocialplatform.websocket.service.impl;

import com.alibaba.fastjson2.JSON;
import com.run.runsocialplatform.websocket.model.WebSocketMessage;
import com.run.runsocialplatform.websocket.service.WebSocketMessageService;
import com.run.runsocialplatform.websocket.service.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * WebSocket消息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessageServiceImpl implements WebSocketMessageService {

    private final WebSocketSessionManager sessionManager;

    @Override
    public void sendPrivateMessage(Long receiverId, Object messageData) {
        WebSocketMessage message = WebSocketMessage.builder()
                .type("new_message")
                .data(messageData)
                .timestamp(System.currentTimeMillis())
                .build();
        sendToUser(receiverId, message);
    }

    @Override
    public void sendToUser(Long userId, WebSocketMessage message) {
        Map<String, WebSocketSession> sessions = sessionManager.getUserSessions(userId);
        
        if (sessions.isEmpty()) {
            log.debug("用户不在线，无法推送消息，用户ID: {}", userId);
            return;
        }

        String json = JSON.toJSONString(message);
        TextMessage textMessage = new TextMessage(json);

        int successCount = 0;
        int failCount = 0;

        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            WebSocketSession session = entry.getValue();
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                    successCount++;
                } else {
                    // 会话已关闭，移除
                    sessionManager.removeSession(userId, session);
                    failCount++;
                }
            } catch (Exception e) {
                log.error("发送WebSocket消息失败，用户ID: {}, 会话ID: {}, 错误: {}", 
                        userId, entry.getKey(), e.getMessage());
                failCount++;
            }
        }

        log.info("推送消息给用户，用户ID: {}, 成功: {}, 失败: {}", userId, successCount, failCount);
    }

    @Override
    public void broadcast(WebSocketMessage message) {
        // 广播给所有在线用户（如果需要的话）
        // 这里暂时不实现，因为私信不需要广播
        log.warn("广播功能暂未实现");
    }
}

