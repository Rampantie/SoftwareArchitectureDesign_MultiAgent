package com.rampantie.multiagent.service;

import com.rampantie.multiagent.domain.AddIterationResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DialogueLogService {

    private static final String LOG_DIR = "./dialogue_logs";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.systemDefault());

    private final List<DialogueEntry> dialogueEntries = new ArrayList<>();
    private final TokenUsageTracker tokenUsageTracker;
    private final String runId;  // 本次运行的唯一 ID

    public DialogueLogService(TokenUsageTracker tokenUsageTracker) {
        // 确保日志目录存在
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
        } catch (IOException ex) {
            System.err.println("无法创建日志目录: " + ex.getMessage());
        }
        this.tokenUsageTracker = tokenUsageTracker;
        this.runId = FILE_TIMESTAMP_FORMATTER.format(Instant.now());
    }

    public void logIterationStart(int iterationNumber, String objective) {
        String message = String.format("""

                ========== Iteration %d Started ==========
                Objective: %s
                Time: %s
                """, iterationNumber, objective, getCurrentTimestamp());
        addEntry(iterationNumber, -1, "IterationOrchestrator", message, "START");
    }

    public void logIterationCompletion(int iterationNumber, AddIterationResult result) {
        String message = result.getSummary();
        addEntry(iterationNumber, -1, "IterationOrchestrator", message, "COMPLETE");
    }

    public void logAgentExecution(int iterationNumber, int stepNumber, String agentName,
                                  String input, String output) {
        String message = String.format("""
                Time: %s
                Agent: %s
                Step: %d
                Input Summary: %s
                Output Summary: %s
                """,
                getCurrentTimestamp(),
                agentName,
                stepNumber,
                limitLength(input, 100),
                limitLength(output, 200));
        addEntry(iterationNumber, stepNumber, agentName, message, "EXECUTION");
    }

    public void logAddStepOutput(int iterationNumber, int stepNumber, String stepName, String output) {
        String message = String.format("""
                #### Step %d: %s

                %s
                """,
                stepNumber, stepName, output);
        addEntry(iterationNumber, stepNumber, "ArchitectureDesignAgent", message, "ADD_STEP");
    }

    public void logAddIterationDetail(int iterationNumber, AddIterationResult result) {
        StringBuilder detailedLog = new StringBuilder();
        detailedLog.append("\n### 📊 ADD 3.0 Detailed Step Outputs\n\n");

        if (result.getStep1ReviewInputsOutput() != null) {
            detailedLog.append("#### Step 1: Review Inputs\n\n")
                    .append(result.getStep1ReviewInputsOutput())
                    .append("\n\n---\n\n");
        }

        if (result.getStep2DetermineObjectiveOutput() != null) {
            detailedLog.append("#### Step 2: Determine Iteration Objective\n\n")
                    .append(result.getStep2DetermineObjectiveOutput())
                    .append("\n\n---\n\n");
        }

        if (result.getStep3SelectElementsOutput() != null) {
            detailedLog.append("#### Step 3: Select System Elements\n\n")
                    .append(result.getStep3SelectElementsOutput())
                    .append("\n\n---\n\n");
        }

        if (result.getStep4SelectDesignConceptsOutput() != null) {
            detailedLog.append("#### Step 4: Select Design Concept\n\n")
                    .append(result.getStep4SelectDesignConceptsOutput())
                    .append("\n\n---\n\n");
        }

        if (result.getStep5InstantiateElementsOutput() != null) {
            detailedLog.append("#### Step 5: Instantiate Architecture Elements\n\n")
                    .append(result.getStep5InstantiateElementsOutput())
                    .append("\n\n---\n\n");
        }

        // Add views
        if (!result.getStep6ViewOutputs().isEmpty()) {
            detailedLog.append("#### Step 6a: Architecture Views\n\n");
            for (int i = 0; i < result.getStep6ViewOutputs().size(); i++) {
                detailedLog.append("**View ").append(i + 1).append(":**\n\n")
                        .append(result.getStep6ViewOutputs().get(i))
                        .append("\n\n");
            }
            detailedLog.append("---\n\n");
        }

        // Add decisions
        if (!result.getStep6Decisions().isEmpty()) {
            detailedLog.append("#### Step 6b: Architecture Decision Records\n\n");
            detailedLog.append("| Decision ID | Title | Context | Choice | Rationale | Quality Attributes |\n");
            detailedLog.append("|--------|------|------|------|------|----------|\n");
            for (var decision : result.getStep6Decisions()) {
                detailedLog.append("| ").append(decision.getId()).append(" | ")
                        .append(decision.getDecisionTitle()).append(" | ")
                        .append(limitLength(decision.getDecisionContext(), 30)).append(" | ")
                        .append(limitLength(decision.getChosenSolution(), 30)).append(" | ")
                        .append(limitLength(decision.getRationale(), 30)).append(" | ")
                        .append(String.join(", ", decision.getRelatedQualityAttributes()))
                        .append(" |\n");
            }
            detailedLog.append("\n---\n\n");
        }

        if (result.getStep7AnalyzeDesignOutput() != null) {
            detailedLog.append("#### Step 7: Analyze Design\n\n")
                    .append(result.getStep7AnalyzeDesignOutput())
                    .append("\n\n");
        }

        addEntry(iterationNumber, -1, "ArchitectureDesignAgent", detailedLog.toString(), "ITERATION_DETAIL");
    }

    public void logDecision(int iterationNumber, String decisionTitle, String rationale) {
        String message = String.format("""
                Time: %s
                Decision: %s
                Rationale: %s
                """,
                getCurrentTimestamp(),
                decisionTitle,
                rationale);
        addEntry(iterationNumber, 6, "DesignDecisionRecorder", message, "DECISION");
    }

    public void addEntry(int iterationNumber, int stepNumber, String agentName, String content, String type) {
        DialogueEntry entry = new DialogueEntry(iterationNumber, stepNumber, agentName, content, type);
        dialogueEntries.add(entry);

        // 同时写入文件
        writeEntryToFile(entry);
    }

    public List<DialogueEntry> getAllEntries() {
        return new ArrayList<>(dialogueEntries);
    }

    public List<DialogueEntry> getEntriesByIteration(int iterationNumber) {
        return dialogueEntries.stream()
                .filter(e -> e.getIterationNumber() == iterationNumber)
                .toList();
    }

    public TokenUsageTracker getTokenUsageTracker() {
        return tokenUsageTracker;
    }

    public String exportAsMarkdown(int interactionRounds) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Multi-Agent Architecture Design System - Complete Dialogue Log and Design Documentation\n\n");
        sb.append("**Generation Time**: ").append(getCurrentTimestamp()).append("\n\n");
        sb.append("---\n\n");

        int currentIteration = -1;
        for (DialogueEntry entry : dialogueEntries) {
            if (entry.getIterationNumber() != currentIteration) {
                currentIteration = entry.getIterationNumber();
                if (currentIteration >= 1) {
                    sb.append("\n\n## Iteration ").append(currentIteration).append("\n\n");
                }
            }

            // Use different formats based on entry type
            switch (entry.getType()) {
                case "START":
                    sb.append(entry.getContent());
                    break;
                case "ADD_STEP":
                case "ITERATION_DETAIL":
                    sb.append(entry.getContent());
                    break;
                case "DECISION":
                    sb.append("### 🏗️ Architecture Decision - ").append(entry.getAgentName()).append("\n\n");
                    sb.append(entry.getContent()).append("\n\n");
                    break;
                case "EXECUTION":
                    sb.append("### ⚙️ Agent Execution - ").append(entry.getAgentName());
                    if (entry.getStepNumber() >= 0) {
                        sb.append(" (Step ").append(entry.getStepNumber()).append(")");
                    }
                    sb.append("\n\n");
                    sb.append(entry.getContent()).append("\n\n");
                    break;
                case "COMPLETE":
                    sb.append("### ✅ Iteration Completed\n\n");
                    sb.append(entry.getContent()).append("\n\n");
                    break;
                default:
                    sb.append("### ").append(entry.getType()).append(" - ").append(entry.getAgentName());
                    if (entry.getStepNumber() >= 0) {
                        sb.append(" (Step ").append(entry.getStepNumber()).append(")");
                    }
                    sb.append("\n\n");
                    sb.append(entry.getContent()).append("\n\n");
            }
        }

        // Add interaction cost analysis
        sb.append("\n\n---\n\n");
        sb.append(tokenUsageTracker.generateCostAnalysisReport(
                "Multi-Agent System (ADD 3.0)",
                "Qwen3-235B-A22B-instruct-2507",
                interactionRounds
        ));

        return sb.toString();
    }

    public String exportAsMarkdown() {
        // Use actual interaction count
        return exportAsMarkdown(tokenUsageTracker.getInteractionCallCount());
    }

    public void saveToFile(String filename, int interactionRounds) throws IOException {
        String content = exportAsMarkdown(interactionRounds);

        // Generate timestamped filename (if no extension specified)
        String timestampedFilename;
        if (filename.contains(".")) {
            String[] parts = filename.split("\\.");
            timestampedFilename = parts[0] + "_" + runId + "." + parts[1];
        } else {
            timestampedFilename = filename + "_" + runId + ".md";
        }

        // Save timestamped file
        Path timestampedPath = Paths.get(LOG_DIR, timestampedFilename);
        Files.writeString(timestampedPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("✓ Dialogue log saved: " + timestampedPath);

        // Also save as "latest" file for quick viewing
        Path latestPath = Paths.get(LOG_DIR, "latest_" + filename);
        Files.writeString(latestPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("✓ Latest log saved: " + latestPath);

        // Update index file
        updateDialogueIndex(timestampedFilename);
    }

    public void saveToFile(String filename) throws IOException {
        // Backward compatible: use actual interaction count
        saveToFile(filename, tokenUsageTracker.getInteractionCallCount());
    }

    private void updateDialogueIndex(String newLogFile) {
        try {
            Path indexPath = Paths.get(LOG_DIR, "dialogue_index.md");
            StringBuilder indexContent = new StringBuilder();

            // 读取现有索引
            if (Files.exists(indexPath)) {
                String existing = Files.readString(indexPath);
                indexContent.append(existing);
            } else {
                indexContent.append("# 对话日志索引\n\n");
                indexContent.append("所有运行的对话日志列表：\n\n");
            }

            // 添加新条目
            String newEntry = String.format(
                    "- [%s](%s) - %s\n",
                    newLogFile,
                    newLogFile,
                    getCurrentTimestamp()
            );

            // 如果不是第一次添加，插入到列表中
            if (indexContent.toString().contains("- [")) {
                String[] lines = indexContent.toString().split("\n");
                StringBuilder newContent = new StringBuilder();
                for (int i = 0; i < lines.length; i++) {
                    newContent.append(lines[i]).append("\n");
                    // 在第一个日志条目前插入新条目
                    if (i > 1 && lines[i].startsWith("- [") && !lines[i].equals(newEntry.trim())) {
                        newContent.insert(0, newEntry);
                        break;
                    }
                }
                Files.writeString(indexPath, newContent.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                indexContent.append(newEntry);
                Files.writeString(indexPath, indexContent.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            System.out.println("✓ 索引已更新: " + indexPath);
        } catch (IOException ex) {
            System.err.println("无法更新索引文件: " + ex.getMessage());
        }
    }

    private void writeEntryToFile(DialogueEntry entry) {
        try {
            Path filepath = Paths.get(LOG_DIR, "latest_dialogue.log");
            String line = String.format("[%s] [迭代%d] [%s] %s%n",
                    getCurrentTimestamp(),
                    entry.getIterationNumber(),
                    entry.getAgentName(),
                    limitLength(entry.getContent().replace("\n", " "), 200));

            Files.writeString(filepath, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            System.err.println("无法写入日志文件: " + ex.getMessage());
        }
    }

    private String getCurrentTimestamp() {
        return TIMESTAMP_FORMATTER.format(Instant.now());
    }

    private String limitLength(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    public static class DialogueEntry {
        private final int iterationNumber;
        private final int stepNumber;
        private final String agentName;
        private final String content;
        private final String type;
        private final Instant timestamp;

        public DialogueEntry(int iterationNumber, int stepNumber, String agentName, String content, String type) {
            this.iterationNumber = iterationNumber;
            this.stepNumber = stepNumber;
            this.agentName = agentName;
            this.content = content;
            this.type = type;
            this.timestamp = Instant.now();
        }

        public int getIterationNumber() { return iterationNumber; }
        public int getStepNumber() { return stepNumber; }
        public String getAgentName() { return agentName; }
        public String getContent() { return content; }
        public String getType() { return type; }
        public Instant getTimestamp() { return timestamp; }
    }
}
