package com.ai.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void testChat() {
        String chatId= UUID.randomUUID().toString();

        String message="你好";
        String answer= loveApp.doChat(message,chatId);

        message="我想让另一半（aa）更爱我";
        answer= loveApp.doChat(message,chatId);
        Assertions.assertNotNull(answer);

        message="告诉我我的另一半叫什么";
        answer= loveApp.doChat(message,chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId= UUID.randomUUID().toString();
        String message="你好，我是xxx，我想让另一半（aaa）更爱我，但我不知道该怎么做";
        LoveApp.LoveReport loveReport= loveApp.doChatWithReport(message,chatId);
        Assertions.assertNotNull(loveReport);
    }
}