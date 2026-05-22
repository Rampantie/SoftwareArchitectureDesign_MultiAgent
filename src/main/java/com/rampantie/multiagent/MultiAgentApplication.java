package com.rampantie.multiagent;

import com.rampantie.multiagent.config.AgentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AgentProperties.class)
public class MultiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiAgentApplication.class, args);
    }
}
