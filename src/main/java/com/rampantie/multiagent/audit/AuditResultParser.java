package com.rampantie.multiagent.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class AuditResultParser {

    private final ObjectMapper objectMapper;

    public AuditResultParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AuditDecision parse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return fallback("审计Agent返回为空");
        }

        String json = extractJson(rawResponse.trim());
        try {
            JsonNode root = objectMapper.readTree(json);
            AuditDecision decision = new AuditDecision();
            decision.setApproved(root.path("approved").asBoolean(false));
            decision.setRiskLevel(parseRiskLevel(root.path("riskLevel").asText("UNKNOWN")));
            decision.setReasons(parseReasons(root.path("reasons")));
            decision.setRevisedAnswer(root.path("revisedAnswer").asText(""));
            if (decision.getReasons().isEmpty()) {
                decision.getReasons().add("审计结果未提供明确原因");
            }
            return decision;
        } catch (Exception ex) {
            return fallback("审计结果解析失败: " + ex.getMessage());
        }
    }

    private AuditDecision fallback(String reason) {
        AuditDecision decision = new AuditDecision();
        decision.setApproved(false);
        decision.setRiskLevel(RiskLevel.HIGH);
        List<String> reasons = new ArrayList<>();
        reasons.add(reason);
        decision.setReasons(reasons);
        decision.setRevisedAnswer("抱歉，当前回答未通过审计，请稍后重试或调整问题。");
        return decision;
    }

    private String extractJson(String text) {
        if (text.startsWith("```")) {
            int start = text.indexOf('{');
            int end = text.lastIndexOf('}');
            if (start >= 0 && end > start) {
                return text.substring(start, end + 1);
            }
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private RiskLevel parseRiskLevel(String value) {
        try {
            return RiskLevel.valueOf(value.trim().toUpperCase());
        } catch (Exception ex) {
            return RiskLevel.UNKNOWN;
        }
    }

    private List<String> parseReasons(JsonNode reasonsNode) {
        List<String> reasons = new ArrayList<>();
        if (reasonsNode == null || reasonsNode.isMissingNode() || reasonsNode.isNull()) {
            return reasons;
        }
        if (reasonsNode.isArray()) {
            Iterator<JsonNode> iterator = reasonsNode.elements();
            while (iterator.hasNext()) {
                String reason = iterator.next().asText("").trim();
                if (!reason.isEmpty()) {
                    reasons.add(reason);
                }
            }
            return reasons;
        }
        String single = reasonsNode.asText("").trim();
        if (!single.isEmpty()) {
            reasons.add(single);
        }
        return reasons;
    }
}
