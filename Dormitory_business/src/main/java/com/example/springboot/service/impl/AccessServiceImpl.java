package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.DormManager;
import com.example.springboot.entity.DormRoom;
import com.example.springboot.entity.LargeItemRecord;
import com.example.springboot.entity.LateReturnAlert;
import com.example.springboot.entity.Student;
import com.example.springboot.entity.StudentReturnRecord;
import com.example.springboot.mapper.DormRoomMapper;
import com.example.springboot.mapper.LargeItemRecordMapper;
import com.example.springboot.mapper.LateReturnAlertMapper;
import com.example.springboot.mapper.StudentMapper;
import com.example.springboot.mapper.StudentReturnRecordMapper;
import com.example.springboot.service.AccessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccessServiceImpl implements AccessService {

    private static final LocalTime CURFEW_TIME = LocalTime.of(23, 0);
    private static final String ALERT_UNHANDLED = "UNHANDLED";
    private static final String ALERT_HANDLED = "HANDLED";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private StudentMapper studentMapper;
    @Resource
    private DormRoomMapper dormRoomMapper;
    @Resource
    private StudentReturnRecordMapper returnRecordMapper;
    @Resource
    private LateReturnAlertMapper lateReturnAlertMapper;
    @Resource
    private LargeItemRecordMapper largeItemRecordMapper;

    @Override
    public Page<StudentReturnRecord> findReturnRecords(Integer pageNum, Integer pageSize, String search, HttpSession session) {
        QueryWrapper<StudentReturnRecord> wrapper = returnScope(session);
        applyReturnSearch(wrapper, search);
        wrapper.orderByDesc("return_time");
        return returnRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public int addReturnRecord(StudentReturnRecord record, HttpSession session) {
        if (!prepareStudentDorm(record, session)) {
            return 0;
        }
        record.setLate(isLate(record.getReturnTime()) ? 1 : 0);
        record.setRegistrar(currentUsername(session));
        int inserted = returnRecordMapper.insert(record);
        if (inserted == 1 && record.getLate() != null && record.getLate() == 1) {
            createOrRefreshAlert(record);
        }
        return inserted;
    }

    @Override
    public int updateReturnRecord(Integer id, StudentReturnRecord record, HttpSession session) {
        StudentReturnRecord current = findVisibleReturnRecord(id, session);
        if (current == null) {
            return 0;
        }
        record.setId(id);
        if (!prepareStudentDorm(record, session)) {
            return 0;
        }
        record.setLate(isLate(record.getReturnTime()) ? 1 : 0);
        record.setRegistrar(current.getRegistrar());
        int updated = returnRecordMapper.updateById(record);
        if (updated == 1) {
            syncAlert(record);
        }
        return updated;
    }

    @Override
    public int deleteReturnRecord(Integer id, HttpSession session) {
        StudentReturnRecord current = findVisibleReturnRecord(id, session);
        if (current == null) {
            return 0;
        }
        deleteAllAlerts(id);
        return returnRecordMapper.deleteById(id);
    }

    @Override
    public Page<Map<String, Object>> findLateAlerts(Integer pageNum, Integer pageSize, HttpSession session) {
        QueryWrapper<LateReturnAlert> wrapper = alertScope(session);
        wrapper.eq("status", ALERT_UNHANDLED).orderByDesc("create_time");
        Page<LateReturnAlert> alertPage = lateReturnAlertMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        page.setTotal(alertPage.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        for (LateReturnAlert alert : alertPage.getRecords()) {
            records.add(alertRow(alert));
        }
        page.setRecords(records);
        return page;
    }

    @Override
    public Map<String, Object> homeAlerts(HttpSession session) {
        QueryWrapper<LateReturnAlert> countWrapper = alertScope(session);
        countWrapper.eq("status", ALERT_UNHANDLED);
        Long count = lateReturnAlertMapper.selectCount(countWrapper);

        QueryWrapper<LateReturnAlert> recentWrapper = alertScope(session);
        recentWrapper.eq("status", ALERT_UNHANDLED).orderByDesc("create_time").last("limit 5");
        List<LateReturnAlert> alerts = lateReturnAlertMapper.selectList(recentWrapper);
        List<Map<String, Object>> recent = new ArrayList<>();
        for (LateReturnAlert alert : alerts) {
            recent.add(alertRow(alert));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("unhandledCount", count);
        result.put("recentAlerts", recent);
        return result;
    }

    @Override
    public int handleLateAlert(Integer id, HttpSession session) {
        QueryWrapper<LateReturnAlert> wrapper = alertScope(session);
        wrapper.eq("id", id);
        LateReturnAlert alert = lateReturnAlertMapper.selectOne(wrapper);
        if (alert == null) {
            return 0;
        }
        alert.setStatus(ALERT_HANDLED);
        alert.setHandleTime(now());
        return lateReturnAlertMapper.updateById(alert);
    }

    @Override
    public Page<LargeItemRecord> findLargeItemRecords(Integer pageNum, Integer pageSize, String search, HttpSession session) {
        QueryWrapper<LargeItemRecord> wrapper = itemScope(session);
        applyItemSearch(wrapper, search);
        wrapper.orderByDesc("register_time");
        return largeItemRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public int addLargeItemRecord(LargeItemRecord record, HttpSession session) {
        if (!prepareStudentDorm(record, session)) {
            return 0;
        }
        if (record.getRegisterTime() == null || record.getRegisterTime().trim().isEmpty()) {
            record.setRegisterTime(now());
        }
        record.setRegistrar(currentUsername(session));
        return largeItemRecordMapper.insert(record);
    }

    @Override
    public int updateLargeItemRecord(Integer id, LargeItemRecord record, HttpSession session) {
        LargeItemRecord current = findVisibleItemRecord(id, session);
        if (current == null) {
            return 0;
        }
        record.setId(id);
        if (!prepareStudentDorm(record, session)) {
            return 0;
        }
        record.setRegistrar(current.getRegistrar());
        return largeItemRecordMapper.updateById(record);
    }

    @Override
    public int deleteLargeItemRecord(Integer id, HttpSession session) {
        LargeItemRecord current = findVisibleItemRecord(id, session);
        if (current == null) {
            return 0;
        }
        return largeItemRecordMapper.deleteById(id);
    }

    private boolean prepareStudentDorm(StudentReturnRecord record, HttpSession session) {
        Student student = studentMapper.selectById(record.getStudentUsername());
        DormRoom room = findStudentRoom(record.getStudentUsername());
        if (student == null || room == null || !canManageBuild(room.getDormBuildId(), session)) {
            return false;
        }
        record.setStudentName(student.getName());
        record.setDormBuildId(room.getDormBuildId());
        record.setDormRoomId(room.getDormRoomId());
        return true;
    }

    private boolean prepareStudentDorm(LargeItemRecord record, HttpSession session) {
        Student student = studentMapper.selectById(record.getStudentUsername());
        DormRoom room = findStudentRoom(record.getStudentUsername());
        if (student == null || room == null || !canManageBuild(room.getDormBuildId(), session)) {
            return false;
        }
        record.setStudentName(student.getName());
        record.setDormBuildId(room.getDormBuildId());
        record.setDormRoomId(room.getDormRoomId());
        return true;
    }

    private DormRoom findStudentRoom(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        QueryWrapper<DormRoom> wrapper = new QueryWrapper<>();
        wrapper.eq("first_bed", username)
                .or().eq("second_bed", username)
                .or().eq("third_bed", username)
                .or().eq("fourth_bed", username)
                .last("limit 1");
        return dormRoomMapper.selectOne(wrapper);
    }

    private StudentReturnRecord findVisibleReturnRecord(Integer id, HttpSession session) {
        QueryWrapper<StudentReturnRecord> wrapper = returnScope(session);
        wrapper.eq("id", id);
        return returnRecordMapper.selectOne(wrapper);
    }

    private LargeItemRecord findVisibleItemRecord(Integer id, HttpSession session) {
        QueryWrapper<LargeItemRecord> wrapper = itemScope(session);
        wrapper.eq("id", id);
        return largeItemRecordMapper.selectOne(wrapper);
    }

    private void syncAlert(StudentReturnRecord record) {
        if (record.getLate() != null && record.getLate() == 1) {
            createOrRefreshAlert(record);
        } else {
            deleteUnhandledAlert(record.getId());
        }
    }

    private void createOrRefreshAlert(StudentReturnRecord record) {
        QueryWrapper<LateReturnAlert> wrapper = new QueryWrapper<>();
        wrapper.eq("return_record_id", record.getId()).eq("status", ALERT_UNHANDLED);
        LateReturnAlert alert = lateReturnAlertMapper.selectOne(wrapper);
        if (alert == null) {
            alert = new LateReturnAlert();
            alert.setReturnRecordId(record.getId());
            alert.setStatus(ALERT_UNHANDLED);
            alert.setCreateTime(now());
        }
        alert.setStudentUsername(record.getStudentUsername());
        alert.setStudentName(record.getStudentName());
        alert.setDormBuildId(record.getDormBuildId());
        alert.setDormRoomId(record.getDormRoomId());
        alert.setReturnTime(record.getReturnTime());
        if (alert.getId() == null) {
            lateReturnAlertMapper.insert(alert);
        } else {
            lateReturnAlertMapper.updateById(alert);
        }
    }

    private void deleteUnhandledAlert(Integer returnRecordId) {
        QueryWrapper<LateReturnAlert> wrapper = new QueryWrapper<>();
        wrapper.eq("return_record_id", returnRecordId).eq("status", ALERT_UNHANDLED);
        lateReturnAlertMapper.delete(wrapper);
    }

    private void deleteAllAlerts(Integer returnRecordId) {
        QueryWrapper<LateReturnAlert> wrapper = new QueryWrapper<>();
        wrapper.eq("return_record_id", returnRecordId);
        lateReturnAlertMapper.delete(wrapper);
    }

    private QueryWrapper<StudentReturnRecord> returnScope(HttpSession session) {
        QueryWrapper<StudentReturnRecord> wrapper = new QueryWrapper<>();
        applyBuildScope(wrapper, session);
        return wrapper;
    }

    private QueryWrapper<LateReturnAlert> alertScope(HttpSession session) {
        QueryWrapper<LateReturnAlert> wrapper = new QueryWrapper<>();
        applyBuildScope(wrapper, session);
        return wrapper;
    }

    private QueryWrapper<LargeItemRecord> itemScope(HttpSession session) {
        QueryWrapper<LargeItemRecord> wrapper = new QueryWrapper<>();
        applyBuildScope(wrapper, session);
        return wrapper;
    }

    private void applyBuildScope(QueryWrapper<?> wrapper, HttpSession session) {
        Object identity = session.getAttribute("Identity");
        if ("stu".equals(identity)) {
            wrapper.eq("dormbuild_id", -1);
            return;
        }
        Integer dormBuildId = visibleDormBuildId(session);
        if (dormBuildId != null) {
            wrapper.eq("dormbuild_id", dormBuildId);
        }
    }

    private Integer visibleDormBuildId(HttpSession session) {
        Object identity = session.getAttribute("Identity");
        Object user = session.getAttribute("User");
        if ("dormManager".equals(identity) && user instanceof DormManager) {
            return ((DormManager) user).getDormBuildId();
        }
        return null;
    }

    private boolean canManageBuild(Integer dormBuildId, HttpSession session) {
        if ("stu".equals(session.getAttribute("Identity")) || dormBuildId == null) {
            return false;
        }
        Integer visibleBuildId = visibleDormBuildId(session);
        return visibleBuildId == null || visibleBuildId.equals(dormBuildId);
    }

    private void applyReturnSearch(QueryWrapper<StudentReturnRecord> wrapper, String search) {
        if (search == null || search.trim().isEmpty()) {
            return;
        }
        String keyword = search.trim();
        wrapper.and(qw -> qw.like("student_username", keyword)
                .or().like("student_name", keyword)
                .or().like("dormroom_id", keyword));
    }

    private void applyItemSearch(QueryWrapper<LargeItemRecord> wrapper, String search) {
        if (search == null || search.trim().isEmpty()) {
            return;
        }
        String keyword = search.trim();
        wrapper.and(qw -> qw.like("student_username", keyword)
                .or().like("student_name", keyword)
                .or().like("item_name", keyword)
                .or().like("dormroom_id", keyword));
    }

    private boolean isLate(String returnTime) {
        if (returnTime == null || returnTime.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(returnTime, FORMATTER);
            return dateTime.toLocalTime().isAfter(CURFEW_TIME);
        } catch (Exception ignored) {
            return false;
        }
    }

    private Map<String, Object> alertRow(LateReturnAlert alert) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", alert.getId());
        row.put("returnRecordId", alert.getReturnRecordId());
        row.put("studentUsername", alert.getStudentUsername());
        row.put("studentName", alert.getStudentName());
        row.put("dormBuildId", alert.getDormBuildId());
        row.put("dormRoomId", alert.getDormRoomId());
        row.put("returnTime", alert.getReturnTime());
        row.put("status", alert.getStatus());
        row.put("createTime", alert.getCreateTime());
        row.put("handleTime", alert.getHandleTime());
        return row;
    }

    private String currentUsername(HttpSession session) {
        Object user = session.getAttribute("User");
        if (user instanceof Student) {
            return ((Student) user).getUsername();
        }
        if (user instanceof DormManager) {
            return ((DormManager) user).getUsername();
        }
        try {
            Object username = user.getClass().getMethod("getUsername").invoke(user);
            return username == null ? "" : username.toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
