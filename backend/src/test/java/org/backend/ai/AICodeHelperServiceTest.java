package org.backend.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class AICodeHelperServiceTest {

    @Resource
    private AICodeHelperService aiCodeHelperService;

    @Test
    void chat() {
        String result=aiCodeHelperService.chat("user1", "你好，AI！请介绍一下你自己。");
        System.out.println(result);
    }

    @Test
    void chatWithMemory() {
        String result=aiCodeHelperService.chat("user1", "你好，我是yby");
        System.out.println(result);
        result=aiCodeHelperService.chat("user1", "你能否根据之前的对话内容，介绍一下我吗？");
        System.out.println(result);
    }


    @Test
    void chatWithUserMessage() {
    }

    @Test
    void testChatWithRag() {
        String result=aiCodeHelperService.chatWithRag("user1", "我想了解一下Java中的Stream API。");
        System.out.println(result);
        result=aiCodeHelperService.chatWithRag("user1", "能否给我一些示例代码吗？");
        System.out.println(result);
        result=aiCodeHelperService.chatWithRag("user1", "你能否根据之前的对话内容，介绍一下Stream API吗？");
        System.out.println(result);
    }

    @Test
    void chatWithTools() {
        String result=aiCodeHelperService.chat("user1", "使用工具帮我搜索一下有哪些常见的计算机网络面试题？");
        System.out.println(result);
    }

    @Test
    void chatWithMcp()
    {
        String result=aiCodeHelperService.chat("user1", "什么是程序员鱼皮的编程导航");
        System.out.println(result);
    }
}