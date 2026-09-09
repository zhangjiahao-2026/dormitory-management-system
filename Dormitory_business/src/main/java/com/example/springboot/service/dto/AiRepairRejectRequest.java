package com.example.springboot.service.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AiRepairRejectRequest {
    @NotBlank(message = "拒绝原因不能为空")
    @Size(max = 500, message = "拒绝原因不能超过500字")
    private String reason;
}
