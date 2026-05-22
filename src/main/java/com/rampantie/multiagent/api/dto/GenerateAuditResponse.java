package com.rampantie.multiagent.api.dto;

public class GenerateAuditResponse {

    private String traceId;
    private String input;
    private String generatedAnswer;
    private String finalAnswer;
    private boolean approved;
    private AuditResultDto audit;

    public GenerateAuditResponse() {
    }

    public GenerateAuditResponse(String traceId,
                                 String input,
                                 String generatedAnswer,
                                 String finalAnswer,
                                 boolean approved,
                                 AuditResultDto audit) {
        this.traceId = traceId;
        this.input = input;
        this.generatedAnswer = generatedAnswer;
        this.finalAnswer = finalAnswer;
        this.approved = approved;
        this.audit = audit;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getGeneratedAnswer() {
        return generatedAnswer;
    }

    public void setGeneratedAnswer(String generatedAnswer) {
        this.generatedAnswer = generatedAnswer;
    }

    public String getFinalAnswer() {
        return finalAnswer;
    }

    public void setFinalAnswer(String finalAnswer) {
        this.finalAnswer = finalAnswer;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public AuditResultDto getAudit() {
        return audit;
    }

    public void setAudit(AuditResultDto audit) {
        this.audit = audit;
    }
}
