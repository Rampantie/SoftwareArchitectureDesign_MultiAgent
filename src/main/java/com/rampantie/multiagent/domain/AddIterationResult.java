package com.rampantie.multiagent.domain;

import java.util.List;
import java.util.Map;

public class AddIterationResult {
    private int iterationNumber;
    private String iterationObjective;

    // 7个步骤的输出
    private String step1ReviewInputsOutput;
    private String step2DetermineObjectiveOutput;
    private String step3SelectElementsOutput;
    private String step4SelectDesignConceptsOutput;
    private String step5InstantiateElementsOutput;
    private List<String> step6ViewOutputs;  // Mermaid图
    private List<DesignDecision> step6Decisions;  // 设计决策
    private String step7AnalyzeDesignOutput;

    private String traceId;
    private long executionTimeMs;

    public AddIterationResult(int iterationNumber, String iterationObjective) {
        this.iterationNumber = iterationNumber;
        this.iterationObjective = iterationObjective;
        this.step6ViewOutputs = List.of();
        this.step6Decisions = List.of();
    }

    // Setters
    public void setStep1ReviewInputsOutput(String output) { this.step1ReviewInputsOutput = output; }
    public void setStep2DetermineObjectiveOutput(String output) { this.step2DetermineObjectiveOutput = output; }
    public void setStep3SelectElementsOutput(String output) { this.step3SelectElementsOutput = output; }
    public void setStep4SelectDesignConceptsOutput(String output) { this.step4SelectDesignConceptsOutput = output; }
    public void setStep5InstantiateElementsOutput(String output) { this.step5InstantiateElementsOutput = output; }
    public void setStep6ViewOutputs(List<String> outputs) { this.step6ViewOutputs = outputs; }
    public void setStep6Decisions(List<DesignDecision> decisions) { this.step6Decisions = decisions; }
    public void setStep7AnalyzeDesignOutput(String output) { this.step7AnalyzeDesignOutput = output; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public void setExecutionTimeMs(long timeMs) { this.executionTimeMs = timeMs; }

    // Getters
    public int getIterationNumber() { return iterationNumber; }
    public String getIterationObjective() { return iterationObjective; }
    public String getStep1ReviewInputsOutput() { return step1ReviewInputsOutput; }
    public String getStep2DetermineObjectiveOutput() { return step2DetermineObjectiveOutput; }
    public String getStep3SelectElementsOutput() { return step3SelectElementsOutput; }
    public String getStep4SelectDesignConceptsOutput() { return step4SelectDesignConceptsOutput; }
    public String getStep5InstantiateElementsOutput() { return step5InstantiateElementsOutput; }
    public List<String> getStep6ViewOutputs() { return step6ViewOutputs; }
    public List<DesignDecision> getStep6Decisions() { return step6Decisions; }
    public String getStep7AnalyzeDesignOutput() { return step7AnalyzeDesignOutput; }
    public String getTraceId() { return traceId; }
    public long getExecutionTimeMs() { return executionTimeMs; }

    public String getSummary() {
        return String.format("""
                ========== Iteration %d Results Summary ==========
                Objective: %s
                Execution Time: %d ms
                Trace ID: %s

                📋 ADD 3.0 Step Completion Status:
                ✓ Step 1 (Review Inputs): Completed
                ✓ Step 2 (Determine Objective): Completed
                ✓ Step 3 (Select Elements): Completed
                ✓ Step 4 (Select Concept): Completed
                ✓ Step 5 (Instantiate Elements): Completed
                ✓ Step 6 (Sketch Views/Record Decisions): Completed (%d views, %d decisions)
                ✓ Step 7 (Analyze Design): Completed
                """,
                iterationNumber, iterationObjective, executionTimeMs, traceId,
                step6ViewOutputs.size(), step6Decisions.size());
    }
}
