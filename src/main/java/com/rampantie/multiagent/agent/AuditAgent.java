package com.rampantie.multiagent.agent;

import com.rampantie.multiagent.audit.AuditDecision;
import com.rampantie.multiagent.audit.AuditResultParser;
import com.rampantie.multiagent.exception.AgentInvocationException;
import com.rampantie.multiagent.service.TokenUsageTracker;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AuditAgent {

    private final ChatClient chatClient;
    private final AuditResultParser auditResultParser;
    private final TokenUsageTracker tokenUsageTracker;

    public AuditAgent(@Qualifier("auditChatClient") ChatClient chatClient,
                      AuditResultParser auditResultParser,
                      TokenUsageTracker tokenUsageTracker) {
        this.chatClient = chatClient;
        this.auditResultParser = auditResultParser;
        this.tokenUsageTracker = tokenUsageTracker;
    }

    public AuditDecision audit(String userInput, String generatedAnswer) {
        String prompt = buildPrompt(userInput, generatedAnswer);
        try {
            var result = chatClient.prompt()
                    .user(prompt)
                    .call();

            String content = result.content();

            // Estimate token count based on content length
            long inputTokens = estimateTokenCount(prompt);
            long outputTokens = estimateTokenCount(content);

            tokenUsageTracker.recordUsage("AuditAgent", 5, inputTokens, outputTokens);
            tokenUsageTracker.recordInteractionCall();

            return auditResultParser.parse(content);
        } catch (Exception ex) {
            throw new AgentInvocationException("Audit Agent call failed: " + ex.getMessage(), ex);
        }
    }

    private long estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        long count = 0;
        for (char c : text.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FFF) {
                // Chinese character: 1 char = 1 token
                count += 1;
            } else if (Character.isWhitespace(c)) {
                // Spaces don't count
                continue;
            } else if (c >= 0x00 && c <= 0x7F) {
                // ASCII character: 4 chars = 1 token
                continue;
            } else {
                // Other character: 1 char = 1 token
                count += 1;
            }
        }

        // Add English word count estimate
        String[] words = text.split("\\s+");
        long englishTokens = (long) (words.length * 0.25); // Average 0.25 tokens per word

        return count + englishTokens;
    }

    private String buildPrompt(String userInput, String generatedAnswer) {
        return """
                Please audit the following answer and output JSON as required.

                User Question:
                %s

                Generation Agent Answer:
                %s
                """.formatted(userInput, generatedAnswer);
    }
}
