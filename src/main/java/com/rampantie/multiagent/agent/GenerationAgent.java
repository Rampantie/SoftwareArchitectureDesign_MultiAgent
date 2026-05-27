package com.rampantie.multiagent.agent;

import com.rampantie.multiagent.exception.AgentInvocationException;
import com.rampantie.multiagent.service.TokenUsageTracker;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GenerationAgent {

    private final ChatClient chatClient;
    private final TokenUsageTracker tokenUsageTracker;

    public GenerationAgent(@Qualifier("generationChatClient") ChatClient chatClient,
                          TokenUsageTracker tokenUsageTracker) {
        this.chatClient = chatClient;
        this.tokenUsageTracker = tokenUsageTracker;
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

    public String generateWithFormat(String userInput,
                                     String context,
                                     String outputFormat,
                                     String previousAttempt,
                                     List<String> feedback) {
        return invoke(buildFormattedPrompt(userInput, context, outputFormat, previousAttempt, feedback));
    }

    private String invoke(String prompt) {
        try {
            var result = chatClient.prompt()
                    .user(prompt)
                    .call();

            String content = result.content();
            if (content == null || content.isBlank()) {
                throw new AgentInvocationException("生成Agent返回空内容");
            }

            // 基于内容长度估算token（改进算法）
            // 中文：平均1.5字符 = 1 token
            // 英文：平均4字符 = 1 token
            long inputTokens = estimateTokenCount(prompt);
            long outputTokens = estimateTokenCount(content);

            tokenUsageTracker.recordUsage("GenerationAgent", -1, inputTokens, outputTokens);
            tokenUsageTracker.recordInteractionCall();

            return content.trim();
        } catch (AgentInvocationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AgentInvocationException("Generation Agent call failed: " + ex.getMessage(), ex);
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

    private String buildInitialPrompt(String userInput, String context) {
        StringBuilder builder = new StringBuilder();
        builder.append("User Question:\n").append(userInput).append("\n");
        appendContext(builder, context);
        builder.append("\nPlease provide your answer.");
        return builder.toString();
    }

    private String buildRegeneratePrompt(String userInput,
                                         String context,
                                         String previousAnswer,
                                         List<String> feedbackReasons,
                                         String auditHint) {
        String reasons = feedbackReasons == null || feedbackReasons.isEmpty()
                ? "No specific reasons provided"
                : feedbackReasons.stream().collect(Collectors.joining("; "));

        StringBuilder builder = new StringBuilder();
        builder.append("User Question:\n").append(userInput).append("\n");
        appendContext(builder, context);
        builder.append("\nYour previous answer did not pass audit. Please regenerate an improved answer based on the feedback.\n");
        builder.append("\nPrevious Answer:\n").append(previousAnswer).append("\n");
        builder.append("\nAudit Feedback:\n").append(reasons).append("\n");
        if (auditHint != null && !auditHint.isBlank()) {
            builder.append("\nAudit Suggestion:\n").append(auditHint.trim()).append("\n");
        }
        builder.append("\nPlease output the revised complete answer, ensuring to address the above issues.");
        return builder.toString();
    }

    private void appendContext(StringBuilder builder, String context) {
        if (context != null && !context.isBlank()) {
            builder.append("\nAdditional Context:\n").append(context.trim()).append("\n");
        }
    }

    private String buildFormattedPrompt(String userInput,
                                       String context,
                                       String outputFormat,
                                       String previousAttempt,
                                       List<String> feedback) {
        StringBuilder builder = new StringBuilder();
        builder.append("User Question:\n").append(userInput).append("\n");
        appendContext(builder, context);
        builder.append("\nOutput Requirements:\n").append(outputFormat).append("\n");

        if (previousAttempt != null && !previousAttempt.isBlank()) {
            builder.append("\nPrevious Attempt:\n").append(previousAttempt).append("\n");
        }

        if (feedback != null && !feedback.isEmpty()) {
            builder.append("\nAreas for Improvement:\n");
            String feedbackStr = feedback.stream().collect(Collectors.joining("; "));
            builder.append(feedbackStr).append("\n");
        }

        builder.append("\nPlease strictly follow the output requirements above.");
        return builder.toString();
    }
}
