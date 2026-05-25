package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "utility_alert")
public class UtilityAlert {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("dormroom_id")
    private Integer dormRoomId;
    @TableField("dormbuild_id")
    private Integer dormBuildId;
    @TableField("alert_type")
    private String alertType;
    @TableField("trigger_value")
    private BigDecimal triggerValue;
    @TableField("limit_value")
    private BigDecimal limitValue;
    @TableField("status")
    private String status;
    @TableField("create_time")
    private String createTime;
    @TableField("handle_time")
    private String handleTime;
}
