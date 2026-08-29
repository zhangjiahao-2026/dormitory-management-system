package com.example.springboot.service;

import com.example.springboot.service.dto.AiServiceAnalyzeRequest;
import com.example.springboot.service.dto.AiServiceAnalyzeResponse;

public interface AiServiceClient {
    AiServiceAnalyzeResponse analyze(AiServiceAnalyzeRequest request);
}
