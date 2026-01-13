package com.run.runsocialplatform.websocket.interceptor;

import com.run.runsocialplatform.module.auth.service.UserService;
import com.run.runsocialplatform.security.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * WebSocket认证拦截器
 * 在建立连接前验证JWT Token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            // 从查询参数或请求头获取Token
            URI uri = request.getURI();
            String query = uri.getQuery();
            
            String token = null;
            if (query != null && query.contains("token=")) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("token=")) {
                        token = param.substring(6);
                        break;
                    }
                }
            }
            
            // 如果查询参数没有，尝试从请求头获取
            if (token == null || token.isEmpty()) {
                String authHeader = request.getHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            if (token == null || token.isEmpty()) {
                log.warn("WebSocket连接失败：未提供Token");
                return false;
            }

            // 验证Token
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                log.warn("WebSocket连接失败：Token无效");
                return false;
            }

            // 通过username查询userId
            var user = userService.findByUsername(username);
            if (user == null || user.getStatus() != 1) {
                log.warn("WebSocket连接失败：用户不存在或已被禁用");
                return false;
            }
            Long userId = user.getId();

            // 将用户信息存储到attributes中，供WebSocketHandler使用
            attributes.put("userId", userId);
            attributes.put("username", username);
            attributes.put("token", token);

            log.info("WebSocket连接认证成功，用户ID: {}, 用户名: {}", userId, username);
            return true;

        } catch (Exception e) {
            log.error("WebSocket连接认证失败：{}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手后的处理，可以在这里记录日志等
        if (exception != null) {
            log.error("WebSocket握手后发生异常：{}", exception.getMessage());
        }
    }
}

