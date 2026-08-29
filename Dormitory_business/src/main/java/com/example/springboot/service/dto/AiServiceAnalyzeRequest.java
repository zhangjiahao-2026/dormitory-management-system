package com.example.springboot.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiServiceAnalyzeRequest {
    private String building;
    private String room;
    private String description;
    @JsonProperty("user_id")
    private String userId;
}
