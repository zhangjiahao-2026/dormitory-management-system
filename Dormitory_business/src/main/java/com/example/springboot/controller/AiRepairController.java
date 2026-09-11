package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.AiRepairService;
import com.example.springboot.service.dto.AiRepairAnalyzeRequest;
import com.example.springboot.service.dto.AiRepairConfirmRequest;
import com.example.springboot.service.dto.AiRepairRejectRequest;
import com.example.springboot.service.dto.AiRepairFeedbackRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@RequestMapping("/repair/ai")
public class AiRepairController {
    @Resource
    private AiRepairService aiRepairService;

    @PostMapping("/analyze")
    public Result<?> analyze(@Valid @RequestBody AiRepairAnalyzeRequest request, HttpSession session) {
        try {
            return Result.success(aiRepairService.analyze(request, session));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error("-1", e.getMessage());
        }
    }

    @GetMapping("/mine")
    public Result<?> mine(HttpSession session) {
        return Result.success(aiRepairService.mine(session));
    }

    @GetMapping("/pending")
    public Result<?> pending(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(defaultValue = "") String search,
                             HttpSession session) {
        return Result.success(aiRepairService.pending(pageNum, pageSize, search, session));
    }

    @PostMapping("/{requestId}/confirm")
    public Result<?> confirm(@PathVariable String requestId,
                             @Valid @RequestBody AiRepairConfirmRequest request,
                             HttpSession session) {
        try {
            return Result.success(aiRepairService.confirm(requestId, request, session));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error("-1", e.getMessage());
        }
    }

    @PostMapping("/{requestId}/reject")
    public Result<?> reject(@PathVariable String requestId,
                            @Valid @RequestBody AiRepairRejectRequest request,
                            HttpSession session) {
        try {
            aiRepairService.reject(requestId, request.getReason(), session);
            return Result.success();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error("-1", e.getMessage());
        }
    }

    @PostMapping("/feedback")
    public Result<?> feedback(@Valid @RequestBody AiRepairFeedbackRequest request, HttpSession session) {
        try {
            aiRepairService.feedback(request, session);
            return Result.success();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error("-1", e.getMessage());
        }
    }

    @GetMapping("/metrics")
    public Result<?> metrics(HttpSession session) {
        try {
            return Result.success(aiRepairService.metrics(session));
        } catch (IllegalStateException e) {
            return Result.error("-1", e.getMessage());
        }
    }
}
