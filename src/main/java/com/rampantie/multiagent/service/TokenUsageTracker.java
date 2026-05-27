package com.rampantie.multiagent.service;

import com.rampantie.multiagent.domain.TokenUsage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TokenUsageTracker {

    private final List<TokenUsage> usages = new ArrayList<>();
    private int interactionCallCount = 0;  // 追踪实际的API调用次数

    public void recordUsage(String agentName, int stepNumber, long inputTokens, long outputTokens) {
        TokenUsage usage = new TokenUsage(agentName, stepNumber, inputTokens, outputTokens);
        usages.add(usage);
    }

    public void recordInteractionCall() {
        interactionCallCount++;
    }

    public int getInteractionCallCount() {
        return interactionCallCount;
    }

    public List<TokenUsage> getAllUsages() {
        return new ArrayList<>(usages);
    }

    public long getTotalInputTokens() {
        return usages.stream().mapToLong(TokenUsage::getInputTokens).sum();
    }

    public long getTotalOutputTokens() {
        return usages.stream().mapToLong(TokenUsage::getOutputTokens).sum();
    }

    public long getTotalTokens() {
        return getTotalInputTokens() + getTotalOutputTokens();
    }

    public long getTotalTokensInThousands() {
        return (getTotalTokens() + 500) / 1000; // 四舍五入到千位
    }

    public String generateCostAnalysisReport(String completionMethod, String model, int interactionRounds) {
        return String.format("""
                # Interaction Cost Analysis

                | Assignment Completion Method | Language Model Used | Human Interactions (Rounds) | Token Consumption (K tokens) |
                |------------|---------|-----------|----------|
                | %s | %s | %d | %d |

                ## Detailed Statistics

                - **Total Input Tokens**: %,d
                - **Total Output Tokens**: %,d
                - **Total Tokens**: %,d
                - **Converted to K Tokens**: %d
                - **Actual API Call Count**: %d

                ## Token Usage Details by Step

                | Agent | Step | Input Tokens | Output Tokens | Total Tokens |
                |-------|------|-----------|-----------|-----------|
                %s
                """,
                completionMethod, model, interactionRounds, getTotalTokensInThousands(),
                getTotalInputTokens(), getTotalOutputTokens(), getTotalTokens(),
                getTotalTokensInThousands(), interactionCallCount,
                generateDetailedTable()
        );
    }

    private String generateDetailedTable() {
        StringBuilder sb = new StringBuilder();
        for (TokenUsage usage : usages) {
            sb.append(String.format("| %s | %d | %,d | %,d | %,d |\n",
                    usage.getAgentName(),
                    usage.getStepNumber(),
                    usage.getInputTokens(),
                    usage.getOutputTokens(),
                    usage.getTotalTokens()
            ));
        }
        return sb.toString();
    }

    public void clear() {
        usages.clear();
        interactionCallCount = 0;
    }
}
