package com.run.runsocialplatform.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话管理器
 * 管理用户ID与WebSocket会话的映射关系
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /**
     * 用户ID -> WebSocket会话集合（一个用户可能有多个设备同时在线）
     */
    private final Map<Long, ConcurrentHashMap<String, WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    /**
     * 添加会话
     */
    public void addSession(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(session.getId(), session);
        log.debug("添加WebSocket会话，用户ID: {}, 会话ID: {}, 当前会话数: {}", 
                userId, session.getId(), getUserSessionCount(userId));
    }

    /**
     * 移除会话
     */
    public void removeSession(Long userId, WebSocketSession session) {
        Map<String, WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session.getId());
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
        log.debug("移除WebSocket会话，用户ID: {}, 会话ID: {}, 当前会话数: {}", 
                userId, session.getId(), getUserSessionCount(userId));
    }

    /**
     * 获取用户的所有会话
     */
    public Map<String, WebSocketSession> getUserSessions(Long userId) {
        return userSessions.getOrDefault(userId, new ConcurrentHashMap<>());
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        Map<String, WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * 获取用户的会话数量
     */
    public int getUserSessionCount(Long userId) {
        Map<String, WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * 获取所有在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }
}

