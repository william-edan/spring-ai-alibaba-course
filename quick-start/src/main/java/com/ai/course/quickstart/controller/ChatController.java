package com.ai.course.quickstart.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个友好的AI助手。")
                .build();
    }

    /**
     * 同步对话
     */
    @GetMapping
    public String chat(@RequestParam(value = "message", defaultValue = "你好") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 流式对话
     */
    @GetMapping(value = "/stream", produces = "text/html;charset=UTF-8")
    public Flux<String> chatStream(@RequestParam(value = "message", defaultValue = "你好") String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}
