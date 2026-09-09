package com.example.springboot.service.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AiRepairAnalyzeRequest {
    @NotBlank(message = "报修标题不能为空")
    @Size(max = 200, message = "报修标题不能超过200字")
    private String title;

    @NotBlank(message = "问题描述不能为空")
    @Size(min = 4, max = 2000, message = "问题描述长度应为4到2000字")
    private String description;
}
