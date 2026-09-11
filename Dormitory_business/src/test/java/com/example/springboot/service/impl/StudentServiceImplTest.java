package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Student;
import com.example.springboot.mapper.StudentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 学生信息模块 — Service 层单元测试
 * 覆盖 addNewStudent / find / updateNewStudent / deleteStudent / stuLogin / stuNum / stuInfo
 */
@DisplayName("学生信息 Service 层测试")
class StudentServiceImplTest {

    /* ==================== 添加学生 ==================== */

    @Nested
    @DisplayName("添加学生 — addNewStudent")
    class AddNewStudent {

        @Test
        @DisplayName("添加成功：有效学生信息应返回 1")
        void shouldReturnOneWhenInsertSucceeds() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            Student student = new Student("S001", "123456", "张三", 20, "男",
                    "13800001111", "zhangsan@qq.com", null);
            when(mapper.insert(student)).thenReturn(1);

            int result = service.addNewStudent(student);

            assertThat(result).isEqualTo(1);
            verify(mapper).insert(student);
        }

        @Test
        @DisplayName("添加失败：数据库插入返回 0 时应返回 0")
        void shouldReturnZeroWhenInsertFails() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            Student student = new Student("S002", "123456", "李四", 21, "女",
                    "13800002222", "lisi@qq.com", null);
            when(mapper.insert(student)).thenReturn(0);

            int result = service.addNewStudent(student);

            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("添加：传入的学生信息应完整持久化")
        void shouldPersistAllStudentFields() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            Student student = new Student("S003", "pass123", "王五", 22, "男",
                    "13800003333", "wangwu@qq.com", "avatar.png");
            when(mapper.insert(student)).thenReturn(1);

            service.addNewStudent(student);

            verify(mapper).insert(org.mockito.ArgumentMatchers.argThat(s ->
                    "S003".equals(s.getUsername())
                            && "王五".equals(s.getName())
                            && s.getAge() == 22
                            && "男".equals(s.getGender())
                            && "13800003333".equals(s.getPhoneNum())
                            && "wangwu@qq.com".equals(s.getEmail())));
        }
    }

    /* ==================== 分页查询学生 ==================== */

    @Nested
    @DisplayName("分页查询学生 — find")
    class Find {

        @Test
        @DisplayName("无搜索关键字：返回全部学生分页数据")
        void shouldReturnAllStudentsWhenSearchIsEmpty() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            Page<Student> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(
                    new Student("S001", null, "张三", 20, "男", null, null, null),
                    new Student("S002", null, "李四", 21, "女", null, null, null)
            ));
            mockPage.setTotal(2);
            when(mapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

            Page result = service.find(1, 10, "");

            assertThat(result).isNotNull();
            assertThat(result.getRecords()).hasSize(2);
            assertThat(result.getTotal()).isEqualTo(2);
        }

        @Test
        @DisplayName("按姓名搜索：应返回姓名匹配的学生")
        void shouldFilterByNameWhenSearchProvided() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            Page<Student> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.singletonList(
                    new Student("S001", null, "张三", 20, "男", null, null, null)
            ));
            mockPage.setTotal(1);
            when(mapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

            Page result = service.find(1, 10, "张三");

            assertThat(result.getRecords()).hasSize(1);
            assertThat(((Student) result.getRecords().get(0)).getName()).isEqualTo("张三");
        }

        @Test
        @DisplayName("搜索不存在的学生：应返回空列表")
        void shouldReturnEmptyWhenNoMatch() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            Page<Student> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.emptyList());
            mockPage.setTotal(0);
            when(mapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

            Page result = service.find(1, 10, "不存在");

            assertThat(result.getRecords()).isEmpty();
            assertThat(result.getTotal()).isEqualTo(0);
        }
    }

    /* ==================== 更新学生 ==================== */

    @Nested
    @DisplayName("更新学生 — updateNewStudent")
    class UpdateNewStudent {

        @Test
        @DisplayName("更新成功：存在的学生应更新并返回 1")
        void shouldReturnOneWhenUpdateSucceeds() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            Student student = new Student("S001", null, "张三（已更新）", 21, "男",
                    "13900001111", "newemail@qq.com", null);
            when(mapper.updateById(student)).thenReturn(1);

            int result = service.updateNewStudent(student);

            assertThat(result).isEqualTo(1);
            verify(mapper).updateById(student);
        }

        @Test
        @DisplayName("更新失败：不存在的学号应返回 0")
        void shouldReturnZeroWhenStudentNotFound() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            Student student = new Student("NOTEXIST", null, "不存在", 0, null, null, null, null);
            when(mapper.updateById(student)).thenReturn(0);

            int result = service.updateNewStudent(student);

            assertThat(result).isEqualTo(0);
        }
    }

    /* ==================== 删除学生 ==================== */

    @Nested
    @DisplayName("删除学生 — deleteStudent")
    class DeleteStudent {

        @Test
        @DisplayName("删除成功：存在的学号应删除并返回 1")
        void shouldReturnOneWhenDeleteSucceeds() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            when(mapper.deleteById("S001")).thenReturn(1);

            int result = service.deleteStudent("S001");

            assertThat(result).isEqualTo(1);
            verify(mapper).deleteById("S001");
        }

        @Test
        @DisplayName("删除失败：不存在的学号应返回 0")
        void shouldReturnZeroWhenUsernameNotFound() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            when(mapper.deleteById("NOTEXIST")).thenReturn(0);

            int result = service.deleteStudent("NOTEXIST");

            assertThat(result).isEqualTo(0);
        }
    }

    /* ==================== 学生登录 ==================== */

    @Nested
    @DisplayName("学生登录 — stuLogin")
    class StuLogin {

        @Test
        @DisplayName("登录成功：正确用户名和密码应返回 Student 对象")
        void shouldReturnStudentWhenCredentialsMatch() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            Student mockStudent = new Student("S001", null, "张三", 20, "男", null, null, null);
            when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(mockStudent);

            Student result = service.stuLogin("S001", "123456");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("S001");
            assertThat(result.getName()).isEqualTo("张三");
        }

        @Test
        @DisplayName("登录失败：错误密码应返回 null")
        void shouldReturnNullWhenPasswordWrong() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            Student result = service.stuLogin("S001", "wrongpassword");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("登录失败：不存在的用户应返回 null")
        void shouldReturnNullWhenUserNotFound() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            Student result = service.stuLogin("NOTEXIST", "123456");

            assertThat(result).isNull();
        }
    }

    /* ==================== 学生统计 ==================== */

    @Nested
    @DisplayName("学生统计 — stuNum")
    class StuNum {

        @Test
        @DisplayName("有学生数据：应返��正确的学生总数")
        void shouldReturnCorrectStudentCount() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            when(mapper.selectCount(any(QueryWrapper.class))).thenReturn(200L);

            int count = service.stuNum();

            assertThat(count).isEqualTo(200);
        }

        @Test
        @DisplayName("无学生数据：应返回 0")
        void shouldReturnZeroWhenNoStudents() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            when(mapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

            int count = service.stuNum();

            assertThat(count).isEqualTo(0);
        }
    }

    /* ==================== 查询单个学生 ==================== */

    @Nested
    @DisplayName("查询单个学生 — stuInfo")
    class StuInfo {

        @Test
        @DisplayName("查询成功：存在的学号应返回完整学生信息")
        void shouldReturnStudentWhenFound() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            Student mockStudent = new Student("S001", "pass", "张三", 20, "男",
                    "13800001111", "zhangsan@qq.com", "avatar.png");
            when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(mockStudent);

            Student result = service.stuInfo("S001");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("S001");
            assertThat(result.getPhoneNum()).isEqualTo("13800001111");
            assertThat(result.getEmail()).isEqualTo("zhangsan@qq.com");
        }

        @Test
        @DisplayName("查询失败：不存在的学号应返回 null")
        void shouldReturnNullWhenNotFound() {
            StudentServiceImpl service = newService();
            StudentMapper mapper = mapper(service);

            when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            Student result = service.stuInfo("NOTEXIST");

            assertThat(result).isNull();
        }
    }

    /* ==================== 辅助方法 ==================== */

    private StudentServiceImpl newService() {
        StudentServiceImpl service = new StudentServiceImpl();
        ReflectionTestUtils.setField(service, "studentMapper", mock(StudentMapper.class));
        return service;
    }

    @SuppressWarnings("unchecked")
    private StudentMapper mapper(Object service) {
        return (StudentMapper) ReflectionTestUtils.getField(service, "studentMapper");
    }
}
