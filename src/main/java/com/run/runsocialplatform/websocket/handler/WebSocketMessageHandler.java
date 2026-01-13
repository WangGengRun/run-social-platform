package com.run.runsocialplatform.websocket.handler;

import com.alibaba.fastjson2.JSON;
import com.run.runsocialplatform.websocket.model.WebSocketMessage;
import com.run.runsocialplatform.websocket.service.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * WebSocket消息处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketMessageHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        Long userId = (Long) attributes.get("userId");
        String username = (String) attributes.get("username");

        if (userId != null) {
            sessionManager.addSession(userId, session);
            log.info("WebSocket连接建立，用户ID: {}, 用户名: {}, 会话ID: {}", 
                    userId, username, session.getId());
            
            // 发送连接成功消息
            WebSocketMessage connectMsg = WebSocketMessage.builder()
                    .type("connected")
                    .data("连接成功")
                    .build();
            sendMessage(session, connectMsg);
        } else {
            log.warn("WebSocket连接建立失败：用户ID为空");
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        Long userId = (Long) attributes.get("userId");

        if (userId == null) {
            log.warn("收到消息但用户ID为空，会话ID: {}", session.getId());
            return;
        }

        try {
            // 解析消息
            WebSocketMessage wsMessage = JSON.parseObject(message.getPayload(), WebSocketMessage.class);
            log.debug("收到WebSocket消息，用户ID: {}, 类型: {}, 内容: {}", 
                    userId, wsMessage.getType(), wsMessage.getData());

            // 处理不同类型的消息
            handleMessage(userId, wsMessage, session);

        } catch (Exception e) {
            log.error("处理WebSocket消息失败：{}", e.getMessage(), e);
            WebSocketMessage errorMsg = WebSocketMessage.builder()
                    .type("error")
                    .data("消息处理失败: " + e.getMessage())
                    .build();
            sendMessage(session, errorMsg);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        Long userId = (Long) attributes.get("userId");

        if (userId != null) {
            sessionManager.removeSession(userId, session);
            log.info("WebSocket连接关闭，用户ID: {}, 会话ID: {}, 状态: {}", 
                    userId, session.getId(), status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        Long userId = (Long) attributes.get("userId");
        log.error("WebSocket传输错误，用户ID: {}, 会话ID: {}, 错误: {}", 
                userId, session.getId(), exception.getMessage());
        
        if (userId != null) {
            sessionManager.removeSession(userId, session);
        }
    }

    /**
     * 处理收到的消息
     */
    private void handleMessage(Long userId, WebSocketMessage message, WebSocketSession session) {
        String type = message.getType();

        switch (type) {
            case "ping":
                // 心跳消息，回复pong
                WebSocketMessage pong = WebSocketMessage.builder()
                        .type("pong")
                        .data("pong")
                        .timestamp(System.currentTimeMillis())
                        .build();
                sendMessage(session, pong);
                break;

            default:
                log.warn("未知的WebSocket消息类型: {}", type);
                WebSocketMessage error = WebSocketMessage.builder()
                        .type("error")
                        .data("未知的消息类型: " + type)
                        .timestamp(System.currentTimeMillis())
                        .build();
                sendMessage(session, error);
                break;
        }
    }

    /**
     * 发送消息给指定会话
     */
    public void sendMessage(WebSocketSession session, WebSocketMessage message) {
        try {
            if (session.isOpen()) {
                String json = JSON.toJSONString(message);
                session.sendMessage(new TextMessage(json));
            }
        } catch (Exception e) {
            log.error("发送WebSocket消息失败：{}", e.getMessage(), e);
        }
    }
}

