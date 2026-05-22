package com.rampantie.multiagent.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditResultParserTest {

    private AuditResultParser parser;

    @BeforeEach
    void setUp() {
        parser = new AuditResultParser(new ObjectMapper());
    }

    @Test
    void shouldParseValidJson() {
        String raw = """
                {
                  "approved": true,
                  "riskLevel": "LOW",
                  "reasons": ["内容完整"],
                  "revisedAnswer": ""
                }
                """;

        AuditDecision decision = parser.parse(raw);

        assertThat(decision.isApproved()).isTrue();
        assertThat(decision.getRiskLevel()).isEqualTo(RiskLevel.LOW);
        assertThat(decision.getReasons()).containsExactly("内容完整");
    }

    @Test
    void shouldParseJsonWrappedByMarkdown() {
        String raw = """
                ```json
                {
                  "approved": false,
                  "riskLevel": "HIGH",
                  "reasons": ["存在风险"],
                  "revisedAnswer": "请调整问题后重试"
                }
                ```
                """;

        AuditDecision decision = parser.parse(raw);

        assertThat(decision.isApproved()).isFalse();
        assertThat(decision.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(decision.getRevisedAnswer()).isEqualTo("请调整问题后重试");
    }

    @Test
    void shouldFallbackWhenJsonInvalid() {
        AuditDecision decision = parser.parse("not-json");

        assertThat(decision.isApproved()).isFalse();
        assertThat(decision.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(decision.getReasons()).isNotEmpty();
    }
}
