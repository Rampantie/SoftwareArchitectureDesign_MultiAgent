package com.rampantie.multiagent.api.dto;

import java.time.Instant;

public class ApiErrorResponse {

    private String code;
    private String message;
    private String traceId;
    private Instant timestamp;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(String code, String message, String traceId, Instant timestamp) {
        this.code = code;
        this.message = message;
        this.traceId = traceId;
        this.timestamp = timestamp;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
