package com.example.springboot.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AiRepairFeedbackRequest {
    @JsonProperty("request_id")
    private String requestId;
    @NotBlank(message = "反馈评分不能为空")
    private String rating;
    private String reason;
    @JsonProperty("expected_category")
    private String expectedCategory;
    @Size(max = 500, message = "反馈内容不能超过500字")
    private String comment;
}
