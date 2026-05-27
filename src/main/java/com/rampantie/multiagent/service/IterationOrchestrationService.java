package com.rampantie.multiagent.service;

import com.rampantie.multiagent.agent.ArchitectureDesignAgent;
import com.rampantie.multiagent.agent.ArchitectureViewGeneratorAgent;
import com.rampantie.multiagent.agent.DesignDecisionRecorderAgent;
import com.rampantie.multiagent.domain.AddIterationResult;
import com.rampantie.multiagent.domain.DesignDecision;
import com.rampantie.multiagent.domain.IterationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IterationOrchestrationService {

    private final ArchitectureDesignAgent architectureDesignAgent;
    private final DesignDecisionRecorderAgent designDecisionRecorderAgent;
    private final DialogueLogService dialogueLogService;
    private final ArchitectureViewGeneratorAgent archViewGeneratorAgent;

    public IterationOrchestrationService(ArchitectureDesignAgent architectureDesignAgent,
                                        DesignDecisionRecorderAgent designDecisionRecorderAgent,
                                        DialogueLogService dialogueLogService,
                                        ArchitectureViewGeneratorAgent archViewGeneratorAgent) {
        this.architectureDesignAgent = architectureDesignAgent;
        this.designDecisionRecorderAgent = designDecisionRecorderAgent;
        this.dialogueLogService = dialogueLogService;
        this.archViewGeneratorAgent = archViewGeneratorAgent;
    }

    public List<AddIterationResult> executeAllIterations() {
        List<AddIterationResult> allResults = new ArrayList<>();
        IterationContext context = new IterationContext(1);

        // 4 iterations' objectives
        String[] iterationObjectives = {
                "Establish overall system structure - Define top-level architecture and core modules",
                "Identify architecture supporting main functions - Refine implementation for 6 HPS use cases",
                "Handle reliability and availability quality attributes - Design high-availability, high-reliability system",
                "Handle development and operations - Deployment architecture, monitoring, CI/CD, team allocation"
        };

        for (int iteration = 1; iteration <= 4; iteration++) {
            try {
                dialogueLogService.logIterationStart(iteration, iterationObjectives[iteration - 1]);

                // 更新迭代上下文
                context = new IterationContext(iteration);
                if (iteration > 1) {
                    // 添加前置迭代的结果
                    for (int i = 1; i < iteration; i++) {
                        context.addCompletedIteration(i, allResults.get(i - 1));
                    }
                }

                // 执行本次迭代的ADD设计
                AddIterationResult iterationResult = architectureDesignAgent.executeAddIteration(
                        context,
                        iteration,
                        iterationObjectives[iteration - 1]
                );

                // 生成架构视图
                generateArchitectureViews(iterationResult, iteration);

                // 调用DesignDecisionRecorderAgent提炼决策
                List<DesignDecision> decisions = designDecisionRecorderAgent.recordDecisions(
                        iteration,
                        iterationResult.getStep4SelectDesignConceptsOutput(),
                        iterationResult.getStep4SelectDesignConceptsOutput(),
                        iterationResult.getStep5InstantiateElementsOutput()
                );
                iterationResult.setStep6Decisions(decisions);

                // 记录完整的设计内容到对话日志（最重要）
                dialogueLogService.logAddIterationDetail(iteration, iterationResult);

                // 记录决策到日志
                for (DesignDecision decision : decisions) {
                    dialogueLogService.logDecision(iteration, decision.getDecisionTitle(), decision.getRationale());
                }

                // 记录迭代完成
                dialogueLogService.logIterationCompletion(iteration, iterationResult);
                allResults.add(iterationResult);

                System.out.println(iterationResult.getSummary());

            } catch (Exception ex) {
                System.err.println("Iteration " + iteration + " execution failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        // Export dialogue log after all iterations completed
        try {
            // Use actual interaction count (actual API calls)
            int realInteractionCount = dialogueLogService.getTokenUsageTracker().getInteractionCallCount();
            dialogueLogService.saveToFile("complete_dialogue_log.md", realInteractionCount);
            System.out.println("✓ Dialogue log saved to: ./dialogue_logs/complete_dialogue_log.md");
            System.out.println("✓ Actual interaction count: " + realInteractionCount);
        } catch (Exception ex) {
            System.err.println("Failed to save dialogue log: " + ex.getMessage());
        }

        return allResults;
    }

    public AddIterationResult executeSingleIteration(int iterationNumber, String objective) {
        IterationContext context = new IterationContext(iterationNumber);

        dialogueLogService.logIterationStart(iterationNumber, objective);

        AddIterationResult result = architectureDesignAgent.executeAddIteration(
                context,
                iterationNumber,
                objective
        );

        dialogueLogService.logIterationCompletion(iterationNumber, result);

        return result;
    }

    private void generateArchitectureViews(AddIterationResult iterationResult, int iterationNumber) {
        try {
            List<String> views = new ArrayList<>();

            switch (iterationNumber) {
                case 1:
                    // 迭代1：生成C1上下文图 + C2容器图
                    String c1View = archViewGeneratorAgent.generateMermaidDiagram(
                            iterationResult.getStep5InstantiateElementsOutput(),
                            "C1 System Context Diagram"
                    );
                    views.add(c1View);

                    String c2View = archViewGeneratorAgent.generateMermaidDiagram(
                            iterationResult.getStep5InstantiateElementsOutput(),
                            "C2 Container Diagram"
                    );
                    views.add(c2View);
                    break;

                case 2:
                case 3:
                    // 迭代2/3：生成C3组件图 + 时序图
                    String c3View = archViewGeneratorAgent.generateMermaidDiagram(
                            iterationResult.getStep5InstantiateElementsOutput(),
                            "C3 Component Diagram"
                    );
                    views.add(c3View);

                    String sequenceView = archViewGeneratorAgent.generateMermaidDiagram(
                            iterationResult.getStep5InstantiateElementsOutput(),
                            "Sequence Diagram (Price Update Flow)"
                    );
                    views.add(sequenceView);
                    break;

                case 4:
                    // 迭代4：生成部署图 + 监控架构图
                    String deployView = archViewGeneratorAgent.generateMermaidDiagram(
                            iterationResult.getStep5InstantiateElementsOutput(),
                            "Deployment Diagram"
                    );
                    views.add(deployView);

                    String monitoringView = archViewGeneratorAgent.generateMermaidDiagram(
                            iterationResult.getStep5InstantiateElementsOutput(),
                            "Monitoring Architecture Diagram"
                    );
                    views.add(monitoringView);
                    break;
            }

            iterationResult.setStep6ViewOutputs(views);
            System.out.println("✓ Iteration " + iterationNumber + " generated " + views.size() + " Mermaid diagrams");

        } catch (Exception ex) {
            System.err.println("⚠️ Diagram generation failed: " + ex.getMessage());
            // Do not interrupt the flow, continue with subsequent steps
        }
    }
}
