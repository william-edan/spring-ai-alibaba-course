package com.ai.course.functioncalling.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 开发环境用：拦截 RestClient 的 HTTP 请求/响应，打印完整 body。
 * 用于观察 Spring AI 与 DashScope API 的两轮交互过程。
 */
@Configuration
public class HttpLoggingConfig {

    @Bean
    public RestClientCustomizer httpLoggingCustomizer() {
        return builder -> builder
                .requestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
                .requestInterceptor(new LoggingInterceptor());
    }

    static class LoggingInterceptor implements ClientHttpRequestInterceptor {

        private static final Logger log = LoggerFactory.getLogger("HTTP_LOG");

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            // 打印请求
            log.info(">>> {} {}", request.getMethod(), request.getURI());
            if (body.length > 0) {
                log.info(">>> Request Body:\n{}", new String(body, StandardCharsets.UTF_8));
            }

            ClientHttpResponse response = execution.execute(request, body);

            // 打印响应（BufferingClientHttpRequestFactory 允许多次读取 body）
            byte[] responseBody = response.getBody().readAllBytes();
            log.info("<<< Status: {}", response.getStatusCode());
            log.info("<<< Response Body:\n{}", new String(responseBody, StandardCharsets.UTF_8));

            return response;
        }
    }
}
