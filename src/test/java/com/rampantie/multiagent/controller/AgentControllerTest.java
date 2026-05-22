package com.rampantie.multiagent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rampantie.multiagent.api.dto.AuditResultDto;
import com.rampantie.multiagent.api.dto.GenerateAuditRequest;
import com.rampantie.multiagent.api.dto.GenerateAuditResponse;
import com.rampantie.multiagent.service.MultiAgentOrchestratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgentController.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MultiAgentOrchestratorService orchestratorService;

    @Test
    void healthShouldReturnUp() throws Exception {
        mockMvc.perform(get("/api/v1/agents/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldRejectBlankInput() throws Exception {
        GenerateAuditRequest request = new GenerateAuditRequest();
        request.setInput("");

        mockMvc.perform(post("/api/v1/agents/generate-and-audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnGenerateAndAuditResponse() throws Exception {
        GenerateAuditRequest request = new GenerateAuditRequest();
        request.setInput("你好");

        AuditResultDto audit = new AuditResultDto(true, "LOW", List.of("通过"), "");
        GenerateAuditResponse response = new GenerateAuditResponse(
                "trace-1",
                "你好",
                "你好，我是助手",
                "你好，我是助手",
                true,
                audit
        );
        when(orchestratorService.process(any(GenerateAuditRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/agents/generate-and-audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.traceId").value("trace-1"))
                .andExpect(jsonPath("$.approved").value(true))
                .andExpect(jsonPath("$.finalAnswer").value("你好，我是助手"));
    }
}
