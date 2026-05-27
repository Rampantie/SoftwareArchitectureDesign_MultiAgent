package com.rampantie.multiagent.domain;

public class TokenUsage {
    private long inputTokens;
    private long outputTokens;
    private String agentName;
    private int stepNumber;
    private long timestamp;

    public TokenUsage(String agentName, int stepNumber, long inputTokens, long outputTokens) {
        this.agentName = agentName;
        this.stepNumber = stepNumber;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.timestamp = System.currentTimeMillis();
    }

    public long getTotalTokens() {
        return inputTokens + outputTokens;
    }

    // Getters
    public long getInputTokens() { return inputTokens; }
    public long getOutputTokens() { return outputTokens; }
    public String getAgentName() { return agentName; }
    public int getStepNumber() { return stepNumber; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("Agent: %s, Step: %d, Input: %d, Output: %d, Total: %d",
                agentName, stepNumber, inputTokens, outputTokens, getTotalTokens());
    }
}
