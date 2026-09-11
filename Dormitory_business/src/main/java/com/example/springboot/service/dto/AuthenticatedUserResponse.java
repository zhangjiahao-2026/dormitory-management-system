package com.example.springboot.service.dto;

import com.example.springboot.entity.Admin;
import com.example.springboot.entity.DormManager;
import com.example.springboot.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Safe user representation returned to browsers. It deliberately has no password field. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUserResponse {
    private String username;
    private String name;
    private Integer age;
    private String gender;
    private String phoneNum;
    private String email;
    private String avatar;
    private Integer dormBuildId;

    public static AuthenticatedUserResponse from(Object user) {
        if (user instanceof Student) {
            Student value = (Student) user;
            return new AuthenticatedUserResponse(value.getUsername(), value.getName(), value.getAge(),
                    value.getGender(), value.getPhoneNum(), value.getEmail(), value.getAvatar(), null);
        }
        if (user instanceof DormManager) {
            DormManager value = (DormManager) user;
            return new AuthenticatedUserResponse(value.getUsername(), value.getName(), value.getAge(),
                    value.getGender(), value.getPhoneNum(), value.getEmail(), value.getAvatar(), value.getDormBuildId());
        }
        if (user instanceof Admin) {
            Admin value = (Admin) user;
            return new AuthenticatedUserResponse(value.getUsername(), value.getName(), value.getAge(),
                    value.getGender(), value.getPhoneNum(), value.getEmail(), value.getAvatar(), null);
        }
        throw new IllegalArgumentException("不支持的用户类型");
    }
}
