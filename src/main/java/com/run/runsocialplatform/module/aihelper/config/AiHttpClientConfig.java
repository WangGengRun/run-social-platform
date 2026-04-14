package com.run.runsocialplatform.module.aihelper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Spring AI Alibaba DashScope 默认超时偏短，复杂回答/RAG 容易触发 read timeout。
 * 通过注入 RestClient.Builder 统一延长 connect/read timeout。
 */
@Configuration
public class AiHttpClientConfig {

    @Bean
    public RestClient.Builder restClientBuilderProvider() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(30));
        factory.setReadTimeout(Duration.ofSeconds(120));
        return RestClient.builder().requestFactory(factory);
    }
}

