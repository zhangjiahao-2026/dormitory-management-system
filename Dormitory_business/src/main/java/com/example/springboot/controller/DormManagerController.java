package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.common.SessionAuth;
import com.example.springboot.entity.DormManager;
import com.example.springboot.entity.User;
import com.example.springboot.service.DormManagerService;
import com.example.springboot.service.dto.AuthenticatedUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@RequestMapping("/dormManager")
public class DormManagerController {

    private static final Logger log = LoggerFactory.getLogger(DormManagerController.class);

    @Resource
    private DormManagerService dormManagerService;

    /**
     * 宿管添加
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody DormManager dormManager, HttpSession session) {
        SessionAuth.requireRole(session, "admin");
        int i = dormManagerService.addNewDormManager(dormManager);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "添加失败");
        }
    }

    /**
     * 宿管信息更新
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody DormManager dormManager, HttpSession session) {
        SessionAuth.requireSelfOrAdmin(session, dormManager.getUsername());
        if (!SessionAuth.hasRole(session, "admin")) {
            dormManager.setDormBuildId(SessionAuth.dormBuildId(session));
        }
        int i = dormManagerService.updateNewDormManager(dormManager);
        if (i == 1) {
            if (dormManager.getUsername().equals(SessionAuth.username(session))) {
                DormManager refreshed = dormManagerService.getById(dormManager.getUsername());
                refreshed.setPassword(null);
                session.setAttribute("User", refreshed);
            }
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    /**
     * 宿管删除
     */
    @DeleteMapping("/delete/{username}")
    public Result<?> delete(@PathVariable String username, HttpSession session) {
        SessionAuth.requireRole(session, "admin");
        int i = dormManagerService.deleteDormManager(username);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 宿管查找
     */
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search,
                              HttpSession session) {
        SessionAuth.requireRole(session, "admin");
        Page page = dormManagerService.find(pageNum, pageSize, search);
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 宿管登录
     */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody User user, HttpSession session) {
        DormManager dormManager = dormManagerService.dormManagerLogin(user.getUsername(), user.getPassword());
        if (dormManager != null) {
            log.info("宿管登录成功: {}", user.getUsername());
            //存入session
            session.setAttribute("Identity", "dormManager");
            AuthenticatedUserResponse response = AuthenticatedUserResponse.from(dormManager);
            dormManager.setPassword(null);
            session.setAttribute("User", dormManager);
            return Result.success(response);
        } else {
            return Result.error("-1", "用户名或密码错误");
        }
    }
}
