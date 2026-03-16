package com.ai.course.quickstart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/experiment")
public class ExperimentController {

    private static final Logger log = LoggerFactory.getLogger(ExperimentController.class);

    private final ChatClient chatClient;

    public ExperimentController(ChatClient.Builder builder) {
        // 通过 System Prompt 限制回答长度，让对比更直观
        this.chatClient = builder
                .defaultSystem("你是一个简洁的助手，回答限制在20字以内。")
                .build();
    }

    /**
     * Temperature 对比实验
     * GET /api/experiment/temperature?message=用一个词形容春天
     *
     * 同一问题，分别用 temperature 0 和 1.0 各调用 3 次
     * temperature=0 的回答几乎相同，temperature=1.0 每次都不一样
     */
    @GetMapping("/temperature")
    public Map<String, List<String>> temperatureExperiment(
            @RequestParam(value = "message", defaultValue = "用一个词形容春天") String message) {

        double[] temperatures = {0.0, 1.0};
        int rounds = 3;
        log.info("开始 Temperature 对比实验，消息：{}", message);

        Map<String, CompletableFuture<List<String>>> futures = new LinkedHashMap<>();
        for (double temp : temperatures) {
            String key = "temperature_" + temp;
            futures.put(key, CompletableFuture.supplyAsync(() -> {
                List<String> responses = new ArrayList<>();
                for (int i = 0; i < rounds; i++) {
                    log.info("[{}] 第 {} 次调用开始...", key, i + 1);
                    long start = System.currentTimeMillis();
                    String content = chatClient.prompt()
                            .user(message)
                            .options(ChatOptions.builder()
                                    .temperature(temp)
                                    .build())
                            .call()
                            .content();
                    long cost = System.currentTimeMillis() - start;
                    log.info("[{}] 第 {} 次调用完成，耗时 {}ms，回答：{}", key, i + 1, cost, content);
                    responses.add(content);
                }
                return responses;
            }));
        }

        Map<String, List<String>> results = new LinkedHashMap<>();
        futures.forEach((key, future) -> results.put(key, future.join()));
        log.info("Temperature 对比实验完成");
        return results;
    }
}
