package com.rampantie.multiagent.controller;

import com.rampantie.multiagent.api.dto.GenerateAuditRequest;
import com.rampantie.multiagent.api.dto.GenerateAuditResponse;
import com.rampantie.multiagent.domain.AddIterationResult;
import com.rampantie.multiagent.service.MultiAgentOrchestratorService;
import com.rampantie.multiagent.service.IterationOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    private final MultiAgentOrchestratorService orchestratorService;
    private final IterationOrchestrationService iterationOrchestrationService;

    public AgentController(MultiAgentOrchestratorService orchestratorService,
                          IterationOrchestrationService iterationOrchestrationService) {
        this.orchestratorService = orchestratorService;
        this.iterationOrchestrationService = iterationOrchestrationService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "multi-agent-system"));
    }

    @PostMapping("/generate-and-audit")
    public ResponseEntity<GenerateAuditResponse> generateAndAudit(@Valid @RequestBody GenerateAuditRequest request) {
        GenerateAuditResponse response = orchestratorService.process(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/architecture/design-iterations")
    public ResponseEntity<Map<String, Object>> executeDesignIterations() {
        List<AddIterationResult> results = iterationOrchestrationService.executeAllIterations();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "完成了4次迭代的架构设计",
                "iterationCount", results.size(),
                "results", results.stream().map(r -> Map.of(
                        "iteration", r.getIterationNumber(),
                        "objective", r.getIterationObjective(),
                        "executionTimeMs", r.getExecutionTimeMs(),
                        "traceId", r.getTraceId()
                )).toList()
        ));
    }
}
