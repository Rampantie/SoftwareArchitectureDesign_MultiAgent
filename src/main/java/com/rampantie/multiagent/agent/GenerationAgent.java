package com.rampantie.multiagent.agent;

import com.rampantie.multiagent.exception.AgentInvocationException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GenerationAgent {

    private final ChatClient chatClient;

    public GenerationAgent(@Qualifier("generationChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String generate(String userInput, String context) {
        return invoke(buildInitialPrompt(userInput, context));
    }

    public String regenerate(String userInput,
                             String context,
                             String previousAnswer,
                             List<String> feedbackReasons,
                             String auditHint) {
        return invoke(buildRegeneratePrompt(userInput, context, previousAnswer, feedbackReasons, auditHint));
    }

    private String invoke(String prompt) {
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

    private String buildInitialPrompt(String userInput, String context) {
        StringBuilder builder = new StringBuilder();
        builder.append("用户问题：\n").append(userInput).append("\n");
        appendContext(builder, context);
        builder.append("\n请给出你的回答。");
        return builder.toString();
    }

    private String buildRegeneratePrompt(String userInput,
                                         String context,
                                         String previousAnswer,
                                         List<String> feedbackReasons,
                                         String auditHint) {
        String reasons = feedbackReasons == null || feedbackReasons.isEmpty()
                ? "未提供具体原因"
                : feedbackReasons.stream().collect(Collectors.joining("；"));

        StringBuilder builder = new StringBuilder();
        builder.append("用户问题：\n").append(userInput).append("\n");
        appendContext(builder, context);
        builder.append("\n你上一次回答未通过审计，请根据反馈重新生成改进后的回答。\n");
        builder.append("\n上一次回答：\n").append(previousAnswer).append("\n");
        builder.append("\n审计反馈：\n").append(reasons).append("\n");
        if (auditHint != null && !auditHint.isBlank()) {
            builder.append("\n审计建议方向：\n").append(auditHint.trim()).append("\n");
        }
        builder.append("\n请输出修订后的完整回答，确保解决上述问题。");
        return builder.toString();
    }

    private void appendContext(StringBuilder builder, String context) {
        if (context != null && !context.isBlank()) {
            builder.append("\n补充上下文：\n").append(context.trim()).append("\n");
        }
    }
}
