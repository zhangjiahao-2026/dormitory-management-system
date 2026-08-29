package com.example.springboot.service;

import com.example.springboot.service.dto.AiServiceAnalyzeRequest;
import com.example.springboot.service.dto.AiServiceAnalyzeResponse;
import com.example.springboot.service.dto.AiRepairFeedbackRequest;
import com.example.springboot.service.dto.AiRepairMetricsResponse;

public interface AiServiceClient {
    AiServiceAnalyzeResponse analyze(AiServiceAnalyzeRequest request);
    void feedback(AiRepairFeedbackRequest request);
    AiRepairMetricsResponse metrics();
}
