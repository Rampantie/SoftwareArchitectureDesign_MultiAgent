package com.rampantie.multiagent.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GenerateAuditRequest {

    @NotBlank(message = "input 不能为空")
    @Size(max = 4000, message = "input 长度不能超过 4000")
    private String input;

    @Size(max = 4000, message = "context 长度不能超过 4000")
    private String context;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
