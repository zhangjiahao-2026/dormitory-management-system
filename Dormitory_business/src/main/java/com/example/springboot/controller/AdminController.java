package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.common.SessionAuth;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.User;
import com.example.springboot.service.AdminService;
import com.example.springboot.service.dto.AuthenticatedUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Resource
    private AdminService adminService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody User user, HttpSession session) {
        Admin admin = adminService.adminLogin(user.getUsername(), user.getPassword());
        if (admin != null) {
            log.info("管理员登录成功: {}", user.getUsername());
            session.setAttribute("Identity", "admin");
            AuthenticatedUserResponse response = AuthenticatedUserResponse.from(admin);
            admin.setPassword(null);
            session.setAttribute("User", admin);
            return Result.success(response);
        } else {
            return Result.error("-1", "用户名或密码错误");
        }
    }

    /**
     * 管理员信息更新
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Admin admin, HttpSession session) {
        SessionAuth.requireRole(session, "admin");
        SessionAuth.requireSelfOrAdmin(session, admin.getUsername());
        int i = adminService.updateAdmin(admin);
        if (i == 1) {
            Admin refreshed = adminService.getById(admin.getUsername());
            refreshed.setPassword(null);
            session.setAttribute("User", refreshed);
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }
}
