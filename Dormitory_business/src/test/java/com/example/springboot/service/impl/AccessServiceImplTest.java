package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.entity.DormRoom;
import com.example.springboot.entity.LateReturnAlert;
import com.example.springboot.entity.Student;
import com.example.springboot.entity.StudentReturnRecord;
import com.example.springboot.mapper.DormRoomMapper;
import com.example.springboot.mapper.LargeItemRecordMapper;
import com.example.springboot.mapper.LateReturnAlertMapper;
import com.example.springboot.mapper.StudentMapper;
import com.example.springboot.mapper.StudentReturnRecordMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccessServiceImplTest {

    @Test
    void addReturnRecordCreatesLateAlertAfterCurfew() {
        AccessServiceImpl service = newService();
        StudentReturnRecordMapper returnMapper = mapper(service, "returnRecordMapper");
        LateReturnAlertMapper alertMapper = mapper(service, "lateReturnAlertMapper");
        mockStudentAndRoom(service);
        when(returnMapper.insert(any(StudentReturnRecord.class))).thenReturn(1);
        when(alertMapper.insert(any(LateReturnAlert.class))).thenReturn(1);

        StudentReturnRecord record = new StudentReturnRecord();
        record.setStudentUsername("stu001");
        record.setReturnTime("2026-05-25 23:01:00");

        int result = service.addReturnRecord(record, adminSession());

        assertThat(result).isEqualTo(1);
        ArgumentCaptor<StudentReturnRecord> recordCaptor = ArgumentCaptor.forClass(StudentReturnRecord.class);
        verify(returnMapper).insert(recordCaptor.capture());
        StudentReturnRecord savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.getStudentName()).isEqualTo("张三");
        assertThat(savedRecord.getDormBuildId()).isEqualTo(1);
        assertThat(savedRecord.getDormRoomId()).isEqualTo(101);
        assertThat(savedRecord.getLate()).isEqualTo(1);

        ArgumentCaptor<LateReturnAlert> alertCaptor = ArgumentCaptor.forClass(LateReturnAlert.class);
        verify(alertMapper).insert(alertCaptor.capture());
        assertThat(alertCaptor.getValue().getStatus()).isEqualTo("UNHANDLED");
        assertThat(alertCaptor.getValue().getStudentUsername()).isEqualTo("stu001");
    }

    @Test
    void addReturnRecordDoesNotCreateLateAlertBeforeCurfew() {
        AccessServiceImpl service = newService();
        StudentReturnRecordMapper returnMapper = mapper(service, "returnRecordMapper");
        LateReturnAlertMapper alertMapper = mapper(service, "lateReturnAlertMapper");
        mockStudentAndRoom(service);
        when(returnMapper.insert(any(StudentReturnRecord.class))).thenReturn(1);

        StudentReturnRecord record = new StudentReturnRecord();
        record.setStudentUsername("stu001");
        record.setReturnTime("2026-05-25 22:59:00");

        int result = service.addReturnRecord(record, adminSession());

        assertThat(result).isEqualTo(1);
        ArgumentCaptor<StudentReturnRecord> recordCaptor = ArgumentCaptor.forClass(StudentReturnRecord.class);
        verify(returnMapper).insert(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getLate()).isEqualTo(0);
        verify(alertMapper, never()).insert(any(LateReturnAlert.class));
    }

    @Test
    void deleteReturnRecordDeletesAllRelatedAlerts() {
        AccessServiceImpl service = newService();
        StudentReturnRecordMapper returnMapper = mapper(service, "returnRecordMapper");
        LateReturnAlertMapper alertMapper = mapper(service, "lateReturnAlertMapper");

        StudentReturnRecord current = new StudentReturnRecord();
        current.setId(12);
        current.setDormBuildId(1);
        when(returnMapper.selectOne(any(Wrapper.class))).thenReturn(current);
        when(returnMapper.deleteById(12)).thenReturn(1);
        when(alertMapper.delete(any(Wrapper.class))).thenReturn(2);

        int result = service.deleteReturnRecord(12, adminSession());

        assertThat(result).isEqualTo(1);
        ArgumentCaptor<QueryWrapper<LateReturnAlert>> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(alertMapper).delete(wrapperCaptor.capture());
        assertThat(wrapperCaptor.getValue().getSqlSegment()).contains("return_record_id");
        assertThat(wrapperCaptor.getValue().getSqlSegment()).doesNotContain("status");
    }

    @SuppressWarnings("unchecked")
    private <T> T mapper(AccessServiceImpl service, String fieldName) {
        return (T) ReflectionTestUtils.getField(service, fieldName);
    }

    private AccessServiceImpl newService() {
        AccessServiceImpl service = new AccessServiceImpl();
        ReflectionTestUtils.setField(service, "studentMapper", mock(StudentMapper.class));
        ReflectionTestUtils.setField(service, "dormRoomMapper", mock(DormRoomMapper.class));
        ReflectionTestUtils.setField(service, "returnRecordMapper", mock(StudentReturnRecordMapper.class));
        ReflectionTestUtils.setField(service, "lateReturnAlertMapper", mock(LateReturnAlertMapper.class));
        ReflectionTestUtils.setField(service, "largeItemRecordMapper", mock(LargeItemRecordMapper.class));
        return service;
    }

    private void mockStudentAndRoom(AccessServiceImpl service) {
        StudentMapper studentMapper = mapper(service, "studentMapper");
        DormRoomMapper dormRoomMapper = mapper(service, "dormRoomMapper");

        Student student = new Student();
        student.setUsername("stu001");
        student.setName("张三");
        when(studentMapper.selectById("stu001")).thenReturn(student);

        DormRoom room = new DormRoom();
        room.setDormBuildId(1);
        room.setDormRoomId(101);
        room.setFirstBed("stu001");
        when(dormRoomMapper.selectOne(any(Wrapper.class))).thenReturn(room);
    }

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("Identity", "admin");
        session.setAttribute("User", new Object());
        return session;
    }
}
