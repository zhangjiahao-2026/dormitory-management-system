package com.example.springboot.service.impl;

import com.example.springboot.entity.AiRepairRequest;
import com.example.springboot.entity.DormManager;
import com.example.springboot.entity.DormRoom;
import com.example.springboot.entity.Repair;
import com.example.springboot.entity.Student;
import com.example.springboot.mapper.AiRepairRequestMapper;
import com.example.springboot.mapper.DormRoomMapper;
import com.example.springboot.mapper.RepairMapper;
import com.example.springboot.service.AiServiceClient;
import com.example.springboot.service.dto.AiRepairAnalyzeRequest;
import com.example.springboot.service.dto.AiRepairConfirmRequest;
import com.example.springboot.service.dto.AiRepairFeedbackRequest;
import com.example.springboot.service.dto.AiServiceAnalyzeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiRepairServiceImplTest {
    private AiRepairServiceImpl service;
    private AiRepairRequestMapper requestMapper;
    private DormRoomMapper roomMapper;
    private RepairMapper repairMapper;
    private AiServiceClient aiServiceClient;

    @BeforeEach
    void setUp() {
        service = new AiRepairServiceImpl();
        requestMapper = mock(AiRepairRequestMapper.class);
        roomMapper = mock(DormRoomMapper.class);
        repairMapper = mock(RepairMapper.class);
        aiServiceClient = mock(AiServiceClient.class);
        ReflectionTestUtils.setField(service, "aiRepairRequestMapper", requestMapper);
        ReflectionTestUtils.setField(service, "dormRoomMapper", roomMapper);
        ReflectionTestUtils.setField(service, "repairMapper", repairMapper);
        ReflectionTestUtils.setField(service, "aiServiceClient", aiServiceClient);
    }

    @Test
    void studentAnalysisUsesSessionIdentityAndRoom() {
        DormRoom room = new DormRoom();
        room.setDormBuildId(1);
        room.setDormRoomId(101);
        when(roomMapper.selectOne(any())).thenReturn(room);
        AiServiceAnalyzeResponse response = analysis("req_test_001");
        when(aiServiceClient.analyze(any())).thenReturn(response);

        AiRepairAnalyzeRequest request = new AiRepairAnalyzeRequest();
        request.setTitle("插座异常");
        request.setDescription("插座突然冒烟并有焦味");
        Map<String, Object> result = service.analyze(request, studentSession());

        AiRepairRequest pending = (AiRepairRequest) result.get("request");
        assertEquals("stu001", pending.getApplicantUsername());
        assertEquals(1, pending.getDormBuildId());
        assertEquals("PENDING_REVIEW", pending.getStatus());
        verify(requestMapper).insert(any(AiRepairRequest.class));
    }

    @Test
    void nonStudentCannotStartAnalysis() {
        assertThrows(SecurityException.class,
                () -> service.analyze(new AiRepairAnalyzeRequest(), managerSession(1)));
    }

    @Test
    void managerCannotConfirmAnotherBuildingRequest() {
        AiRepairRequest pending = pending("req_test_002", 2, "PENDING_REVIEW");
        when(requestMapper.selectById("req_test_002")).thenReturn(pending);
        assertThrows(SecurityException.class,
                () -> service.confirm("req_test_002", decision(), managerSession(1)));
    }

    @Test
    void repeatedConfirmationReturnsExistingRepair() {
        AiRepairRequest confirmed = pending("req_test_003", 1, "CONFIRMED");
        confirmed.setRepairId(88);
        when(requestMapper.selectById("req_test_003")).thenReturn(confirmed);
        Map<String, Object> result = service.confirm("req_test_003", decision(), managerSession(1));
        assertEquals(88, result.get("repairId"));
        assertEquals(true, result.get("duplicate"));
    }

    @Test
    void studentCannotSubmitFeedbackForAnotherApplicant() {
        AiRepairRequest pending = pending("req_feedback_001", 1, "PENDING_REVIEW");
        pending.setApplicantUsername("stu002");
        when(requestMapper.selectById("req_feedback_001")).thenReturn(pending);
        AiRepairFeedbackRequest feedback = new AiRepairFeedbackRequest();
        feedback.setRequestId("req_feedback_001");
        feedback.setRating("UP");
        assertThrows(SecurityException.class, () -> service.feedback(feedback, studentSession()));
    }

    @Test
    void downFeedbackRequiresKnownReason() {
        AiRepairFeedbackRequest feedback = new AiRepairFeedbackRequest();
        feedback.setRequestId("req_feedback_002");
        feedback.setRating("DOWN");
        assertThrows(IllegalArgumentException.class, () -> service.feedback(feedback, studentSession()));
    }

    private AiServiceAnalyzeResponse analysis(String requestId) {
        AiServiceAnalyzeResponse response = new AiServiceAnalyzeResponse();
        response.setRequestId(requestId);
        response.setCategory("ELECTRICAL");
        response.setUrgency("EMERGENCY");
        response.setConfidence(0.96);
        response.setSuggestedDepartment("WATER_ELECTRIC");
        response.setSuggestedDepartmentName("水电维修组");
        response.setRecommendedActions(Collections.singletonList("保持断电"));
        response.setReviewReasons(Collections.singletonList("涉及用电安全"));
        response.setStatus("NEED_REVIEW");
        return response;
    }

    private AiRepairRequest pending(String requestId, int buildId, String status) {
        AiRepairRequest request = new AiRepairRequest();
        request.setRequestId(requestId);
        request.setDormBuildId(buildId);
        request.setStatus(status);
        return request;
    }

    private AiRepairConfirmRequest decision() {
        AiRepairConfirmRequest request = new AiRepairConfirmRequest();
        request.setCategory("ELECTRICAL");
        request.setUrgency("EMERGENCY");
        request.setDepartment("WATER_ELECTRIC");
        return request;
    }

    private MockHttpSession studentSession() {
        Student student = new Student();
        student.setUsername("stu001");
        student.setName("李同学");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("Identity", "stu");
        session.setAttribute("User", student);
        return session;
    }

    private MockHttpSession managerSession(int buildId) {
        DormManager manager = new DormManager();
        manager.setUsername("manager1");
        manager.setDormBuildId(buildId);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("Identity", "dormManager");
        session.setAttribute("User", manager);
        return session;
    }
}
