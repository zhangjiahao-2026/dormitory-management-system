package com.example.springboot.service.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AiRepairConfirmRequest {
    @NotBlank(message = "确认类别不能为空")
    private String category;
    @NotBlank(message = "确认紧急程度不能为空")
    private String urgency;
    @NotBlank(message = "确认部门不能为空")
    private String department;
    @Size(max = 500, message = "审核备注不能超过500字")
    private String operatorComment;
}
