package com.rampantie.multiagent.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentChatClientConfig {

    @Bean
    public ChatClient generationChatClient(ChatClient.Builder builder, AgentProperties properties) {
        return builder
                .defaultSystem(properties.getGeneration().getSystemPrompt())
                .defaultOptions(DashScopeChatOptions.builder().build())
                .build();
    }

    @Bean
    public ChatClient auditChatClient(ChatClient.Builder builder, AgentProperties properties) {
        return builder
                .defaultSystem(properties.getAudit().getSystemPrompt())
                .defaultOptions(DashScopeChatOptions.builder().temperature(0.2).build())
                .build();
    }
}
