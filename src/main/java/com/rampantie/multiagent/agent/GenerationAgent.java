package com.rampantie.multiagent.agent;

import com.rampantie.multiagent.exception.AgentInvocationException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class GenerationAgent {

    private final ChatClient chatClient;

    public GenerationAgent(@Qualifier("generationChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String generate(String userInput, String context) {
        String prompt = buildPrompt(userInput, context);
        try {
            String content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            if (content == null || content.isBlank()) {
                throw new AgentInvocationException("生成Agent返回空内容");
            }
            return content.trim();
        } catch (AgentInvocationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AgentInvocationException("生成Agent调用失败: " + ex.getMessage(), ex);
        }
    }

    private String buildPrompt(String userInput, String context) {
        StringBuilder builder = new StringBuilder();
        builder.append("用户问题：\n").append(userInput).append("\n");
        if (context != null && !context.isBlank()) {
            builder.append("\n补充上下文：\n").append(context.trim()).append("\n");
        }
        builder.append("\n请给出你的回答。");
        return builder.toString();
    }
}
