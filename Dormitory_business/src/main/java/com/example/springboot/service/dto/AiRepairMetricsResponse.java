package com.example.springboot.service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class AiRepairMetricsResponse {
    @JsonAlias("total_requests")
    private Integer totalRequests;
    @JsonAlias("success_rate")
    private Double successRate;
    @JsonAlias("average_latency_ms")
    private Double averageLatencyMs;
    @JsonAlias("structured_output_rate")
    private Double structuredOutputRate;
    @JsonAlias("human_review_rate")
    private Double humanReviewRate;
    @JsonAlias("positive_feedback_rate")
    private Double positiveFeedbackRate;
    @JsonAlias("top_errors")
    private Map<String, Integer> topErrors = new LinkedHashMap<>();
}
