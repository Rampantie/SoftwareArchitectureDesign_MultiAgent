package com.rampantie.multiagent.domain;

import java.util.HashMap;
import java.util.Map;

public class IterationContext {
    private int currentIterationNumber;
    private Map<Integer, AddIterationResult> completedIterations;
    private AddIterationResult currentIterationResult;

    public IterationContext(int iterationNumber) {
        this.currentIterationNumber = iterationNumber;
        this.completedIterations = new HashMap<>();
        this.currentIterationResult = new AddIterationResult(iterationNumber, "");
    }

    public void addCompletedIteration(int number, AddIterationResult result) {
        this.completedIterations.put(number, result);
    }

    public AddIterationResult getPreviousIterationResult(int iterationNumber) {
        return completedIterations.get(iterationNumber - 1);
    }

    public String getPreviousIterationsContext() {
        if (completedIterations.isEmpty()) {
            return "这是第一次迭代，没有前置设计。";
        }

        StringBuilder context = new StringBuilder("前置迭代的设计成果:\n");
        for (int i = 1; i < currentIterationNumber; i++) {
            AddIterationResult prev = completedIterations.get(i);
            if (prev != null) {
                context.append(String.format("""

                        迭代%d: %s
                        - 步骤5结果摘要: %s
                        """,
                        i, prev.getIterationObjective(),
                        limitLength(prev.getStep5InstantiateElementsOutput(), 200)));
            }
        }
        return context.toString();
    }

    private String limitLength(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    // Getters
    public int getCurrentIterationNumber() { return currentIterationNumber; }
    public Map<Integer, AddIterationResult> getCompletedIterations() { return completedIterations; }
    public AddIterationResult getCurrentIterationResult() { return currentIterationResult; }

    // Setters
    public void setCurrentIterationResult(AddIterationResult result) { this.currentIterationResult = result; }
}
