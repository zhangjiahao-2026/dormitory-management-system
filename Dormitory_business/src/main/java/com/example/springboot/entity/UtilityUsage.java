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
@TableName(value = "utility_usage")
public class UtilityUsage {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("dormroom_id")
    private Integer dormRoomId;
    @TableField("dormbuild_id")
    private Integer dormBuildId;
    @TableField("electric_usage")
    private BigDecimal electricUsage;
    @TableField("water_usage")
    private BigDecimal waterUsage;
    @TableField("collect_time")
    private String collectTime;
    @TableField("collect_source")
    private String collectSource;
}
