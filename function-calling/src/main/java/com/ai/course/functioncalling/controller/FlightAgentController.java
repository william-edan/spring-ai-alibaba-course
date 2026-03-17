package com.ai.course.functioncalling.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/flight")
public class FlightAgentController {

    private final ChatClient chatClient;

    public FlightAgentController(ChatClient.Builder builder) {
        this.chatClient = builder
            .defaultSystem("""
                你是机票分析师「票小蜜」。

                工具使用规则：
                1. 用户查机票时，调用 searchFlights 工具获取真实航班数据
                2. 用户要求对比时，先查询获取航班号，再调用 compareFlights 对比
                3. 绝不编造航班信息
                4. 信息不足时（缺出发地/目的地/日期），先追问
                5. 闲聊不调用工具

                当前日期：%s
                """.formatted(LocalDate.now()))
            .build();
    }

    /**
     * 查询场景——只给只读工具
     * 遵循最小权限原则：查询场景不暴露比价工具
     */
    @GetMapping("/search")
    public String search(@RequestParam String q,
                         @RequestParam(defaultValue = "guest") String userId) {
        return chatClient.prompt(q)
            .toolNames("searchFlights")
            .toolContext(Map.of(
                "userId", userId,
                "requestTime", LocalDateTime.now().toString()
            ))
            .call()
            .content();
    }

    /**
     * 智能对话——给查询 + 比价两个工具
     * LLM 自主决定调用顺序：先查询再比价
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String q,
                       @RequestParam(defaultValue = "guest") String userId) {
        System.out.println(">>> LOGGING_LEVEL=" + System.getenv("DASHSCOPE_NETWORK_LOGGING_LEVEL"));
        return chatClient.prompt(q)
            .toolNames("searchFlights", "compareFlights")
            .toolContext(Map.of(
                "userId", userId,
                "requestTime", LocalDateTime.now().toString()
            ))
                // Advisor + 日志
            .call()
            .content();
    }

    /**
     * 流式输出版本
     */
    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> stream(@RequestParam String q,
                               @RequestParam(defaultValue = "guest") String userId) {
        return chatClient.prompt(q)
            .toolNames("searchFlights", "compareFlights")
            .toolContext(Map.of("userId", userId))
            .stream()
            .content();
    }
}
