package com.rampantie.multiagent.controller;

import com.rampantie.multiagent.api.dto.GenerateAuditRequest;
import com.rampantie.multiagent.api.dto.GenerateAuditResponse;
import com.rampantie.multiagent.service.MultiAgentOrchestratorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    private final MultiAgentOrchestratorService orchestratorService;

    public AgentController(MultiAgentOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
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
}
