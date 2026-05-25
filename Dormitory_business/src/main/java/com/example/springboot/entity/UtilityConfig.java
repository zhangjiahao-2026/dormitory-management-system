package com.example.springboot.entity;

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
@TableName(value = "utility_config")
public class UtilityConfig {

    @TableId(value = "id")
    private Integer id;
    @TableField("electric_limit")
    private BigDecimal electricLimit;
    @TableField("water_limit")
    private BigDecimal waterLimit;
    @TableField("update_time")
    private String updateTime;
}
