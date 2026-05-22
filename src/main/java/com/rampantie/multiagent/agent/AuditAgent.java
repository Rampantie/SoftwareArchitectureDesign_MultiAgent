package com.rampantie.multiagent.agent;

import com.rampantie.multiagent.audit.AuditDecision;
import com.rampantie.multiagent.audit.AuditResultParser;
import com.rampantie.multiagent.exception.AgentInvocationException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AuditAgent {

    private final ChatClient chatClient;
    private final AuditResultParser auditResultParser;

    public AuditAgent(@Qualifier("auditChatClient") ChatClient chatClient,
                      AuditResultParser auditResultParser) {
        this.chatClient = chatClient;
        this.auditResultParser = auditResultParser;
    }

    public AuditDecision audit(String userInput, String generatedAnswer) {
        String prompt = buildPrompt(userInput, generatedAnswer);
        try {
            String content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            return auditResultParser.parse(content);
        } catch (Exception ex) {
            throw new AgentInvocationException("审计Agent调用失败: " + ex.getMessage(), ex);
        }
    }

    private String buildPrompt(String userInput, String generatedAnswer) {
        return """
                请审计以下回答，并按要求输出 JSON。

                用户问题：
                %s

                生成Agent回答：
                %s
                """.formatted(userInput, generatedAnswer);
    }
}
