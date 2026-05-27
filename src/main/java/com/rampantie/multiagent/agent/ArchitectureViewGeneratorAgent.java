package com.rampantie.multiagent.agent;

import com.rampantie.multiagent.domain.AddPromptTemplates;
import com.rampantie.multiagent.exception.AgentInvocationException;
import org.springframework.stereotype.Component;

@Component
public class ArchitectureViewGeneratorAgent {

    private final GenerationAgent generationAgent;

    public ArchitectureViewGeneratorAgent(GenerationAgent generationAgent) {
        this.generationAgent = generationAgent;
    }

    public String generateMermaidDiagram(String architectureDescription, String diagramType) {
        try {
            String prompt = AddPromptTemplates.buildViewGenerationPrompt(architectureDescription, diagramType);
            System.out.println("📊 Generating Mermaid diagram: " + diagramType);

            String output = generationAgent.generate("Generate architecture diagram", prompt);

            if (output == null || output.trim().isEmpty()) {
                throw new AgentInvocationException("Generation Agent returned empty content");
            }

            String mermaidCode = extractMermaidCode(output);
            System.out.println("✓ Mermaid diagram generated successfully (" + diagramType + ")");
            return mermaidCode;

        } catch (Exception ex) {
            System.err.println("❌ Mermaid diagram generation failed (" + diagramType + "): " + ex.getMessage());
            throw new AgentInvocationException("View generation Agent call failed: " + ex.getMessage(), ex);
        }
    }

    private String extractMermaidCode(String output) {
        if (output == null || output.trim().isEmpty()) {
            return "```mermaid\ngraph LR\n    A[ERROR: No output]\n```";
        }

        // 尝试提取已标记的mermaid代码块
        if (output.contains("```mermaid")) {
            int startIdx = output.indexOf("```mermaid");
            int endIdx = output.indexOf("```", startIdx + 10);

            if (endIdx > startIdx) {
                String extracted = output.substring(startIdx, endIdx + 3);
                return extracted.trim();
            }
        }

        // 如果没有找到标记，假设整个输出就是mermaid代码
        String trimmed = output.trim();
        if (!trimmed.startsWith("```mermaid")) {
            return "```mermaid\n" + trimmed + "\n```";
        }

        return trimmed;
    }
}
