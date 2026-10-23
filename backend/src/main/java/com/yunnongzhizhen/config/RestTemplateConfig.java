package com.yunnongzhizhen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate：调用 DeepSeek OpenAI 兼容接口。
 */
@Configuration
public class RestTemplateConfig {

    @Value("${deepseek.connect-timeout-ms:10000}")
    private long connectTimeoutMs;

    @Value("${deepseek.read-timeout-ms:60000}")
    private long readTimeoutMs;

    /**
     * @param builder RestTemplateBuilder
     * @return 带超时的 RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .setReadTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }
}
