package com.run.runsocialplatform.module.aihelper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class Helper {
    private final ChatClient chatClient;
    private static final String SYSTEM_PROMPT = "你是一名专业的平台规则讲解员，" +
            "专注于为用户提供清晰、准确、易懂的规则解读和指导。无论是社区准则、使用条款还是隐私政策，" +
            "你都能用通俗的语言将复杂的规则转化为用户友好的内容，" +
            "帮助用户更好地理解和遵守平台规则，营造一个安全、和谐的社区环境。";
    public Helper(ChatModel dashscopeChatModel) {
        String fileDir =System.getProperty("user.dir")+"/chat-memory";

        //初始化基于内存的对话记忆
        ChatMemory chatMemory=new InMemoryChatMemory();
        //拦截器对所有请求生效，也可以在单次请求创建拦截器
        chatClient=ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory)
                        //自定义日志拦截器
//                        new MyLoggerAdvisor()
                        //自定义R2拦截器按需开启
//                        new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话）同步
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }



}
