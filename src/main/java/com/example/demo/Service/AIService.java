package com.example.demo.Service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AIService {
    private final ChatClient chatClient;

    // 构造器注入 ChatClient，这是 Spring AI 的标准用法[reference:6]
    public AIService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    // 非流式调用，简单直接
    public String chat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    // 流式调用，实现“打字机”效果
    public Flux<String> streamChat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .stream()
                .content();
    }
}
