package com.example.springboot.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class AiRepairMetricsResponse {
    @JsonProperty("total_requests")
    private Integer totalRequests;
    @JsonProperty("success_rate")
    private Double successRate;
    @JsonProperty("average_latency_ms")
    private Double averageLatencyMs;
    @JsonProperty("structured_output_rate")
    private Double structuredOutputRate;
    @JsonProperty("human_review_rate")
    private Double humanReviewRate;
    @JsonProperty("positive_feedback_rate")
    private Double positiveFeedbackRate;
    @JsonProperty("top_errors")
    private Map<String, Integer> topErrors = new LinkedHashMap<>();
}
