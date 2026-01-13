package com.run.runsocialplatform.websocket.config;

import com.run.runsocialplatform.websocket.handler.WebSocketMessageHandler;
import com.run.runsocialplatform.websocket.interceptor.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketMessageHandler messageHandler;
    private final WebSocketAuthInterceptor authInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageHandler, "/ws/message")
                .addInterceptors(authInterceptor)
                .setAllowedOrigins("*"); // 生产环境应该配置具体域名
    }
}

