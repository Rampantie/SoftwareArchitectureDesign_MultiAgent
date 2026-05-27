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

            // 调用生成Agent进行ADD设计，指定输出格式
            String designOutput = generationAgent.generateWithFormat(
                    "请根据酒店定价系统的需求和约束，按照ADD 3.0方法进行第" + iterationNumber + "次迭代的架构设计",
                    systemPrompt,
                    AddPromptTemplates.ADD_3_0_FRAMEWORK,
                    null,
                    null
            );

            // 解析7个步骤的输出
            parseAndSetStepOutputs(result, designOutput);

            // 审计关键步骤（步骤5）
            auditStep5Output(result);

            // 设置执行信息
            result.setTraceId("trace_" + System.currentTimeMillis());
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        } catch (Exception ex) {
            throw new AgentInvocationException("架构设计Agent执行失败: " + ex.getMessage(), ex);
        }

        return result;
    }

    private void parseAndSetStepOutputs(AddIterationResult result, String fullOutput) {
        // 使用正则表达式解析各个步骤的输出
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

        // 如果某些步骤解析失败，设置默认值
        if (result.getStep1ReviewInputsOutput() == null) result.setStep1ReviewInputsOutput("[未能解析]");
        if (result.getStep2DetermineObjectiveOutput() == null) result.setStep2DetermineObjectiveOutput("[未能解析]");
        if (result.getStep3SelectElementsOutput() == null) result.setStep3SelectElementsOutput("[未能解析]");
        if (result.getStep4SelectDesignConceptsOutput() == null) result.setStep4SelectDesignConceptsOutput("[未能解析]");
        if (result.getStep5InstantiateElementsOutput() == null) result.setStep5InstantiateElementsOutput("[未能解析]");
        if (result.getStep7AnalyzeDesignOutput() == null) result.setStep7AnalyzeDesignOutput("[未能解析]");
    }

    private void handleStep6Output(AddIterationResult result, String step6Output) {
        // 步骤6包含视图和决策记录，需要分离处理
        List<String> mermaidViews = extractMermaidBlocks(step6Output);
        result.setStep6ViewOutputs(mermaidViews);

        // 决策记录会由DesignDecisionRecorderAgent单独处理
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
        // 对步骤5的架构实例化输出进行审计
        String step5Output = result.getStep5InstantiateElementsOutput();
        if (step5Output != null && !step5Output.isEmpty()) {
            var auditDecision = auditAgent.audit(
                    "请审查这个架构设计的步骤5输出",
                    step5Output
            );

            if (!auditDecision.isApproved()) {
                // 如果审计不通过，尝试根据反馈改进
                String feedback = String.join("；", auditDecision.getReasons());
                String improvedOutput = generationAgent.regenerate(
                        "请根据审计反馈改进步骤5的架构设计",
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
