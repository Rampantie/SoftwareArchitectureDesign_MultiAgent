package com.rampantie.multiagent.service;

import com.rampantie.multiagent.agent.AuditAgent;
import com.rampantie.multiagent.agent.GenerationAgent;
import com.rampantie.multiagent.api.dto.GenerateAuditRequest;
import com.rampantie.multiagent.api.dto.GenerateAuditResponse;
import com.rampantie.multiagent.audit.AuditDecision;
import com.rampantie.multiagent.audit.RiskLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiAgentOrchestratorServiceTest {

    @Mock
    private GenerationAgent generationAgent;

    @Mock
    private AuditAgent auditAgent;

    @InjectMocks
    private MultiAgentOrchestratorService orchestratorService;

    @Test
    void shouldReturnGeneratedAnswerWhenApproved() {
        GenerateAuditRequest request = new GenerateAuditRequest();
        request.setInput("什么是微服务？");

        when(generationAgent.generate(eq("什么是微服务？"), eq(null))).thenReturn("微服务是...");
        when(auditAgent.audit(eq("什么是微服务？"), eq("微服务是..."))).thenReturn(approvedDecision());

        GenerateAuditResponse response = orchestratorService.process(request);

        assertThat(response.isApproved()).isTrue();
        assertThat(response.getGeneratedAnswer()).isEqualTo("微服务是...");
        assertThat(response.getFinalAnswer()).isEqualTo("微服务是...");
        assertThat(response.getTraceId()).isNotBlank();
    }

    @Test
    void shouldReturnRevisedAnswerWhenRejected() {
        GenerateAuditRequest request = new GenerateAuditRequest();
        request.setInput("危险问题");

        when(generationAgent.generate(eq("危险问题"), eq(null))).thenReturn("原始回答");
        when(auditAgent.audit(eq("危险问题"), eq("原始回答"))).thenReturn(rejectedDecision());

        GenerateAuditResponse response = orchestratorService.process(request);

        assertThat(response.isApproved()).isFalse();
        assertThat(response.getFinalAnswer()).isEqualTo("修订后的安全回答");
        assertThat(response.getAudit().getReasons()).contains("存在高风险内容");
    }

    private AuditDecision approvedDecision() {
        AuditDecision decision = new AuditDecision();
        decision.setApproved(true);
        decision.setRiskLevel(RiskLevel.LOW);
        decision.setReasons(List.of("通过"));
        decision.setRevisedAnswer("");
        return decision;
    }

    private AuditDecision rejectedDecision() {
        AuditDecision decision = new AuditDecision();
        decision.setApproved(false);
        decision.setRiskLevel(RiskLevel.HIGH);
        decision.setReasons(List.of("存在高风险内容"));
        decision.setRevisedAnswer("修订后的安全回答");
        return decision;
    }
}
