package com.rampantie.multiagent.api.dto;

import java.util.List;

public class AuditResultDto {

    private boolean approved;
    private String riskLevel;
    private List<String> reasons;
    private String revisedAnswer;

    public AuditResultDto() {
    }

    public AuditResultDto(boolean approved, String riskLevel, List<String> reasons, String revisedAnswer) {
        this.approved = approved;
        this.riskLevel = riskLevel;
        this.reasons = reasons;
        this.revisedAnswer = revisedAnswer;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public String getRevisedAnswer() {
        return revisedAnswer;
    }

    public void setRevisedAnswer(String revisedAnswer) {
        this.revisedAnswer = revisedAnswer;
    }
}
