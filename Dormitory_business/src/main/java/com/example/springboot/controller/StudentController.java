package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Student;
import com.example.springboot.entity.User;
import com.example.springboot.service.StudentService;
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
    public Result<?> add(@RequestBody Student student) {
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
    public Result<?> update(@RequestBody Student student) {
        int i = studentService.updateNewStudent(student);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    /**
     * 删除学生信息
     */
    @DeleteMapping("/delete/{username}")
    public Result<?> delete(@PathVariable String username) {
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
                              @RequestParam(defaultValue = "") String search) {
        Page page = studentService.find(pageNum, pageSize, search);
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
        Object o = studentService.stuLogin(user.getUsername(), user.getPassword());
        if (o != null) {
            log.info("学生登录成功: {}", user.getUsername());
            session.setAttribute("Identity", "stu");
            session.setAttribute("User", o);
            return Result.success(o);
        } else {
            return Result.error("-1", "用户名或密码错误");
        }
    }

    /**
     * 主页顶部：学生统计
     */
    @GetMapping("/stuNum")
    public Result<?> stuNum() {
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
            return Result.success(student);
        } else {
            return Result.error("-1", "不存在该学生");
        }
    }
}
