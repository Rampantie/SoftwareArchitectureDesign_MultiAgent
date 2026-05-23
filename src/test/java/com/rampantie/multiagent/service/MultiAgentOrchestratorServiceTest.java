package com.rampantie.multiagent.service;

import com.rampantie.multiagent.agent.AuditAgent;
import com.rampantie.multiagent.agent.GenerationAgent;
import com.rampantie.multiagent.api.dto.GenerateAuditRequest;
import com.rampantie.multiagent.api.dto.GenerateAuditResponse;
import com.rampantie.multiagent.audit.AuditDecision;
import com.rampantie.multiagent.audit.RiskLevel;
import com.rampantie.multiagent.config.AgentProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiAgentOrchestratorServiceTest {

    @Mock
    private GenerationAgent generationAgent;

    @Mock
    private AuditAgent auditAgent;

    private AgentProperties agentProperties;
    private MultiAgentOrchestratorService orchestratorService;

    @BeforeEach
    void setUp() {
        agentProperties = new AgentProperties();
        orchestratorService = new MultiAgentOrchestratorService(
                generationAgent, auditAgent, agentProperties);
    }

    @Test
    void shouldReturnGeneratedAnswerWhenApproved() {
        agentProperties.getOrchestration().setMaxRetries(2);
        GenerateAuditRequest request = new GenerateAuditRequest();
        request.setInput("什么是微服务？");

        when(generationAgent.generate(eq("什么是微服务？"), isNull())).thenReturn("微服务是...");
        when(auditAgent.audit(eq("什么是微服务？"), eq("微服务是..."))).thenReturn(approvedDecision());

        GenerateAuditResponse response = orchestratorService.process(request);

        assertThat(response.isApproved()).isTrue();
        assertThat(response.getGeneratedAnswer()).isEqualTo("微服务是...");
        assertThat(response.getFinalAnswer()).isEqualTo("微服务是...");
        assertThat(response.getRetryCount()).isZero();
        assertThat(response.getTotalAttempts()).isEqualTo(1);
        verify(generationAgent, never()).regenerate(
                eq("什么是微服务？"), isNull(), eq("微服务是..."), anyList(), eq(""));
    }

    @Test
    void shouldRegenerateWhenRejectedThenApproved() {
        agentProperties.getOrchestration().setMaxRetries(2);
        GenerateAuditRequest request = new GenerateAuditRequest();
        request.setInput("危险问题");

        AuditDecision firstReject = rejectedDecision("存在高风险内容", "请更安全地表述");
        AuditDecision secondApprove = approvedDecision();

        when(generationAgent.generate(eq("危险问题"), isNull())).thenReturn("原始回答");
        when(generationAgent.regenerate(
                eq("危险问题"),
                isNull(),
                eq("原始回答"),
                eq(List.of("存在高风险内容")),
                eq("请更安全地表述")
        )).thenReturn("改进后的回答");
        when(auditAgent.audit(eq("危险问题"), eq("原始回答"))).thenReturn(firstReject);
        when(auditAgent.audit(eq("危险问题"), eq("改进后的回答"))).thenReturn(secondApprove);

        GenerateAuditResponse response = orchestratorService.process(request);

        assertThat(response.isApproved()).isTrue();
        assertThat(response.getGeneratedAnswer()).isEqualTo("改进后的回答");
        assertThat(response.getFinalAnswer()).isEqualTo("改进后的回答");
        assertThat(response.getRetryCount()).isEqualTo(1);
        assertThat(response.getTotalAttempts()).isEqualTo(2);
        verify(generationAgent, times(1)).regenerate(
                eq("危险问题"), isNull(), eq("原始回答"), eq(List.of("存在高风险内容")), eq("请更安全地表述"));
    }

    @Test
    void shouldReturnRevisedAnswerWhenStillRejectedAfterMaxRetries() {
        agentProperties.getOrchestration().setMaxRetries(1);
        GenerateAuditRequest request = new GenerateAuditRequest();
        request.setInput("危险问题");

        AuditDecision reject = rejectedDecision("存在高风险内容", "修订后的安全回答");

        when(generationAgent.generate(eq("危险问题"), isNull())).thenReturn("原始回答");
        when(generationAgent.regenerate(
                eq("危险问题"),
                isNull(),
                eq("原始回答"),
                eq(List.of("存在高风险内容")),
                eq("修订后的安全回答")
        )).thenReturn("仍不安全的回答");
        when(auditAgent.audit(eq("危险问题"), eq("原始回答"))).thenReturn(reject);
        when(auditAgent.audit(eq("危险问题"), eq("仍不安全的回答"))).thenReturn(reject);

        GenerateAuditResponse response = orchestratorService.process(request);

        assertThat(response.isApproved()).isFalse();
        assertThat(response.getFinalAnswer()).isEqualTo("修订后的安全回答");
        assertThat(response.getRetryCount()).isEqualTo(1);
        assertThat(response.getTotalAttempts()).isEqualTo(2);
    }

    private AuditDecision approvedDecision() {
        AuditDecision decision = new AuditDecision();
        decision.setApproved(true);
        decision.setRiskLevel(RiskLevel.LOW);
        decision.setReasons(List.of("通过"));
        decision.setRevisedAnswer("");
        return decision;
    }

    private AuditDecision rejectedDecision(String reason, String revisedAnswer) {
        AuditDecision decision = new AuditDecision();
        decision.setApproved(false);
        decision.setRiskLevel(RiskLevel.HIGH);
        decision.setReasons(List.of(reason));
        decision.setRevisedAnswer(revisedAnswer);
        return decision;
    }
}
