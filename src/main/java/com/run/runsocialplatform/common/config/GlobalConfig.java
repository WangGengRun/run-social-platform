package com.run.runsocialplatform.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

@Configuration
public class GlobalConfig {

    /**
     * 方案二：使用Spring Boot的自动配置特性
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            // 设置日期时间格式
            builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 配置Java 8日期时间序列化
            builder.serializers(
                    new com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    )
            );
            builder.serializers(
                    new com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    )
            );
            builder.serializers(
                    new com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer(
                            DateTimeFormatter.ofPattern("HH:mm:ss")
                    )
            );

            // 配置Java 8日期时间反序列化
            builder.deserializers(
                    new com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    )
            );
            builder.deserializers(
                    new com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    )
            );
            builder.deserializers(
                    new com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer(
                            DateTimeFormatter.ofPattern("HH:mm:ss")
                    )
            );
        };
    }
}