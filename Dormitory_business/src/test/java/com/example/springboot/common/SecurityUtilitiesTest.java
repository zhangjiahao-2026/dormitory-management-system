package com.example.springboot.common;

import com.example.springboot.entity.DormManager;
import com.example.springboot.entity.Student;
import com.example.springboot.service.dto.AuthenticatedUserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityUtilitiesTest {

    @Test
    void bcryptSupportsNewPasswordsAndLegacyMd5Migration() {
        String bcrypt = PasswordUtil.encode("123456");
        assertNotEquals("123456", bcrypt);
        assertTrue(PasswordUtil.matches("123456", bcrypt));
        assertTrue(PasswordUtil.matches("123456", MD5Util.md5("123456")));
        assertTrue(PasswordUtil.needsUpgrade(MD5Util.md5("123456")));
        assertFalse(PasswordUtil.needsUpgrade(bcrypt));
    }

    @Test
    void entitiesAndLoginResponseNeverSerializePassword() throws Exception {
        Student student = new Student("stu001", "secret", "李同学", 20, "男", "1", "a@b.com", null);
        String entityJson = new ObjectMapper().writeValueAsString(student);
        String responseJson = new ObjectMapper().writeValueAsString(AuthenticatedUserResponse.from(student));
        assertFalse(entityJson.contains("password"));
        assertFalse(entityJson.contains("secret"));
        assertFalse(responseJson.contains("password"));
    }

    @Test
    void managerCanOnlyOperateOwnBuilding() {
        DormManager manager = new DormManager("manager1", null, 200, "宿管", "男", 40, null, null, null);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("Identity", "dormManager");
        session.setAttribute("User", manager);
        SessionAuth.requireManagedBuild(session, 200);
        assertThrows(SecurityException.class, () -> SessionAuth.requireManagedBuild(session, 201));
    }
}
