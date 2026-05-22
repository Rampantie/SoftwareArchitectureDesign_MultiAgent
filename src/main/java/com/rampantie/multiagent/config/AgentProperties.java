package com.rampantie.multiagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "multi-agent")
public class AgentProperties {

    private final Generation generation = new Generation();
    private final Audit audit = new Audit();

    public Generation getGeneration() {
        return generation;
    }

    public Audit getAudit() {
        return audit;
    }

    public static class Generation {
        private String systemPrompt;

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }
    }

    public static class Audit {
        private String systemPrompt;

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }
    }
}
