package com.rampantie.multiagent.exception;

public class AgentInvocationException extends RuntimeException {

    public AgentInvocationException(String message) {
        super(message);
    }

    public AgentInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
