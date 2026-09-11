package com.example.springboot.controller;

import com.example.springboot.entity.DormManager;
import com.example.springboot.entity.DormRoom;
import com.example.springboot.entity.Visitor;
import com.example.springboot.service.DormRoomService;
import com.example.springboot.service.VisitorService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VisitorControllerSecurityTest {

    @Test
    void managerListIsFilteredToManagedBuilding() {
        VisitorController controller = controller();
        VisitorService service = visitorService(controller);

        controller.findPage(1, 10, "", managerSession("manager1", 1));

        verify(service).findByDormBuild(1, 10, "", 1);
        verify(service, never()).find(any(), any(), any());
    }

    @Test
    void managerCannotDeleteVisitorFromAnotherBuilding() {
        VisitorController controller = controller();
        VisitorService service = visitorService(controller);
        Visitor visitor = new Visitor();
        visitor.setId(9);
        visitor.setDormBuildId(2);
        when(service.getById(9)).thenReturn(visitor);

        assertThrows(SecurityException.class,
                () -> controller.delete(9, managerSession("manager1", 1)));
        verify(service, never()).deleteVisitor(9);
    }

    @Test
    void managerCreateUsesSessionBuildingAndRegistrar() {
        VisitorController controller = controller();
        VisitorService visitorService = visitorService(controller);
        DormRoomService roomService = roomService(controller);
        Visitor visitor = new Visitor();
        visitor.setDormRoomId(101);
        when(roomService.getById(101))
                .thenReturn(new DormRoom(101, 1, 1, 4, 0, null, null, null, null));
        when(visitorService.addNewVisitor(visitor)).thenReturn(1);

        controller.add(visitor, managerSession("manager1", 1));

        assertThat(visitor.getDormBuildId()).isEqualTo(1);
        assertThat(visitor.getRegistrar()).isEqualTo("manager1");
        verify(visitorService).addNewVisitor(visitor);
    }

    private VisitorController controller() {
        VisitorController controller = new VisitorController();
        ReflectionTestUtils.setField(controller, "visitorService", mock(VisitorService.class));
        ReflectionTestUtils.setField(controller, "dormRoomService", mock(DormRoomService.class));
        return controller;
    }

    private VisitorService visitorService(VisitorController controller) {
        return (VisitorService) ReflectionTestUtils.getField(controller, "visitorService");
    }

    private DormRoomService roomService(VisitorController controller) {
        return (DormRoomService) ReflectionTestUtils.getField(controller, "dormRoomService");
    }

    private MockHttpSession managerSession(String username, int buildId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("Identity", "dormManager");
        session.setAttribute("User", new DormManager(username, null, buildId, "宿管", "男", 40,
                null, null, null));
        return session;
    }
}
