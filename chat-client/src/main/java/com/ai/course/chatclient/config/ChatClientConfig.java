package com.ai.course.chatclient.config;

import com.ai.course.chatclient.advisor.TokenUsageAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient 全局配置
 * 统一设置 System Prompt、模型参数、Advisor 链
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
            // 全局 System Prompt —— 所有对话都生效
            .defaultSystem("你是一个专业机票分析师「票小蜜」，" +
                "只回答机票、航班、旅行相关问题。" +
                "回答时提供航班号、时间、价格信息。")
            // 全局模型参数
            .defaultOptions(ChatOptions.builder()
                .model("qwen-plus")
                .temperature(0.3)
                .build())
            // Advisor 链——Token 监控 + 日志
            .defaultAdvisors(
                new TokenUsageAdvisor(),
                new SimpleLoggerAdvisor()
            )
            .build();
    }
}
