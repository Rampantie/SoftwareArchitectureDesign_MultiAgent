package com.rampantie.multiagent.audit;

import java.util.ArrayList;
import java.util.List;

public class AuditDecision {

    private boolean approved;
    private RiskLevel riskLevel = RiskLevel.UNKNOWN;
    private List<String> reasons = new ArrayList<>();
    private String revisedAnswer = "";

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons != null ? reasons : new ArrayList<>();
    }

    public String getRevisedAnswer() {
        return revisedAnswer;
    }

    public void setRevisedAnswer(String revisedAnswer) {
        this.revisedAnswer = revisedAnswer != null ? revisedAnswer : "";
    }
}
