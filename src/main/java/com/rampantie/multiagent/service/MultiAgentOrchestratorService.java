package com.rampantie.multiagent.service;

import com.rampantie.multiagent.agent.AuditAgent;
import com.rampantie.multiagent.agent.GenerationAgent;
import com.rampantie.multiagent.api.dto.AuditResultDto;
import com.rampantie.multiagent.api.dto.GenerateAuditRequest;
import com.rampantie.multiagent.api.dto.GenerateAuditResponse;
import com.rampantie.multiagent.audit.AuditDecision;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MultiAgentOrchestratorService {

    private final GenerationAgent generationAgent;
    private final AuditAgent auditAgent;

    public MultiAgentOrchestratorService(GenerationAgent generationAgent, AuditAgent auditAgent) {
        this.generationAgent = generationAgent;
        this.auditAgent = auditAgent;
    }

    public GenerateAuditResponse process(GenerateAuditRequest request) {
        String traceId = UUID.randomUUID().toString();

        String generated = generationAgent.generate(request.getInput(), request.getContext());
        AuditDecision decision = auditAgent.audit(request.getInput(), generated);

        String finalAnswer = resolveFinalAnswer(generated, decision);
        AuditResultDto auditResult = toDto(decision);

        return new GenerateAuditResponse(
                traceId,
                request.getInput(),
                generated,
                finalAnswer,
                auditResult.isApproved(),
                auditResult
        );
    }

    private String resolveFinalAnswer(String generated, AuditDecision decision) {
        if (decision.isApproved()) {
            return generated;
        }
        if (decision.getRevisedAnswer() != null && !decision.getRevisedAnswer().isBlank()) {
            return decision.getRevisedAnswer().trim();
        }
        return generated;
    }

    private AuditResultDto toDto(AuditDecision decision) {
        return new AuditResultDto(
                decision.isApproved(),
                decision.getRiskLevel().name(),
                decision.getReasons(),
                decision.getRevisedAnswer()
        );
    }
}
