package com.run.runsocialplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.run.runsocialplatform.module.**.mapper") // 单个包路径
public class RunSocialPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(RunSocialPlatformApplication.class, args);
    }
}
