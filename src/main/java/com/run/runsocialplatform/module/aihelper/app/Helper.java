package com.run.runsocialplatform.module.aihelper.app;

import com.run.runsocialplatform.module.aihelper.advisor.MyLoggerAdvisor;
import com.run.runsocialplatform.module.aihelper.chatmemory.FileBasedChatMemory;
import com.run.runsocialplatform.module.aihelper.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

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

    /**
     * 初始化AI Client
     * @param dashscopeChatModel
     */
    public Helper(ChatModel dashscopeChatModel) {
        String fileDir =System.getProperty("user.dir")+"/chat-memory";
        //初始化基于文件的对话记忆
        FileBasedChatMemory chatMemory=new FileBasedChatMemory(fileDir);
        //初始化基于内存的对话记忆
//        ChatMemory chatMemory=new InMemoryChatMemory();
        //拦截器对所有请求生效，也可以在单次请求创建拦截器
        chatClient=ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        //自定义日志拦截器
                        new MyLoggerAdvisor()
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
    //AI恋爱知识库问答功能
    @Resource
    private VectorStore helperAppVectorStore;
    @Resource
    private QueryRewriter queryRewriter;
    /**
     * 和RAG知识库进行对话
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        //查询重写
        String rewritenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                //使用改写后的查询
                .user(rewritenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                //应用RAG知识库问答
                .advisors(new QuestionAnswerAdvisor(helperAppVectorStore))
                //应用RAG检索增强服务（基于云知识库服务）
//                .advisors(loveAppRagCloudAdvisor)
                //应用RAG检索增强服务（基于PgVector向量存储）
//                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI 流式对话（边生成边返回）
     */
    public Flux<String> doChatStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }

    /**
     * AI + RAG 流式对话（边检索边生成边返回）
     */
    public Flux<String> doChatWithRagStream(String message, String chatId) {
        String rewritenMessage = queryRewriter.doQueryRewrite(message);
        return chatClient
                .prompt()
                .user(rewritenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .advisors(new QuestionAnswerAdvisor(helperAppVectorStore))
                .stream()
                .content();
    }



}
