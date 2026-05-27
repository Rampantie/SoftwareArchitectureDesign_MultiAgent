package com.rampantie.multiagent.agent;

import com.rampantie.multiagent.domain.AddIterationResult;
import com.rampantie.multiagent.domain.AddPromptTemplates;
import com.rampantie.multiagent.domain.IterationContext;
import com.rampantie.multiagent.exception.AgentInvocationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ArchitectureDesignAgent {

    private final GenerationAgent generationAgent;
    private final AuditAgent auditAgent;

    public ArchitectureDesignAgent(GenerationAgent generationAgent, AuditAgent auditAgent) {
        this.generationAgent = generationAgent;
        this.auditAgent = auditAgent;
    }

    public AddIterationResult executeAddIteration(IterationContext context,
                                                   int iterationNumber,
                                                   String iterationObjective) {
        long startTime = System.currentTimeMillis();
        AddIterationResult result = new AddIterationResult(iterationNumber, iterationObjective);

        try {
            // 构建包含前置迭代信息的上下文
            String previousContext = context.getPreviousIterationsContext();

            // 构建系统指令
            String systemPrompt = AddPromptTemplates.buildSystemPromptForIteration(
                    iterationNumber,
                    iterationObjective,
                    previousContext
            );

            // Call generation agent for ADD design with formatted output
            String designOutput = generationAgent.generateWithFormat(
                    "Perform iteration " + iterationNumber + " of architecture design for the Hotel Pricing System according to ADD 3.0 methodology",
                    systemPrompt,
                    AddPromptTemplates.ADD_3_0_FRAMEWORK,
                    null,
                    null
            );

            // Parse outputs of 7 steps
            parseAndSetStepOutputs(result, designOutput);

            // Audit key step (Step 5)
            auditStep5Output(result);

            // Set execution information
            result.setTraceId("trace_" + System.currentTimeMillis());
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        } catch (Exception ex) {
            throw new AgentInvocationException("Architecture Design Agent execution failed: " + ex.getMessage(), ex);
        }

        return result;
    }

    private void parseAndSetStepOutputs(AddIterationResult result, String fullOutput) {
        // Use regex to parse outputs of each step
        Pattern stepPattern = Pattern.compile("--- STEP (\\d+)\\s*(\\w+)?\\s*OUTPUT ---([\\s\\S]*?)(?=--- STEP|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = stepPattern.matcher(fullOutput);

        while (matcher.find()) {
            int stepNum = Integer.parseInt(matcher.group(1));
            String stepOutput = matcher.group(3).trim();

            switch (stepNum) {
                case 1:
                    result.setStep1ReviewInputsOutput(stepOutput);
                    break;
                case 2:
                    result.setStep2DetermineObjectiveOutput(stepOutput);
                    break;
                case 3:
                    result.setStep3SelectElementsOutput(stepOutput);
                    break;
                case 4:
                    result.setStep4SelectDesignConceptsOutput(stepOutput);
                    break;
                case 5:
                    result.setStep5InstantiateElementsOutput(stepOutput);
                    break;
                case 6:
                    handleStep6Output(result, stepOutput);
                    break;
                case 7:
                    result.setStep7AnalyzeDesignOutput(stepOutput);
                    break;
            }
        }

        // If some steps failed to parse, set default values
        if (result.getStep1ReviewInputsOutput() == null) result.setStep1ReviewInputsOutput("[Failed to parse]");
        if (result.getStep2DetermineObjectiveOutput() == null) result.setStep2DetermineObjectiveOutput("[Failed to parse]");
        if (result.getStep3SelectElementsOutput() == null) result.setStep3SelectElementsOutput("[Failed to parse]");
        if (result.getStep4SelectDesignConceptsOutput() == null) result.setStep4SelectDesignConceptsOutput("[Failed to parse]");
        if (result.getStep5InstantiateElementsOutput() == null) result.setStep5InstantiateElementsOutput("[Failed to parse]");
        if (result.getStep7AnalyzeDesignOutput() == null) result.setStep7AnalyzeDesignOutput("[Failed to parse]");
    }

    private void handleStep6Output(AddIterationResult result, String step6Output) {
        // Step 6 contains views and decision records, needs separate handling
        List<String> mermaidViews = extractMermaidBlocks(step6Output);
        result.setStep6ViewOutputs(mermaidViews);

        // Decision recording will be handled separately by DesignDecisionRecorderAgent
    }

    private List<String> extractMermaidBlocks(String content) {
        List<String> mermaidBlocks = new ArrayList<>();
        Pattern mermaidPattern = Pattern.compile("```mermaid\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
        Matcher matcher = mermaidPattern.matcher(content);

        while (matcher.find()) {
            mermaidBlocks.add(matcher.group(1).trim());
        }

        return mermaidBlocks;
    }

    private void auditStep5Output(AddIterationResult result) {
        // Audit the Step 5 architecture instantiation output
        String step5Output = result.getStep5InstantiateElementsOutput();
        if (step5Output != null && !step5Output.isEmpty()) {
            var auditDecision = auditAgent.audit(
                    "Please review the Step 5 output of this architecture design",
                    step5Output
            );

            if (!auditDecision.isApproved()) {
                // If audit fails, try to improve based on feedback
                String feedback = String.join("; ", auditDecision.getReasons());
                String improvedOutput = generationAgent.regenerate(
                        "Please improve the Step 5 architecture design based on audit feedback",
                        result.getStep5InstantiateElementsOutput(),
                        result.getStep5InstantiateElementsOutput(),
                        auditDecision.getReasons(),
                        auditDecision.getRevisedAnswer()
                );
                result.setStep5InstantiateElementsOutput(improvedOutput);
            }
        }
    }
}
