package com.rampantie.multiagent.agent;

import com.rampantie.multiagent.domain.AddPromptTemplates;
import com.rampantie.multiagent.domain.DesignDecision;
import com.rampantie.multiagent.exception.AgentInvocationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DesignDecisionRecorderAgent {

    private final GenerationAgent generationAgent;

    public DesignDecisionRecorderAgent(GenerationAgent generationAgent) {
        this.generationAgent = generationAgent;
    }

    public List<DesignDecision> recordDecisions(int iterationNumber,
                                               String stepContext,
                                               String designAlternatives,
                                               String chosenDesign) {
        try {
            String prompt = AddPromptTemplates.buildDecisionRecordingPrompt(
                    stepContext, designAlternatives, chosenDesign
            );

            String output = generationAgent.generate("Record architecture decisions", prompt);
            return parseDecisions(iterationNumber, output);
        } catch (Exception ex) {
            throw new AgentInvocationException("Design Decision Recorder Agent call failed: " + ex.getMessage(), ex);
        }
    }

    private List<DesignDecision> parseDecisions(int iterationNumber, String output) {
        List<DesignDecision> decisions = new ArrayList<>();

        // 按行解析，每行是一个决策
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty() || line.trim().startsWith("决策编号")) {
                continue; // 跳过空行和标题行
            }

            DesignDecision decision = parseDecisionLine(iterationNumber, line.trim());
            if (decision != null) {
                decisions.add(decision);
            }
        }

        return decisions;
    }

    private DesignDecision parseDecisionLine(int iterationNumber, String line) {
        try {
            // 格式: 决策编号 | 决策标题 | 背景/问题 | 考虑的方案 | 最终选择 | 理由 | 相关质量属性
            String[] parts = line.split("\\|");
            if (parts.length < 7) {
                return null;
            }

            String decisionTitle = parts[1].trim();
            String context = parts[2].trim();
            String alternatives = parts[3].trim();
            String chosenSolution = parts[4].trim();
            String rationale = parts[5].trim();
            String qualityAttrs = parts[6].trim();

            List<String> alternativesList = Arrays.stream(alternatives.split("、"))
                    .map(String::trim)
                    .collect(Collectors.toList());

            List<String> qualityAttrsList = Arrays.stream(qualityAttrs.split("、"))
                    .map(String::trim)
                    .collect(Collectors.toList());

            // 假设为步骤5（架构要素实例化）的决策
            return new DesignDecision(iterationNumber, 5, decisionTitle, context,
                    alternativesList, chosenSolution, rationale, qualityAttrsList);
        } catch (Exception ex) {
            // 解析失败，返回null
            return null;
        }
    }
}
