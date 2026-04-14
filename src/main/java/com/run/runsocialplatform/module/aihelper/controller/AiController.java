package com.run.runsocialplatform.module.aihelper.controller;

import com.run.runsocialplatform.common.result.Result;
import com.run.runsocialplatform.module.aihelper.app.Helper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {
    @Resource
    private Helper helper;

    /**
     * 同步调用校友助手应用
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/app/chat/sync")
    @PreAuthorize("isAuthenticated()")
    public Result<ChatResponse> doChatWithLoveAppSync(String message, String chatId) {
        String finalChatId = StringUtils.hasText(chatId) ? chatId : UUID.randomUUID().toString();
        String answer = helper.doChatWithRag(message, finalChatId);
        return Result.success(new ChatResponse(finalChatId, answer));
    }

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String finalChatId = StringUtils.hasText(request.getChatId()) ? request.getChatId() : UUID.randomUUID().toString();
        boolean useRag = request.getUseRag() == null || request.getUseRag();
        String answer = useRag
                ? helper.doChatWithRag(request.getMessage(), finalChatId)
                : helper.doChat(request.getMessage(), finalChatId);
        return Result.success(new ChatResponse(finalChatId, answer));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter chatStream(@Valid @RequestBody ChatRequest request) {
        String finalChatId = StringUtils.hasText(request.getChatId()) ? request.getChatId() : UUID.randomUUID().toString();
        boolean useRag = request.getUseRag() == null || request.getUseRag();

        SseEmitter emitter = new SseEmitter(0L);

        try {
            emitter.send(SseEmitter.event().name("meta").data("{\"chatId\":\"" + finalChatId + "\"}"));
        } catch (IOException e) {
            emitter.completeWithError(e);
            return emitter;
        }

        var flux = useRag
                ? helper.doChatWithRagStream(request.getMessage(), finalChatId)
                : helper.doChatStream(request.getMessage(), finalChatId);

        flux.subscribe(
                chunk -> {
                    try {
                        emitter.send(SseEmitter.event().name("delta").data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                err -> {
                    try {
                        emitter.send(SseEmitter.event().name("error").data(err.getMessage() == null ? "AI助手暂时不可用" : err.getMessage()));
                    } catch (IOException ignored) {
                        // ignore
                    } finally {
                        emitter.completeWithError(err);
                    }
                },
                () -> {
                    try {
                        emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                    } catch (IOException ignored) {
                        // ignore
                    } finally {
                        emitter.complete();
                    }
                }
        );

        return emitter;
    }

    @Data
    public static class ChatRequest {
        @NotBlank(message = "message不能为空")
        private String message;
        private String chatId;
        private Boolean useRag;
    }

    @Data
    public static class ChatResponse {
        private String chatId;
        private String answer;

        public ChatResponse(String chatId, String answer) {
            this.chatId = chatId;
            this.answer = answer;
        }
    }

}
