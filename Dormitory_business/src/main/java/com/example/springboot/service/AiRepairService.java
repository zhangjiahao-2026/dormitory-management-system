package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.AiRepairRequest;
import com.example.springboot.service.dto.AiRepairAnalyzeRequest;
import com.example.springboot.service.dto.AiRepairConfirmRequest;
import com.example.springboot.service.dto.AiRepairFeedbackRequest;
import com.example.springboot.service.dto.AiRepairMetricsResponse;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

public interface AiRepairService {
    Map<String, Object> analyze(AiRepairAnalyzeRequest request, HttpSession session);
    List<AiRepairRequest> mine(HttpSession session);
    Page<AiRepairRequest> pending(Integer pageNum, Integer pageSize, String search, HttpSession session);
    Map<String, Object> confirm(String requestId, AiRepairConfirmRequest request, HttpSession session);
    void reject(String requestId, String reason, HttpSession session);
    void feedback(AiRepairFeedbackRequest request, HttpSession session);
    AiRepairMetricsResponse metrics(HttpSession session);
}
