package com.ai.course.chatclient.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/chat")
public class ChatClientController {

    // 注入 ChatClientConfig 中配置好的 ChatClient（已带 Advisor 链）
    private final ChatClient chatClient;

    public ChatClientController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 最简调用——一行代码完成对话
     * GET /api/v2/chat/simple?q=北京到上海的机票
     */
    @GetMapping("/simple")
    public String simple(@RequestParam("q") String q) {
        return chatClient.prompt(q).call().content();
    }

    /**
     * 动态覆盖 System Prompt
     * GET /api/v2/chat/custom?q=写首诗&role=你是一个诗人
     */
    @GetMapping("/custom")
    public String custom(@RequestParam("q") String q, @RequestParam("role") String role) {
        return chatClient.prompt()
            .system(role)          // 覆盖默认 System Prompt
            .user(q)              // 用户问题
            .call()
            .content();
    }

    /**
     * 流式输出
     * GET /api/v2/chat/stream?q=介绍北京到上海的航线
     */
    @GetMapping(value = "/stream", produces = "text/html;charset=UTF-8")
    public Flux<String> stream(@RequestParam("q") String q) {
        return chatClient.prompt(q)
            .stream()
            .content();           // 直接返回 Flux<String>
    }

    /**
     * Per-Request 动态配置
     * 精确查询用低 temperature，推荐类问题用高 temperature
     * GET /api/v2/chat/dynamic?q=推荐去哪旅游&temp=0.8
     */
    @GetMapping("/dynamic")
    public String dynamicConfig(@RequestParam("q") String q,
                                @RequestParam(value = "temp", defaultValue = "0.3") double temp) {
        return chatClient.prompt(q)
            .options(ChatOptions.builder()
                .temperature(temp)
                .model(temp > 0.5 ? "qwen-max" : "qwen-plus")  // 按需切换模型
                .build())
            .call()
            .content();
    }

    /**
     * 参数对比实验——观察不同参数组合的效果
     * GET /api/v2/chat/param-test?q=用一句话形容北京
     */
    @GetMapping("/param-test")
    public Map<String, List<String>> paramTest(@RequestParam("q") String q) {
        // 三组参数，各调用 3 次
        Map<String, ChatOptions> configs = Map.of(
            "temp=0", ChatOptions.builder().temperature(0.0).build(),
            "temp=1", ChatOptions.builder().temperature(1.0).build(),
            "temp=0+topP=0.1", ChatOptions.builder().temperature(0.0).topP(0.1).build()
        );

        Map<String, List<String>> results = new LinkedHashMap<>();
        configs.forEach((name, options) -> {
            List<String> responses = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                responses.add(chatClient.prompt(q).options(options).call().content());
            }
            results.put(name, responses);
        });
        return results;
    }
}
