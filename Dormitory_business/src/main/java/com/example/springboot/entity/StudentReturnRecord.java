package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "student_return_record")
public class StudentReturnRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("student_username")
    private String studentUsername;
    @TableField("student_name")
    private String studentName;
    @TableField("dormbuild_id")
    private Integer dormBuildId;
    @TableField("dormroom_id")
    private Integer dormRoomId;
    @TableField("return_time")
    private String returnTime;
    @TableField("is_late")
    private Integer late;
    @TableField("registrar")
    private String registrar;
    @TableField("remark")
    private String remark;
}
