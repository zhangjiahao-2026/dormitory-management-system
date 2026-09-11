package com.example.springboot.common;

import com.example.springboot.entity.Admin;
import com.example.springboot.entity.DormManager;
import com.example.springboot.entity.Student;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Objects;

public final class SessionAuth {

    private SessionAuth() {
    }

    public static String identity(HttpSession session) {
        Object value = session == null ? null : session.getAttribute("Identity");
        return value == null ? null : value.toString();
    }

    public static String username(HttpSession session) {
        Object user = session == null ? null : session.getAttribute("User");
        if (user instanceof Student) return ((Student) user).getUsername();
        if (user instanceof DormManager) return ((DormManager) user).getUsername();
        if (user instanceof Admin) return ((Admin) user).getUsername();
        return null;
    }

    public static Integer dormBuildId(HttpSession session) {
        Object user = session == null ? null : session.getAttribute("User");
        return user instanceof DormManager ? ((DormManager) user).getDormBuildId() : null;
    }

    public static boolean hasRole(HttpSession session, String... roles) {
        String identity = identity(session);
        return identity != null && Arrays.asList(roles).contains(identity);
    }

    public static void requireRole(HttpSession session, String... roles) {
        if (!hasRole(session, roles)) {
            throw new SecurityException("无权执行此操作");
        }
    }

    public static void requireSelfOrAdmin(HttpSession session, String targetUsername) {
        String currentUsername = username(session);
        if (!hasRole(session, "admin")
                && (currentUsername == null || !Objects.equals(currentUsername, targetUsername))) {
            throw new SecurityException("只能操作自己的资料");
        }
    }

    public static void requireManagedBuild(HttpSession session, Integer targetBuildId) {
        if (hasRole(session, "admin")) return;
        if (!hasRole(session, "dormManager") || !Objects.equals(dormBuildId(session), targetBuildId)) {
            throw new SecurityException("无权操作其他楼栋的数据");
        }
    }
}
