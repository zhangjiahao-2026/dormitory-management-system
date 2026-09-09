package com.example.springboot.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class AiRepairMetricsResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void readsSnakeCaseFromAiServiceAndWritesCamelCaseForFrontend() throws Exception {
        String payload = "{\"total_requests\":23,\"success_rate\":1.0,"
                + "\"average_latency_ms\":2.09,\"structured_output_rate\":1.0,"
                + "\"human_review_rate\":0.3043,\"positive_feedback_rate\":null,"
                + "\"top_errors\":{}}";

        AiRepairMetricsResponse response = objectMapper.readValue(payload, AiRepairMetricsResponse.class);
        assertThat(response.getTotalRequests()).isEqualTo(23);
        assertThat(response.getHumanReviewRate()).isEqualTo(0.3043);

        JsonNode frontendJson = objectMapper.readTree(objectMapper.writeValueAsString(response));
        assertThat(frontendJson.get("totalRequests").asInt()).isEqualTo(23);
        assertThat(frontendJson.get("successRate").asDouble()).isEqualTo(1.0);
        assertThat(frontendJson.get("averageLatencyMs").asDouble()).isEqualTo(2.09);
        assertThat(frontendJson.has("total_requests")).isFalse();
    }
}
