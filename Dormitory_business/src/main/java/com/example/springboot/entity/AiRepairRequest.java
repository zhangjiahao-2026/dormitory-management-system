package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ai_repair_request")
public class AiRepairRequest {
    @TableId(value = "request_id", type = IdType.INPUT)
    private String requestId;
    @TableField("applicant_username")
    private String applicantUsername;
    @TableField("applicant_name")
    private String applicantName;
    @TableField("dormbuild_id")
    private Integer dormBuildId;
    @TableField("dormroom_id")
    private Integer dormRoomId;
    @TableField("title")
    private String title;
    @TableField("content")
    private String content;
    @TableField("category")
    private String category;
    @TableField("urgency")
    private String urgency;
    @TableField("confidence")
    private Double confidence;
    @TableField("department")
    private String department;
    @TableField("department_name")
    private String departmentName;
    @TableField("recommended_actions")
    private String recommendedActions;
    @TableField("review_reasons")
    private String reviewReasons;
    @TableField("sources")
    private String sources;
    @TableField("ai_status")
    private String aiStatus;
    @TableField("status")
    private String status;
    @TableField("created_at")
    private String createdAt;
    @TableField("reviewed_by")
    private String reviewedBy;
    @TableField("reviewed_at")
    private String reviewedAt;
    @TableField("operator_comment")
    private String operatorComment;
    @TableField("repair_id")
    private Integer repairId;
}
