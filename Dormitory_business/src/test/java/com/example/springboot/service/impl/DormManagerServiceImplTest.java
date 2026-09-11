package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.DormManager;
import com.example.springboot.mapper.DormManagerMapper;
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
 * 宿管信息模块 — Service 层单元测试
 * 覆盖 addNewDormManager / find / updateNewDormManager / deleteDormManager / dormManagerLogin
 */
@DisplayName("宿管信息 Service 层测试")
class DormManagerServiceImplTest {

    /* ==================== 添加宿管 ==================== */

    @Nested
    @DisplayName("添加宿管 — addNewDormManager")
    class AddNewDormManager {

        @Test
        @DisplayName("添加成功：有效宿管信息应返回 1")
        void shouldReturnOneWhenInsertSucceeds() {
            DormManagerServiceImpl service = newService();
            DormManagerMapper mapper = mapper(service);

            DormManager manager = new DormManager("M001", "123456", 1, "刘管理员",
                    "男", 35, "13800009999", "liu@qq.com", null);
            when(mapper.insert(manager)).thenReturn(1);

            int result = service.addNewDormManager(manager);

            assertThat(result).isEqualTo(1);
            verify(mapper).insert(manager);
        }

        @Test
        @DisplayName("添加失败：数据库插入返回 0 时应返回 0")
        void shouldReturnZeroWhenInsertFails() {
            DormManagerServiceImpl service = newService();
            DormManagerMapper mapper = mapper(service);

            DormManager manager = new DormManager("M002", "123456", 2, "陈管理员",
                    "女", 28, "13800008888", "chen@qq.com", null);
            when(mapper.insert(manager)).thenReturn(0);

            int result = service.addNewDormManager(manager);

            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("添加：传入的宿管信息应完整持久化（含负责楼宇编号）")
        void shouldPersistAllManagerFields() {
            DormManagerServiceImpl service = newService();
            DormManagerMapper mapper = mapper(service);

            DormManager manager = new DormManager("M003", "pass789", 3, "赵管理员",
                    "女", 30, "13900006666", "zhao@qq.com", null);
            when(mapper.insert(manager)).thenReturn(1);

            service.addNewDormManager(manager);

            verify(mapper).insert(org.mockito.ArgumentMatchers.argThat(m ->
                    "M003".equals(m.getUsername())
                            && "赵管理员".equals(m.getName())
                            && m.getDormBuildId() == 3
                            && m.getAge() == 30
                            && "女".equals(m.getGender())));
        }
    }

    /* ==================== 分页查询宿管 ==================== */

    @Nested
    @DisplayName("分页查询宿管 — find")
    class Find {

        @Test
        @DisplayName("无搜索关键字：返回全部宿管分页数据")
        void shouldReturnAllManagersWhenSearchIsEmpty() {
            DormManagerServiceImpl service = newService();
            DormManagerMapper mapper = mapper(service);

            Page<DormManager> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(
                    new DormManager("M001", null, 1, "刘管理员", "男", 35, null, null, null),
                    new DormManager("M002", null, 2, "陈管理员", "女", 28, null, null, null)
            ));
            mockPage.setTotal(2);
            when(mapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

            Page result = service.find(1, 10, "");

            assertThat(result).isNotNull();
            assertThat(result.getRecords()).hasSize(2);
            assertThat(result.getTotal()).isEqualTo(2);
        }

        @Test
        @DisplayName("按姓名搜索：应返回姓名匹配的宿管")
        void shouldFilterByNameWhenSearchProvided() {
            DormManagerServiceImpl service = newService();
            DormManagerMapper mapper = mapper(service);

            Page<DormManager> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.singletonList(
                    new DormManager("M001", null, 1, "刘管理员", "男", 35, null, null, null)
            ));
            mockPage.setTotal(1);
            when(mapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

            Page result = service.find(1, 10, "刘");

            assertThat(result.getRecords()).hasSize(1);
            assertThat(((DormManager) result.getRecords().get(0)).getName()).isEqualTo("刘管理员");
        }

        @Test
        @DisplayName("搜索不存在的宿管：应返回空列表")
        void shouldReturnEmptyWhenNoMatch() {
            DormManagerServiceImpl service = newService();
            DormManagerMapper mapper = mapper(service);

            Page<DormManager> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.emptyList());
            mockPage.setTotal(0);
            when(mapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

            Page result = service.find(1, 10, "不存在");

            assertThat(result.getRecords()).isEmpty();
        }
    }

    /* ==================== 更新宿管 ==================== */

    @Nested
    @DisplayName("更新宿管 — updateNewDormManager")
    class UpdateNewDormManager {

        @Test
        @DisplayName("更新成功：存在的宿管应更新并返回 1")
        void shouldReturnOneWhenUpdateSucceeds() {
            DormManagerServiceImpl service = newService();
            DormManagerMapper mapper = mapper(service);

            DormManager manager = new DormManager("M001", null, 1, "刘管理员（已调岗）",
                    "男", 36, "13900009999", "liu_new@qq.com", null);
            when(mapper.updateById(manager)).thenReturn(1);

            int result = service.updateNewDormManager(manager);

            assertThat(result).isEqualTo(1);
            verify(mapper).updateById(manager);
        }

        @Test
        @DisplayName("更新失败：不存在的宿管账号应返回 0")
        void shouldReturnZeroWhenManagerNotFound() {
            DormManagerServiceImpl service = newService();
            DormManagerMapper mapper = mapper(service);

            DormManager manager = new DormManager("NOTEXIST", null, 0, "不存在", null, 0, null, null, null);
            when(mapper.updateById(manager)).thenReturn(0);

            int result = service.updateNewDormManager(manager);

            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("更新：仅修改负责楼宇时其他字段应保持不变")
        void shouldOnlyUpdateBuildingAssignment() {
            DormManagerServiceImpl service = newService();
            DormManagerMapper mapper = mapper(service);

            DormManager manager = new DormManager("M001", null, 4, "刘管理员", "男", 35, null, null, null);
            when(mapper.updateById(manager)).thenReturn(1);

            service.updateNewDormManager(manager);

            verify(mapper).updateById(org.mockito.ArgumentMatchers.argThat(m ->
                    "M001".equals(m.getUsername())
                            && m.getDormBuildId() == 4
                            && "刘管理员".equals(m.getName())));
        }
    }

    /* ==================== 删除宿管 ==================== */

    @Nested
    @DisplayName("删除宿管 — deleteDormManager")
    class DeleteDormManager {

        @Test
        @DisplayName("删除成功：存在的账号应删除并返回 1")
        void shouldReturnOneWhenDeleteSucceeds() {
            DormManagerServiceImpl service = newService();
            DormManagerMapper mapper = mapper(service);

            when(mapper.deleteById("M001")).thenReturn(1);

            int result = service.deleteDormManager("M001");

            assertThat(result).isEqualTo(1);
            verify(mapper).deleteById("M001");
        }

        @Test
        @DisplayName("删除失败：不存在的账号应返回 0")
        void shouldReturnZeroWhenUsernameNotFound() {
            DormManagerServiceImpl service = newService();
            DormManagerMapper mapper = mapper(service);

            when(mapper.deleteById("NOTEXIST")).thenReturn(0);

            int result = service.deleteDormManager("NOTEXIST");

            assertThat(result).isEqualTo(0);
        }
    }

    /* ==================== 宿管登录 ==================== */

    @Nested
    @DisplayName("宿管登录 — dormManagerLogin")
    class DormManagerLogin {

        @Test
        @DisplayName("登录成功：正确用户名和密码应返回 DormManager 对象")
        void shouldReturnManagerWhenCredentialsMatch() {
            DormManagerServiceImpl service = newService();
            DormManagerMapper mapper = mapper(service);

            DormManager mockManager = new DormManager("M001", null, 1, "刘管理员",
                    "男", 35, null, null, null);
            when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(mockManager);

            DormManager result = service.dormManagerLogin("M001", "123456");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("M001");
            assertThat(result.getName()).isEqualTo("刘管理员");
        }

        @Test
        @DisplayName("登录失败：错误密码应返回 null")
        void shouldReturnNullWhenPasswordWrong() {
            DormManagerServiceImpl service = newService();
            DormManagerMapper mapper = mapper(service);

            when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            DormManager result = service.dormManagerLogin("M001", "wrongpassword");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("登录失败：不存在的账号应返回 null")
        void shouldReturnNullWhenUserNotFound() {
            DormManagerServiceImpl service = newService();
            DormManagerMapper mapper = mapper(service);

            when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            DormManager result = service.dormManagerLogin("NOTEXIST", "123456");

            assertThat(result).isNull();
        }
    }

    /* ==================== 辅助方法 ==================== */

    private DormManagerServiceImpl newService() {
        DormManagerServiceImpl service = new DormManagerServiceImpl();
        ReflectionTestUtils.setField(service, "dormManagerMapper", mock(DormManagerMapper.class));
        return service;
    }

    @SuppressWarnings("unchecked")
    private DormManagerMapper mapper(Object service) {
        return (DormManagerMapper) ReflectionTestUtils.getField(service, "dormManagerMapper");
    }
}
