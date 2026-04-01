package com.run.runsocialplatform.common.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置类，注入MinioClient客户端
 */
@Configuration
public class MinioConfig {

    // 从application.yml中读取配置项
    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    /**
     * 初始化MinioClient，注入Spring容器
     * 后续业务代码中直接@Autowired即可使用
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint) // 设置MinIO服务地址
                .credentials(accessKey, secretKey) // 设置访问密钥和密钥
                .build();
    }
}