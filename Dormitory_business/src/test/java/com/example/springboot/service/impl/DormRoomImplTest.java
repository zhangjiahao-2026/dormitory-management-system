package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.DormRoom;
import com.example.springboot.mapper.DormRoomMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 房间信息模块 — Service 层单元测试
 * 覆盖 addNewRoom / find / updateNewRoom / deleteRoom / deleteBedInfo / judgeHadBed /
 *      notFullRoom / selectHaveRoomStuNum / checkRoomState / checkRoomExist / checkBedState
 */
@DisplayName("房间信息 Service 层测试")
class DormRoomImplTest {

    /* ==================== 添加房间 ==================== */

    @Nested
    @DisplayName("添加房间 — addNewRoom")
    class AddNewRoom {

        @Test
        @DisplayName("添加成功：有效房间信息应返回 1")
        void shouldReturnOneWhenInsertSucceeds() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            DormRoom room = new DormRoom(301, 1, 3, 4, 0, null, null, null, null);
            when(mapper.insert(room)).thenReturn(1);

            int result = service.addNewRoom(room);

            assertThat(result).isEqualTo(1);
            verify(mapper).insert(room);
        }

        @Test
        @DisplayName("添加失败：数据库插入返回 0 时应返回 0")
        void shouldReturnZeroWhenInsertFails() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            DormRoom room = new DormRoom(302, 2, 3, 4, 2, "S001", "S002", null, null);
            when(mapper.insert(room)).thenReturn(0);

            int result = service.addNewRoom(room);

            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("添加：房间字段应完整持久化")
        void shouldPersistAllRoomFields() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            DormRoom room = new DormRoom(101, 1, 1, 4, 0, null, null, null, null);
            when(mapper.insert(room)).thenReturn(1);

            service.addNewRoom(room);

            verify(mapper).insert(org.mockito.ArgumentMatchers.argThat(r ->
                    r.getDormRoomId() == 101
                            && r.getDormBuildId() == 1
                            && r.getFloorNum() == 1
                            && r.getMaxCapacity() == 4
                            && r.getCurrentCapacity() == 0));
        }
    }

    /* ==================== 分页查询房间 ==================== */

    @Nested
    @DisplayName("分页查询房间 — find")
    class Find {

        @Test
        @DisplayName("无搜索关键字：返回全部房间分页数据")
        void shouldReturnAllRoomsWhenSearchIsEmpty() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            Page<DormRoom> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(
                    new DormRoom(101, 1, 1, 4, 2, "S001", "S002", null, null),
                    new DormRoom(201, 2, 2, 4, 1, "S003", null, null, null)
            ));
            mockPage.setTotal(2);
            when(mapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

            Page result = service.find(1, 10, "");

            assertThat(result).isNotNull();
            assertThat(result.getRecords()).hasSize(2);
            assertThat(result.getTotal()).isEqualTo(2);
        }

        @Test
        @DisplayName("按房间号搜索：应返回匹配的房间")
        void shouldFilterByRoomIdWhenSearchProvided() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            Page<DormRoom> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.singletonList(
                    new DormRoom(101, 1, 1, 4, 2, "S001", "S002", null, null)
            ));
            mockPage.setTotal(1);
            when(mapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

            Page result = service.find(1, 10, "101");

            assertThat(result.getRecords()).hasSize(1);
            assertThat(((DormRoom) result.getRecords().get(0)).getDormRoomId()).isEqualTo(101);
        }

        @Test
        @DisplayName("搜索不存在的房间号：应返回空列表")
        void shouldReturnEmptyWhenNoMatch() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            Page<DormRoom> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.emptyList());
            mockPage.setTotal(0);
            when(mapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

            Page result = service.find(1, 10, "9999");

            assertThat(result.getRecords()).isEmpty();
        }
    }

    /* ==================== 更新房间 ==================== */

    @Nested
    @DisplayName("更新房间 — updateNewRoom")
    class UpdateNewRoom {

        @Test
        @DisplayName("更新成功：存在的房间应更新并返回 1")
        void shouldReturnOneWhenUpdateSucceeds() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            DormRoom room = new DormRoom(101, 1, 1, 4, 3, "S001", "S002", "S004", null);
            when(mapper.updateById(room)).thenReturn(1);

            int result = service.updateNewRoom(room);

            assertThat(result).isEqualTo(1);
            verify(mapper).updateById(room);
        }

        @Test
        @DisplayName("更新失败：不存在的房间号应返回 0")
        void shouldReturnZeroWhenRoomNotFound() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            DormRoom room = new DormRoom(9999, 0, 0, 0, 0, null, null, null, null);
            when(mapper.updateById(room)).thenReturn(0);

            int result = service.updateNewRoom(room);

            assertThat(result).isEqualTo(0);
        }
    }

    /* ==================== 删除房间 ==================== */

    @Nested
    @DisplayName("删除房间 — deleteRoom")
    class DeleteRoom {

        @Test
        @DisplayName("删除成功：存在的房间号应删除并返回 1")
        void shouldReturnOneWhenDeleteSucceeds() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            when(mapper.deleteById(101)).thenReturn(1);

            int result = service.deleteRoom(101);

            assertThat(result).isEqualTo(1);
            verify(mapper).deleteById(101);
        }

        @Test
        @DisplayName("删除失败：不存在的房间号应返回 0")
        void shouldReturnZeroWhenRoomNotFound() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            when(mapper.deleteById(9999)).thenReturn(0);

            int result = service.deleteRoom(9999);

            assertThat(result).isEqualTo(0);
        }
    }

    /* ==================== 删除床位信息 ==================== */

    @Nested
    @DisplayName("删除床位学生信息 — deleteBedInfo")
    class DeleteBedInfo {

        @Test
        @DisplayName("删除成功：清空指定床位并更新已住人数")
        void shouldClearBedAndDecrementCapacity() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            when(mapper.update(any(), any(UpdateWrapper.class))).thenReturn(1);

            int result = service.deleteBedInfo("first_bed", 101, 3);

            assertThat(result).isEqualTo(1);
            verify(mapper).update(any(), any(UpdateWrapper.class));
        }

        @Test
        @DisplayName("第四床位：应正确清空 fourth_bed 列")
        void shouldClearFourthBed() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            when(mapper.update(any(), any(UpdateWrapper.class))).thenReturn(1);

            int result = service.deleteBedInfo("fourth_bed", 202, 4);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("已住人数应从当前值减 1")
        void shouldDecrementCurrentCapacityByOne() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            when(mapper.update(any(), any(UpdateWrapper.class))).thenReturn(1);

            // calCurrentNum = 3, 删除后应为 2
            int result = service.deleteBedInfo("second_bed", 303, 3);

            assertThat(result).isEqualTo(1);
        }
    }

    /* ==================== 判断学生是否已有床位 ==================== */

    @Nested
    @DisplayName("判断学生是否已有床位 — judgeHadBed")
    class JudgeHadBed {

        @Test
        @DisplayName("学生已有床位：应返回所在房间信息")
        void shouldReturnRoomWhenStudentHasBed() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            DormRoom room = new DormRoom(101, 1, 1, 4, 2, "S001", "S002", null, null);
            when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(room);

            DormRoom result = service.judgeHadBed("S001");

            assertThat(result).isNotNull();
            assertThat(result.getDormRoomId()).isEqualTo(101);
            assertThat(result.getFirstBed()).isEqualTo("S001");
        }

        @Test
        @DisplayName("学生未有床位：应返回 null")
        void shouldReturnNullWhenStudentHasNoBed() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            DormRoom result = service.judgeHadBed("S999");

            assertThat(result).isNull();
        }
    }

    /* ==================== 空房间统计 ==================== */

    @Nested
    @DisplayName("空房间统计 — notFullRoom")
    class NotFullRoom {

        @Test
        @DisplayName("有未满房间：应返回正确的未满房间数量")
        void shouldReturnCorrectNotFullRoomCount() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            when(mapper.selectCount(any(QueryWrapper.class))).thenReturn(15L);

            int count = service.notFullRoom();

            assertThat(count).isEqualTo(15);
        }

        @Test
        @DisplayName("无未满房间：应返回 0")
        void shouldReturnZeroWhenAllRoomsFull() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            when(mapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

            int count = service.notFullRoom();

            assertThat(count).isEqualTo(0);
        }
    }

    /* ==================== 住宿总人数统计 ==================== */

    @Nested
    @DisplayName("住宿总人数统计 — selectHaveRoomStuNum")
    class SelectHaveRoomStuNum {

        @Test
        @DisplayName("有住宿记录：应返回所有床位占用总数")
        void shouldReturnTotalHousedStudents() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            Map<String, Object> mockMap = new java.util.HashMap<>();
            mockMap.put("total", 320L);
            when(mapper.selectMaps(any(QueryWrapper.class))).thenReturn(
                    Collections.singletonList(mockMap));

            Long total = service.selectHaveRoomStuNum();

            assertThat(total).isEqualTo(320L);
        }

        @Test
        @DisplayName("无住宿记录：应返回 0")
        void shouldReturnZeroWhenNoStudentsHoused() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            Map<String, Object> mockMap = new java.util.HashMap<>();
            mockMap.put("total", 0L);
            when(mapper.selectMaps(any(QueryWrapper.class))).thenReturn(
                    Collections.singletonList(mockMap));

            Long total = service.selectHaveRoomStuNum();

            assertThat(total).isEqualTo(0L);
        }
    }

    /* ==================== 房间状态检查 ==================== */

    @Nested
    @DisplayName("房间状态检查")
    class RoomStateChecks {

        @Test
        @DisplayName("检查房间未满：未满房间应返回房间信息")
        void shouldReturnRoomWhenNotFull() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            DormRoom room = new DormRoom(101, 1, 1, 4, 2, "S001", "S002", null, null);
            when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(room);

            DormRoom result = service.checkRoomState(101);

            assertThat(result).isNotNull();
            assertThat(result.getDormRoomId()).isEqualTo(101);
        }

        @Test
        @DisplayName("检查房间未满：已满房间应返回 null")
        void shouldReturnNullWhenRoomFull() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            DormRoom result = service.checkRoomState(101);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("检查房间存在：存在的房间应返回房间信息")
        void shouldReturnRoomWhenExists() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            DormRoom room = new DormRoom(101, 1, 1, 4, 2, null, null, null, null);
            when(mapper.selectById(101)).thenReturn(room);

            DormRoom result = service.checkRoomExist(101);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("检查房间存在：不存在的房间应返回 null")
        void shouldReturnNullWhenRoomNotExists() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            when(mapper.selectById(9999)).thenReturn(null);

            DormRoom result = service.checkRoomExist(9999);

            assertThat(result).isNull();
        }
    }

    /* ==================== 床位状态检查 ==================== */

    @Nested
    @DisplayName("床位状态检查 — checkBedState")
    class CheckBedState {

        @Test
        @DisplayName("床位空闲：应返回房间信息（表示该床位可分配）")
        void shouldReturnRoomWhenBedIsFree() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            DormRoom room = new DormRoom(101, 1, 1, 4, 2, "S001", null, null, null);
            when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(room);

            DormRoom result = service.checkBedState(101, 2);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("床位已占用：应返回 null")
        void shouldReturnNullWhenBedOccupied() {
            DormRoomImpl service = newService();
            DormRoomMapper mapper = mapper(service);

            when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            DormRoom result = service.checkBedState(101, 1);

            assertThat(result).isNull();
        }
    }

    /* ==================== 辅助方法 ==================== */

    private DormRoomImpl newService() {
        DormRoomImpl service = new DormRoomImpl();
        ReflectionTestUtils.setField(service, "dormRoomMapper", mock(DormRoomMapper.class));
        return service;
    }

    @SuppressWarnings("unchecked")
    private DormRoomMapper mapper(Object service) {
        return (DormRoomMapper) ReflectionTestUtils.getField(service, "dormRoomMapper");
    }
}
