package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 学生
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "student")
public class Student {

    @NotBlank(message = "用户名不能为空")
    @TableId(value = "username")
    private String username;
    @TableField("password")
    private String password;
    @NotBlank(message = "姓名不能为空")
    @TableField("name")
    private String name;
    @TableField("age")
    private int age;
    @TableField("gender")
    private String gender;
    @TableField("phone_num")
    private String phoneNum;
    @TableField("email")
    private String email;
    @TableField("avatar")
    private String avatar;
}
