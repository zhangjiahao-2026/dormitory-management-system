package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.common.PasswordUtil;
import com.example.springboot.entity.Student;
import com.example.springboot.mapper.StudentMapper;
import com.example.springboot.service.StudentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    /**
     * 注入DAO层对象
     */
    @Resource
    private StudentMapper studentMapper;

    /**
     * 学生登陆
     */
    @Override
    public Student stuLogin(String username, String password) {
        Student student = studentMapper.selectById(username);
        if (student == null || !PasswordUtil.matches(password, student.getPassword())) {
            return null;
        }
        if (PasswordUtil.needsUpgrade(student.getPassword())) {
            student.setPassword(PasswordUtil.encode(password));
            studentMapper.updateById(student);
        }
        return student;
    }

    /**
     * 学生新增
     */
    @Override
    public int addNewStudent(Student student) {
        student.setPassword(PasswordUtil.encode(student.getPassword()));
        int insert = studentMapper.insert(student);
        return insert;
    }

    /**
     * 分页查询学生
     */
    @Override
    public Page find(Integer pageNum, Integer pageSize, String search) {
        Page page = new Page<>(pageNum, pageSize);
        QueryWrapper<Student> qw = new QueryWrapper<>();
        qw.like("name", search);
        Page studentPage = studentMapper.selectPage(page, qw);
        return studentPage;
    }

    @Override
    public Page findByDormBuild(Integer pageNum, Integer pageSize, String search, Integer dormBuildId) {
        Page page = new Page<>(pageNum, pageSize);
        QueryWrapper<Student> qw = new QueryWrapper<>();
        String build = String.valueOf(dormBuildId);
        String residents = "SELECT first_bed FROM dorm_room WHERE dormbuild_id = " + build + " AND first_bed IS NOT NULL"
                + " UNION SELECT second_bed FROM dorm_room WHERE dormbuild_id = " + build + " AND second_bed IS NOT NULL"
                + " UNION SELECT third_bed FROM dorm_room WHERE dormbuild_id = " + build + " AND third_bed IS NOT NULL"
                + " UNION SELECT fourth_bed FROM dorm_room WHERE dormbuild_id = " + build + " AND fourth_bed IS NOT NULL";
        qw.like("name", search).inSql("username", residents);
        return studentMapper.selectPage(page, qw);
    }

    /**
     * 更新学生信息
     */
    @Override
    public int updateNewStudent(Student student) {
        Student stored = studentMapper.selectById(student.getUsername());
        if (stored == null) {
            return 0;
        }
        if (student.getPassword() == null || student.getPassword().isBlank()) {
            student.setPassword(stored.getPassword());
        } else if (!PasswordUtil.isBcrypt(student.getPassword())) {
            student.setPassword(PasswordUtil.encode(student.getPassword()));
        }
        int i = studentMapper.updateById(student);
        return i;
    }

    /**
     * 删除学生信息
     */
    @Override
    public int deleteStudent(String username) {
        int i = studentMapper.deleteById(username);
        return i;
    }


    /**
     * 主页顶部：学生统计
     */
    @Override
    public int stuNum() {
        QueryWrapper<Student> qw = new QueryWrapper<>();
        qw.isNotNull("username");
        int stuNum = Math.toIntExact(studentMapper.selectCount(qw));
        return stuNum;
    }

    /**
     * 床位信息，查询该学生信息
     */
    @Override
    public Student stuInfo(String username) {
        QueryWrapper<Student> qw = new QueryWrapper<>();
        qw.eq("username", username);
        Student student = studentMapper.selectOne(qw);
        return student;
    }
}
