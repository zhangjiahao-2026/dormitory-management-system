package com.example.springboot.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AiServiceSource {
    private String document;
    private String section;
    @JsonProperty("chunk_id")
    private String chunkId;
    private Double score;
}
