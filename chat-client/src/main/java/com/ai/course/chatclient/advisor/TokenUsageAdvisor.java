package com.ai.course.chatclient.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.metadata.Usage;

/**
 * Token 消耗统计 Advisor
 *
 * 这个 Advisor 放在最外层（order=0），before 记录开始时间，after 统计 token。
 * 因为是最外层，after 最后执行，所以耗时包含了所有内层 Advisor 的耗时。
 */
public class TokenUsageAdvisor implements BaseAdvisor {

    private static final Logger log = LoggerFactory.getLogger(TokenUsageAdvisor.class);
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public int getOrder() {
        return 0;  // 最外层——before 最先执行，after 最后执行
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        startTime.set(System.currentTimeMillis());
        return request;  // 不修改请求，直接放行
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        long elapsed = System.currentTimeMillis() - startTime.get();
        startTime.remove();

        Usage usage = response.chatResponse().getMetadata().getUsage();
        log.info("Token 消耗 | 输入: {} | 输出: {} | 总计: {} | 耗时: {}ms",
            usage.getPromptTokens(),
            usage.getCompletionTokens(),
            usage.getTotalTokens(),
            elapsed);

        return response;
    }
}
