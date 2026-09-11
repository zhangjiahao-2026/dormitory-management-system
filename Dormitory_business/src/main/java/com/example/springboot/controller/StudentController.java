package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.common.SessionAuth;
import com.example.springboot.entity.Student;
import com.example.springboot.entity.User;
import com.example.springboot.service.StudentService;
import com.example.springboot.service.dto.AuthenticatedUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@RequestMapping("/stu")
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    @Resource
    private StudentService studentService;

    /**
     * 添加学生信息
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Student student, HttpSession session) {
        SessionAuth.requireRole(session, "admin");
        int i = studentService.addNewStudent(student);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "添加失败");
        }

    }

    /**
     * 更新学生信息
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Student student, HttpSession session) {
        SessionAuth.requireSelfOrAdmin(session, student.getUsername());
        int i = studentService.updateNewStudent(student);
        if (i == 1) {
            if (student.getUsername().equals(SessionAuth.username(session))) {
                Student refreshed = studentService.getById(student.getUsername());
                refreshed.setPassword(null);
                session.setAttribute("User", refreshed);
            }
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    /**
     * 删除学生信息
     */
    @DeleteMapping("/delete/{username}")
    public Result<?> delete(@PathVariable String username, HttpSession session) {
        SessionAuth.requireRole(session, "admin");
        int i = studentService.deleteStudent(username);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 查找学生信息
     */
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search,
                              HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        Page page = SessionAuth.hasRole(session, "admin")
                ? studentService.find(pageNum, pageSize, search)
                : studentService.findByDormBuild(pageNum, pageSize, search, SessionAuth.dormBuildId(session));
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 学生登录
     */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody User user, HttpSession session) {
        Student student = studentService.stuLogin(user.getUsername(), user.getPassword());
        if (student != null) {
            log.info("学生登录成功: {}", user.getUsername());
            session.setAttribute("Identity", "stu");
            AuthenticatedUserResponse response = AuthenticatedUserResponse.from(student);
            student.setPassword(null);
            session.setAttribute("User", student);
            return Result.success(response);
        } else {
            return Result.error("-1", "用户名或密码错误");
        }
    }

    /**
     * 主页顶部：学生统计
     */
    @GetMapping("/stuNum")
    public Result<?> stuNum(HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        int num = studentService.stuNum();
        if (num > 0) {
            return Result.success(num);
        } else {
            return Result.error("-1", "查询失败");
        }
    }


    /**
     * 床位信息，查询是否存在该学生
     * 床位信息，查询床位上的学生信息
     */
    @GetMapping("/exist/{value}")
    public Result<?> exist(@PathVariable String value) {
        Student student = studentService.stuInfo(value);
        if (student != null) {
            java.util.Map<String, Object> summary = new java.util.LinkedHashMap<>();
            summary.put("username", student.getUsername());
            summary.put("name", student.getName());
            summary.put("avatar", student.getAvatar());
            return Result.success(summary);
        } else {
            return Result.error("-1", "不存在该学生");
        }
    }
}
