package com.run.runsocialplatform.websocket.handler;

import com.alibaba.fastjson2.JSON;
import com.run.runsocialplatform.common.exception.BusinessException;
import com.run.runsocialplatform.websocket.model.WebSocketMessage;
import com.run.runsocialplatform.websocket.service.WebSocketMessageService;
import com.run.runsocialplatform.websocket.service.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.run.runsocialplatform.module.message.service.PrivateMessageService;
import com.run.runsocialplatform.module.message.model.dto.MessageSendDTO;
import java.util.Map;

/**
 * WebSocket消息处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketMessageHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final PrivateMessageService messageService;  // 添加这行
    private final WebSocketMessageService webSocketMessageService;
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
                WebSocketMessage pong = WebSocketMessage.builder()
                        .type("pong")
                        .data("pong")
                        .timestamp(System.currentTimeMillis())
                        .build();
                sendMessage(session, pong);
                break;

            case "message":
                handleSendMessage(userId, message.getData(), session);
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
    private void handleSendMessage(Long userId, Object data, WebSocketSession session) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> messageData = (Map<String, Object>) data;

            Long receiverId = Long.valueOf(messageData.get("receiverId").toString());
            String content = messageData.get("content").toString();
            Integer messageType = messageData.get("messageType") != null
                    ? Integer.valueOf(messageData.get("messageType").toString())
                    : 1;
            String clientMessageId = messageData.get("clientMessageId") != null
                    ? messageData.get("clientMessageId").toString()
                    : null;

            MessageSendDTO sendDTO = new MessageSendDTO();
            sendDTO.setReceiverId(receiverId);
            sendDTO.setContent(content);
            sendDTO.setMessageType(messageType);

            // 直接传入 userId（WebSocket 握手时获取的发送者ID）
            Long messageId = messageService.sendMessage(userId, sendDTO);

            // 发送成功确认
            WebSocketMessage ack = WebSocketMessage.builder()
                    .type("message_ack")
                    .data(Map.of(
                            "clientMessageId", clientMessageId != null ? clientMessageId : "",
                            "messageId", messageId,
                            "success", true
                    ))
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, ack);

            log.info("WebSocket发送私信成功，消息ID: {}, 发送者: {}, 接收者: {}",
                    messageId, userId, receiverId);

        } catch (Exception e) {
            log.error("WebSocket发送私信失败：{}", e.getMessage(), e);

            WebSocketMessage ack = WebSocketMessage.builder()
                    .type("message_ack")
                    .data(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ))
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, ack);
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

