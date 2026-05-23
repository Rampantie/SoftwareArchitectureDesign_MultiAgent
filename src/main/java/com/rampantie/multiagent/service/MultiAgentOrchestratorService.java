package com.rampantie.multiagent.service;

import com.rampantie.multiagent.agent.AuditAgent;
import com.rampantie.multiagent.agent.GenerationAgent;
import com.rampantie.multiagent.api.dto.AuditResultDto;
import com.rampantie.multiagent.api.dto.GenerateAuditRequest;
import com.rampantie.multiagent.api.dto.GenerateAuditResponse;
import com.rampantie.multiagent.audit.AuditDecision;
import com.rampantie.multiagent.config.AgentProperties;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MultiAgentOrchestratorService {

    private final GenerationAgent generationAgent;
    private final AuditAgent auditAgent;
    private final AgentProperties agentProperties;

    public MultiAgentOrchestratorService(GenerationAgent generationAgent,
                                         AuditAgent auditAgent,
                                         AgentProperties agentProperties) {
        this.generationAgent = generationAgent;
        this.auditAgent = auditAgent;
        this.agentProperties = agentProperties;
    }

    public GenerateAuditResponse process(GenerateAuditRequest request) {
        String traceId = UUID.randomUUID().toString();
        int maxRetries = Math.max(0, agentProperties.getOrchestration().getMaxRetries());

        String generated = generationAgent.generate(request.getInput(), request.getContext());
        AuditDecision decision = auditAgent.audit(request.getInput(), generated);
        int retryCount = 0;

        while (!decision.isApproved() && retryCount < maxRetries) {
            generated = generationAgent.regenerate(
                    request.getInput(),
                    request.getContext(),
                    generated,
                    decision.getReasons(),
                    decision.getRevisedAnswer()
            );
            decision = auditAgent.audit(request.getInput(), generated);
            retryCount++;
        }

        AuditResultDto auditResult = toDto(decision);
        String finalAnswer = resolveFinalAnswer(generated, decision);

        return new GenerateAuditResponse(
                traceId,
                request.getInput(),
                generated,
                finalAnswer,
                auditResult.isApproved(),
                auditResult,
                retryCount,
                retryCount + 1
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
