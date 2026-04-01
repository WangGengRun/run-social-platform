package com.run.runsocialplatform.module.aihelper;


import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
@SpringBootTest
class HelperTest {

    @Resource
    private Helper helper;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();

        //第一轮
        String message = "你都能做些什么？";
        String answer = helper.doChat(message, chatId);
        Assertions.assertNotNull(answer);


    }
}