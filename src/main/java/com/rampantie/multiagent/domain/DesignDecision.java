package com.rampantie.multiagent.domain;

import java.time.Instant;
import java.util.List;

public class DesignDecision {
    private String id;
    private int iterationNumber;
    private int stepNumber;
    private String decisionTitle;
    private String decisionContext;
    private List<String> alternatives;
    private String chosenSolution;
    private String rationale;
    private List<String> relatedQualityAttributes;
    private Instant createdAt;

    public DesignDecision(int iterationNumber, int stepNumber, String decisionTitle,
                          String decisionContext, List<String> alternatives,
                          String chosenSolution, String rationale,
                          List<String> relatedQualityAttributes) {
        this.id = "decision_" + System.currentTimeMillis();
        this.iterationNumber = iterationNumber;
        this.stepNumber = stepNumber;
        this.decisionTitle = decisionTitle;
        this.decisionContext = decisionContext;
        this.alternatives = alternatives;
        this.chosenSolution = chosenSolution;
        this.rationale = rationale;
        this.relatedQualityAttributes = relatedQualityAttributes;
        this.createdAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public int getIterationNumber() { return iterationNumber; }
    public int getStepNumber() { return stepNumber; }
    public String getDecisionTitle() { return decisionTitle; }
    public String getDecisionContext() { return decisionContext; }
    public List<String> getAlternatives() { return alternatives; }
    public String getChosenSolution() { return chosenSolution; }
    public String getRationale() { return rationale; }
    public List<String> getRelatedQualityAttributes() { return relatedQualityAttributes; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return String.format("""
                【设计决策】(迭代%d - 步骤%d)
                标题: %s
                背景: %s
                候选方案: %s
                选择: %s
                理由: %s
                相关质量属性: %s
                """,
                iterationNumber, stepNumber, decisionTitle, decisionContext,
                String.join(", ", alternatives), chosenSolution, rationale,
                String.join(", ", relatedQualityAttributes));
    }
}
