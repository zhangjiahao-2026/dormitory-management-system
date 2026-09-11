package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.AiRepairRequest;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.DormManager;
import com.example.springboot.entity.DormRoom;
import com.example.springboot.entity.Repair;
import com.example.springboot.entity.Student;
import com.example.springboot.mapper.AiRepairRequestMapper;
import com.example.springboot.mapper.DormRoomMapper;
import com.example.springboot.mapper.RepairMapper;
import com.example.springboot.service.AiRepairService;
import com.example.springboot.service.AiServiceClient;
import com.example.springboot.service.dto.AiRepairAnalyzeRequest;
import com.example.springboot.service.dto.AiRepairConfirmRequest;
import com.example.springboot.service.dto.AiRepairFeedbackRequest;
import com.example.springboot.service.dto.AiRepairMetricsResponse;
import com.example.springboot.service.dto.AiServiceAnalyzeRequest;
import com.example.springboot.service.dto.AiServiceAnalyzeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiRepairServiceImpl implements AiRepairService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Set<String> CATEGORIES = Collections.unmodifiableSet(Arrays.stream(new String[]{
            "ELECTRICAL", "PLUMBING", "NETWORK", "DOOR_LOCK", "AIR_CONDITIONER",
            "FURNITURE", "PUBLIC_AREA", "OTHER"
    }).collect(Collectors.toSet()));
    private static final Set<String> URGENCIES = Collections.unmodifiableSet(Arrays.stream(new String[]{
            "EMERGENCY", "HIGH", "NORMAL", "LOW"
    }).collect(Collectors.toSet()));
    private static final Set<String> DEPARTMENTS = Collections.unmodifiableSet(Arrays.stream(new String[]{
            "WATER_ELECTRIC", "NETWORK_OPERATIONS", "FACILITY_MAINTENANCE",
            "CAMPUS_EMERGENCY", "GENERAL_SERVICES"
    }).collect(Collectors.toSet()));
    private static final Set<String> FEEDBACK_RATINGS = Collections.unmodifiableSet(Arrays.stream(new String[]{
            "UP", "DOWN"
    }).collect(Collectors.toSet()));
    private static final Set<String> FEEDBACK_REASONS = Collections.unmodifiableSet(Arrays.stream(new String[]{
            "CLASSIFICATION_ERROR", "URGENCY_ERROR", "RETRIEVAL_ERROR", "UNSUPPORTED_ANSWER",
            "UNCLEAR_EXPLANATION", "OTHER"
    }).collect(Collectors.toSet()));

    @Resource
    private AiRepairRequestMapper aiRepairRequestMapper;
    @Resource
    private DormRoomMapper dormRoomMapper;
    @Resource
    private RepairMapper repairMapper;
    @Resource
    private AiServiceClient aiServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> analyze(AiRepairAnalyzeRequest request, HttpSession session) {
        Student student = requireStudent(session);
        DormRoom room = findStudentRoom(student.getUsername());
        if (room == null) {
            throw new IllegalArgumentException("当前学生尚未分配宿舍");
        }
        AiServiceAnalyzeResponse analysis = aiServiceClient.analyze(new AiServiceAnalyzeRequest(
                room.getDormBuildId() + "号楼", String.valueOf(room.getDormRoomId()),
                request.getDescription().trim(), student.getUsername()));
        if (aiRepairRequestMapper.selectById(analysis.getRequestId()) != null) {
            throw new IllegalStateException("AI请求编号重复，请重试");
        }
        AiRepairRequest pending = toPending(request, student, room, analysis);
        aiRepairRequestMapper.insert(pending);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("request", pending);
        result.put("analysis", analysis);
        return result;
    }

    @Override
    public List<AiRepairRequest> mine(HttpSession session) {
        Student student = requireStudent(session);
        return aiRepairRequestMapper.selectList(new QueryWrapper<AiRepairRequest>()
                .eq("applicant_username", student.getUsername()).orderByDesc("created_at"));
    }

    @Override
    public Page<AiRepairRequest> pending(Integer pageNum, Integer pageSize, String search, HttpSession session) {
        requireManager(session);
        QueryWrapper<AiRepairRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PENDING_REVIEW");
        if (search != null && !search.trim().isEmpty()) {
            wrapper.and(item -> item.like("title", search.trim()).or().like("content", search.trim()));
        }
        Integer buildId = managerBuildId(session);
        if (buildId != null) {
            wrapper.eq("dormbuild_id", buildId);
        }
        wrapper.orderByDesc("created_at");
        return aiRepairRequestMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional
    public Map<String, Object> confirm(String requestId, AiRepairConfirmRequest request, HttpSession session) {
        String operator = requireManager(session);
        validateDecision(request);
        AiRepairRequest pending = requireVisibleRequest(requestId, session);
        if ("CONFIRMED".equals(pending.getStatus())) {
            return confirmationResult(pending.getRepairId(), true);
        }
        if (!"PENDING_REVIEW".equals(pending.getStatus())) {
            throw new IllegalStateException("该申请当前不可确认");
        }
        String now = now();
        UpdateWrapper<AiRepairRequest> claim = new UpdateWrapper<>();
        claim.eq("request_id", requestId).eq("status", "PENDING_REVIEW")
                .set("status", "CONFIRMED").set("category", request.getCategory())
                .set("urgency", request.getUrgency()).set("department", request.getDepartment())
                .set("reviewed_by", operator).set("reviewed_at", now)
                .set("operator_comment", trim(request.getOperatorComment()));
        if (aiRepairRequestMapper.update(null, claim) != 1) {
            AiRepairRequest current = aiRepairRequestMapper.selectById(requestId);
            if (current != null && "CONFIRMED".equals(current.getStatus())) {
                return confirmationResult(current.getRepairId(), true);
            }
            throw new IllegalStateException("申请状态已变化，请刷新后重试");
        }

        Repair repair = new Repair();
        repair.setRepairer(pending.getApplicantName());
        repair.setDormBuildId(pending.getDormBuildId());
        repair.setDormRoomId(pending.getDormRoomId());
        repair.setTitle(pending.getTitle());
        repair.setContent(pending.getContent());
        repair.setState("未完成");
        repair.setOrderBuildTime(now);
        repair.setAiRequestId(requestId);
        repair.setAiAssisted(1);
        repair.setAiCategory(request.getCategory());
        repair.setAiUrgency(request.getUrgency());
        repair.setAiDepartment(request.getDepartment());
        repair.setHumanConfirmedBy(operator);
        repair.setHumanConfirmedAt(now);
        repairMapper.insert(repair);

        aiRepairRequestMapper.update(null, new UpdateWrapper<AiRepairRequest>()
                .eq("request_id", requestId).set("repair_id", repair.getId()));
        return confirmationResult(repair.getId(), false);
    }

    @Override
    @Transactional
    public void reject(String requestId, String reason, HttpSession session) {
        String operator = requireManager(session);
        AiRepairRequest pending = requireVisibleRequest(requestId, session);
        if (!"PENDING_REVIEW".equals(pending.getStatus())) {
            throw new IllegalStateException("该申请当前不可拒绝");
        }
        int changed = aiRepairRequestMapper.update(null, new UpdateWrapper<AiRepairRequest>()
                .eq("request_id", requestId).eq("status", "PENDING_REVIEW")
                .set("status", "REJECTED").set("reviewed_by", operator)
                .set("reviewed_at", now()).set("operator_comment", reason.trim()));
        if (changed != 1) {
            throw new IllegalStateException("申请状态已变化，请刷新后重试");
        }
    }

    @Override
    public void feedback(AiRepairFeedbackRequest request, HttpSession session) {
        if (request.getRequestId() == null || request.getRequestId().trim().isEmpty()) {
            throw new IllegalArgumentException("反馈请求编号不能为空");
        }
        if (!FEEDBACK_RATINGS.contains(request.getRating())) {
            throw new IllegalArgumentException("反馈评分不在允许范围内");
        }
        if ("DOWN".equals(request.getRating())
                && (request.getReason() == null || !FEEDBACK_REASONS.contains(request.getReason()))) {
            throw new IllegalArgumentException("点踩时必须选择有效错误原因");
        }
        AiRepairRequest pending = aiRepairRequestMapper.selectById(request.getRequestId());
        if (pending == null) {
            throw new IllegalArgumentException("AI报修申请不存在");
        }
        requireFeedbackAccess(pending, session);
        aiServiceClient.feedback(request);
    }

    @Override
    public AiRepairMetricsResponse metrics(HttpSession session) {
        requireManager(session);
        return aiServiceClient.metrics();
    }

    private AiRepairRequest toPending(AiRepairAnalyzeRequest request, Student student, DormRoom room,
                                      AiServiceAnalyzeResponse analysis) {
        AiRepairRequest pending = new AiRepairRequest();
        pending.setRequestId(analysis.getRequestId());
        pending.setApplicantUsername(student.getUsername());
        pending.setApplicantName(student.getName());
        pending.setDormBuildId(room.getDormBuildId());
        pending.setDormRoomId(room.getDormRoomId());
        pending.setTitle(request.getTitle().trim());
        pending.setContent(request.getDescription().trim());
        pending.setCategory(analysis.getCategory());
        pending.setUrgency(analysis.getUrgency());
        pending.setConfidence(analysis.getConfidence());
        pending.setDepartment(analysis.getSuggestedDepartment());
        pending.setDepartmentName(analysis.getSuggestedDepartmentName());
        pending.setRecommendedActions(json(analysis.getRecommendedActions()));
        pending.setReviewReasons(json(analysis.getReviewReasons()));
        pending.setSources(json(analysis.getSources()));
        pending.setAiStatus(analysis.getStatus());
        pending.setStatus("PENDING_REVIEW");
        pending.setCreatedAt(now());
        return pending;
    }

    private DormRoom findStudentRoom(String username) {
        QueryWrapper<DormRoom> wrapper = new QueryWrapper<>();
        wrapper.and(item -> item.eq("first_bed", username).or().eq("second_bed", username)
                .or().eq("third_bed", username).or().eq("fourth_bed", username));
        return dormRoomMapper.selectOne(wrapper);
    }

    private AiRepairRequest requireVisibleRequest(String requestId, HttpSession session) {
        requireManager(session);
        AiRepairRequest request = aiRepairRequestMapper.selectById(requestId);
        if (request == null) {
            throw new IllegalArgumentException("AI报修申请不存在");
        }
        Integer buildId = managerBuildId(session);
        if (buildId != null && !buildId.equals(request.getDormBuildId())) {
            throw new SecurityException("无权审核其他楼栋的申请");
        }
        return request;
    }

    private void requireFeedbackAccess(AiRepairRequest request, HttpSession session) {
        Object identity = session.getAttribute("Identity");
        Object user = session.getAttribute("User");
        if ("stu".equals(identity) && user instanceof Student
                && ((Student) user).getUsername().equals(request.getApplicantUsername())) {
            return;
        }
        if ("admin".equals(identity) && user instanceof Admin) {
            return;
        }
        if ("dormManager".equals(identity) && user instanceof DormManager
                && Objects.equals(((DormManager) user).getDormBuildId(), request.getDormBuildId())) {
            return;
        }
        throw new SecurityException("无权提交该申请的反馈");
    }

    private Student requireStudent(HttpSession session) {
        if (!"stu".equals(session.getAttribute("Identity")) || !(session.getAttribute("User") instanceof Student)) {
            throw new SecurityException("仅学生可以发起AI报修分析");
        }
        return (Student) session.getAttribute("User");
    }

    private String requireManager(HttpSession session) {
        Object identity = session.getAttribute("Identity");
        Object user = session.getAttribute("User");
        if ("admin".equals(identity) && user instanceof Admin) {
            return ((Admin) user).getUsername();
        }
        if ("dormManager".equals(identity) && user instanceof DormManager) {
            return ((DormManager) user).getUsername();
        }
        throw new SecurityException("仅宿管或管理员可以审核AI报修申请");
    }

    private Integer managerBuildId(HttpSession session) {
        Object user = session.getAttribute("User");
        return user instanceof DormManager ? ((DormManager) user).getDormBuildId() : null;
    }

    private void validateDecision(AiRepairConfirmRequest request) {
        if (!CATEGORIES.contains(request.getCategory()) || !URGENCIES.contains(request.getUrgency())
                || !DEPARTMENTS.contains(request.getDepartment())) {
            throw new IllegalArgumentException("类别、紧急程度或处理部门不在允许范围内");
        }
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Collections.emptyList() : value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("AI分析结果序列化失败", e);
        }
    }

    private Map<String, Object> confirmationResult(Integer repairId, boolean duplicate) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("repairId", repairId);
        result.put("duplicate", duplicate);
        return result;
    }

    private static String now() {
        return LocalDateTime.now().format(FORMATTER);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
