package com.run.runsocialplatform.module.message.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 消息测试控制器
 * 提供测试页面访问
 */
@Controller
@Tag(name = "消息测试", description = "WebSocket测试页面")
public class MessageTestController {

    @GetMapping("/websocket-test")
    @Operation(summary = "WebSocket测试页面")
    public String websocketTest() {
        return "forward:/websocket-test.html";
    }
}

