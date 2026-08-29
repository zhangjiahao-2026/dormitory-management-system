package com.example.springboot.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiServiceAnalyzeResponse {
    @JsonProperty("request_id")
    private String requestId;
    private String category;
    @JsonProperty("category_name")
    private String categoryName;
    private String urgency;
    private Double confidence;
    @JsonProperty("suggested_department")
    private String suggestedDepartment;
    @JsonProperty("suggested_department_name")
    private String suggestedDepartmentName;
    @JsonProperty("recommended_actions")
    private List<String> recommendedActions = new ArrayList<>();
    @JsonProperty("requires_human_review")
    private Boolean requiresHumanReview;
    @JsonProperty("review_reasons")
    private List<String> reviewReasons = new ArrayList<>();
    private List<AiServiceSource> sources = new ArrayList<>();
    private String status;
}
