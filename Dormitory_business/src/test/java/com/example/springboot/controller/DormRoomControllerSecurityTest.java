package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.DormManager;
import com.example.springboot.entity.DormRoom;
import com.example.springboot.entity.Student;
import com.example.springboot.service.DormRoomService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DormRoomControllerSecurityTest {

    @Test
    void studentRoomResponseDoesNotExposeBedUsernames() {
        DormRoomController controller = controller();
        DormRoomService service = service(controller);
        DormRoom room = new DormRoom(101, 1, 1, 4, 2,
                "stu001", "stu002", null, null);
        when(service.judgeHadBed("stu001")).thenReturn(room);

        Result<?> result = controller.getMyRoom("stu001", studentSession("stu001"));

        assertThat(result.getCode()).isEqualTo("0");
        assertThat(result.getData()).isInstanceOf(Map.class);
        assertThat(result.getData().toString()).doesNotContain("stu001", "stu002");
        assertThat(((Map<?, ?>) result.getData()).get("ownBedNumber")).isEqualTo(1);
    }

    @Test
    void managerCannotReadStudentRoomInAnotherBuilding() {
        DormRoomController controller = controller();
        DormRoomService service = service(controller);
        when(service.judgeHadBed("stu002"))
                .thenReturn(new DormRoom(10101, 2, 1, 4, 1, "stu002", null, null, null));

        assertThrows(SecurityException.class,
                () -> controller.getMyRoom("stu002", managerSession("manager1", 1)));
    }

    @Test
    void studentCannotCallPrivilegedRoomInspectionEndpoint() {
        DormRoomController controller = controller();
        assertThrows(SecurityException.class,
                () -> controller.checkRoomExist(101, studentSession("stu001")));
    }

    private DormRoomController controller() {
        DormRoomController controller = new DormRoomController();
        ReflectionTestUtils.setField(controller, "dormRoomService", mock(DormRoomService.class));
        return controller;
    }

    private DormRoomService service(DormRoomController controller) {
        return (DormRoomService) ReflectionTestUtils.getField(controller, "dormRoomService");
    }

    private MockHttpSession studentSession(String username) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("Identity", "stu");
        session.setAttribute("User", new Student(username, null, "学生", 20, "男", null, null, null));
        return session;
    }

    private MockHttpSession managerSession(String username, int buildId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("Identity", "dormManager");
        session.setAttribute("User", new DormManager(username, null, buildId, "宿管", "男", 40,
                null, null, null));
        return session;
    }
}
