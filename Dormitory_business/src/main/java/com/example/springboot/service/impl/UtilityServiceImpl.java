package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.DormManager;
import com.example.springboot.entity.DormRoom;
import com.example.springboot.entity.Student;
import com.example.springboot.entity.UtilityAlert;
import com.example.springboot.entity.UtilityConfig;
import com.example.springboot.entity.UtilityUsage;
import com.example.springboot.mapper.DormRoomMapper;
import com.example.springboot.mapper.UtilityAlertMapper;
import com.example.springboot.mapper.UtilityConfigMapper;
import com.example.springboot.mapper.UtilityUsageMapper;
import com.example.springboot.service.UtilityService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UtilityServiceImpl implements UtilityService {

    private static final int CONFIG_ID = 1;
    private static final String ALERT_UNHANDLED = "UNHANDLED";
    private static final String ALERT_HANDLED = "HANDLED";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private DormRoomMapper dormRoomMapper;
    @Resource
    private UtilityUsageMapper usageMapper;
    @Resource
    private UtilityAlertMapper alertMapper;
    @Resource
    private UtilityConfigMapper configMapper;

    @Override
    public Page<Map<String, Object>> findLatest(Integer pageNum, Integer pageSize, String search, HttpSession session) {
        List<DormRoom> rooms = visibleRooms(search, session);
        UtilityConfig config = getConfig();
        int fromIndex = Math.min((pageNum - 1) * pageSize, rooms.size());
        int toIndex = Math.min(fromIndex + pageSize, rooms.size());
        List<Map<String, Object>> records = new ArrayList<>();
        for (DormRoom room : rooms.subList(fromIndex, toIndex)) {
            UtilityUsage latest = latestUsage(room.getDormRoomId());
            records.add(toRow(room, latest, config));
        }
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        page.setTotal(rooms.size());
        page.setRecords(records);
        return page;
    }

    @Override
    public List<?> history(Integer dormRoomId, HttpSession session) {
        Integer dormBuildId = visibleDormBuildId(session);
        DormRoom room = dormRoomMapper.selectById(dormRoomId);
        if (room == null || (dormBuildId != null && !dormBuildId.equals(room.getDormBuildId()))) {
            return new ArrayList<>();
        }
        QueryWrapper<UtilityUsage> wrapper = new QueryWrapper<>();
        wrapper.eq("dormroom_id", dormRoomId).orderByDesc("collect_time");
        return usageMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> monthlyUsage(Integer dormRoomId, HttpSession session) {
        DormRoom room = dormRoomMapper.selectById(dormRoomId);
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dormRoomId", dormRoomId);
        result.put("month", month);
        result.put("electricUsageSum", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        result.put("waterUsageSum", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        if (!canViewRoom(room, session)) {
            return result;
        }

        QueryWrapper<UtilityUsage> wrapper = new QueryWrapper<>();
        wrapper.eq("dormroom_id", dormRoomId).likeRight("collect_time", month);
        List<UtilityUsage> usages = usageMapper.selectList(wrapper);
        BigDecimal electricUsageSum = BigDecimal.ZERO;
        BigDecimal waterUsageSum = BigDecimal.ZERO;
        for (UtilityUsage usage : usages) {
            if (usage.getElectricUsage() != null) {
                electricUsageSum = electricUsageSum.add(usage.getElectricUsage());
            }
            if (usage.getWaterUsage() != null) {
                waterUsageSum = waterUsageSum.add(usage.getWaterUsage());
            }
        }
        result.put("electricUsageSum", electricUsageSum.setScale(2, RoundingMode.HALF_UP));
        result.put("waterUsageSum", waterUsageSum.setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    @Override
    public UtilityConfig getConfig() {
        UtilityConfig config = configMapper.selectById(CONFIG_ID);
        if (config != null) {
            return config;
        }
        UtilityConfig defaultConfig = new UtilityConfig(
                CONFIG_ID,
                new BigDecimal("80.00"),
                new BigDecimal("20.00"),
                now()
        );
        configMapper.insert(defaultConfig);
        return defaultConfig;
    }

    @Override
    public int updateConfig(UtilityConfig config, HttpSession session) {
        if (!"admin".equals(session.getAttribute("Identity"))) {
            return 0;
        }
        UtilityConfig current = getConfig();
        current.setElectricLimit(config.getElectricLimit());
        current.setWaterLimit(config.getWaterLimit());
        current.setUpdateTime(now());
        return configMapper.updateById(current);
    }

    @Override
    public int simulate(String source, HttpSession session) {
        List<DormRoom> rooms = visibleRooms("", session);
        UtilityConfig config = getConfig();
        for (DormRoom room : rooms) {
            createUsage(room, config, source);
        }
        return rooms.size();
    }

    @Override
    public void simulateAllRooms(String source) {
        QueryWrapper<DormRoom> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("dormbuild_id").orderByAsc("dormroom_id");
        List<DormRoom> rooms = dormRoomMapper.selectList(wrapper);
        UtilityConfig config = getConfig();
        for (DormRoom room : rooms) {
            createUsage(room, config, source);
        }
    }

    @Override
    public Page<Map<String, Object>> findAlerts(Integer pageNum, Integer pageSize, HttpSession session) {
        QueryWrapper<UtilityAlert> wrapper = alertScope(session);
        wrapper.eq("status", ALERT_UNHANDLED).orderByDesc("create_time");
        Page<UtilityAlert> alertPage = alertMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        page.setTotal(alertPage.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        for (UtilityAlert alert : alertPage.getRecords()) {
            records.add(alertRow(alert));
        }
        page.setRecords(records);
        return page;
    }

    @Override
    public Map<String, Object> homeAlerts(HttpSession session) {
        QueryWrapper<UtilityAlert> countWrapper = alertScope(session);
        countWrapper.eq("status", ALERT_UNHANDLED);
        Long count = alertMapper.selectCount(countWrapper);

        QueryWrapper<UtilityAlert> recentWrapper = alertScope(session);
        recentWrapper.eq("status", ALERT_UNHANDLED).orderByDesc("create_time").last("limit 5");
        List<UtilityAlert> alerts = alertMapper.selectList(recentWrapper);
        List<Map<String, Object>> recent = new ArrayList<>();
        for (UtilityAlert alert : alerts) {
            recent.add(alertRow(alert));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("unhandledCount", count);
        result.put("recentAlerts", recent);
        return result;
    }

    @Override
    public int handleAlert(Integer id, HttpSession session) {
        QueryWrapper<UtilityAlert> wrapper = alertScope(session);
        wrapper.eq("id", id);
        UtilityAlert alert = alertMapper.selectOne(wrapper);
        if (alert == null) {
            return 0;
        }
        alert.setStatus(ALERT_HANDLED);
        alert.setHandleTime(now());
        return alertMapper.updateById(alert);
    }

    private void createUsage(DormRoom room, UtilityConfig config, String source) {
        BigDecimal electricUsage = randomUsage(config.getElectricLimit(), true);
        BigDecimal waterUsage = randomUsage(config.getWaterLimit(), true);
        UtilityUsage usage = new UtilityUsage(
                null,
                room.getDormRoomId(),
                room.getDormBuildId(),
                electricUsage,
                waterUsage,
                now(),
                source
        );
        usageMapper.insert(usage);
        insertAlertIfOverLimit(room, "ELECTRIC", electricUsage, config.getElectricLimit(), usage.getCollectTime());
        insertAlertIfOverLimit(room, "WATER", waterUsage, config.getWaterLimit(), usage.getCollectTime());
    }

    private void insertAlertIfOverLimit(DormRoom room, String alertType, BigDecimal usageValue, BigDecimal limitValue, String collectTime) {
        if (usageValue.compareTo(limitValue) <= 0) {
            return;
        }
        UtilityAlert alert = new UtilityAlert(
                null,
                room.getDormRoomId(),
                room.getDormBuildId(),
                alertType,
                usageValue,
                limitValue,
                ALERT_UNHANDLED,
                collectTime,
                null
        );
        alertMapper.insert(alert);
    }

    protected BigDecimal randomUsage(BigDecimal limit, boolean allowAlert) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double ratio = allowAlert && random.nextInt(10) < 2
                ? random.nextDouble(1.05, 1.45)
                : random.nextDouble(0.35, 0.92);
        return limit.multiply(BigDecimal.valueOf(ratio)).setScale(2, RoundingMode.HALF_UP);
    }

    private List<DormRoom> visibleRooms(String search, HttpSession session) {
        if ("stu".equals(session.getAttribute("Identity"))) {
            return new ArrayList<>();
        }
        QueryWrapper<DormRoom> wrapper = new QueryWrapper<>();
        Integer dormBuildId = visibleDormBuildId(session);
        if (dormBuildId != null) {
            wrapper.eq("dormbuild_id", dormBuildId);
        }
        if (search != null && !search.trim().isEmpty()) {
            wrapper.like("dormroom_id", search.trim());
        }
        wrapper.orderByAsc("dormbuild_id").orderByAsc("dormroom_id");
        return dormRoomMapper.selectList(wrapper);
    }

    private Integer visibleDormBuildId(HttpSession session) {
        Object identity = session.getAttribute("Identity");
        Object user = session.getAttribute("User");
        if ("dormManager".equals(identity) && user instanceof DormManager) {
            return ((DormManager) user).getDormBuildId();
        }
        return null;
    }

    private boolean canViewRoom(DormRoom room, HttpSession session) {
        if (room == null) {
            return false;
        }
        Object identity = session.getAttribute("Identity");
        Object user = session.getAttribute("User");
        if ("stu".equals(identity) && user instanceof Student) {
            String username = ((Student) user).getUsername();
            return username != null && (
                    username.equals(room.getFirstBed())
                            || username.equals(room.getSecondBed())
                            || username.equals(room.getThirdBed())
                            || username.equals(room.getFourthBed())
            );
        }
        Integer dormBuildId = visibleDormBuildId(session);
        return dormBuildId == null || dormBuildId.equals(room.getDormBuildId());
    }

    private QueryWrapper<UtilityAlert> alertScope(HttpSession session) {
        QueryWrapper<UtilityAlert> wrapper = new QueryWrapper<>();
        if ("stu".equals(session.getAttribute("Identity"))) {
            wrapper.eq("dormbuild_id", -1);
            return wrapper;
        }
        Integer dormBuildId = visibleDormBuildId(session);
        if (dormBuildId != null) {
            wrapper.eq("dormbuild_id", dormBuildId);
        }
        return wrapper;
    }

    private UtilityUsage latestUsage(Integer dormRoomId) {
        QueryWrapper<UtilityUsage> wrapper = new QueryWrapper<>();
        wrapper.eq("dormroom_id", dormRoomId).orderByDesc("collect_time").last("limit 1");
        return usageMapper.selectOne(wrapper);
    }

    private Map<String, Object> toRow(DormRoom room, UtilityUsage latest, UtilityConfig config) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("dormRoomId", room.getDormRoomId());
        row.put("dormBuildId", room.getDormBuildId());
        row.put("electricLimit", config.getElectricLimit());
        row.put("waterLimit", config.getWaterLimit());
        if (latest != null) {
            row.put("usageId", latest.getId());
            row.put("electricUsage", latest.getElectricUsage());
            row.put("waterUsage", latest.getWaterUsage());
            row.put("collectTime", latest.getCollectTime());
            row.put("collectSource", latest.getCollectSource());
            row.put("electricOverLimit", latest.getElectricUsage().compareTo(config.getElectricLimit()) > 0);
            row.put("waterOverLimit", latest.getWaterUsage().compareTo(config.getWaterLimit()) > 0);
        } else {
            row.put("usageId", null);
            row.put("electricUsage", null);
            row.put("waterUsage", null);
            row.put("collectTime", null);
            row.put("collectSource", null);
            row.put("electricOverLimit", false);
            row.put("waterOverLimit", false);
        }
        return row;
    }

    private Map<String, Object> alertRow(UtilityAlert alert) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", alert.getId());
        row.put("dormRoomId", alert.getDormRoomId());
        row.put("dormBuildId", alert.getDormBuildId());
        row.put("alertType", alert.getAlertType());
        row.put("triggerValue", alert.getTriggerValue());
        row.put("limitValue", alert.getLimitValue());
        row.put("status", alert.getStatus());
        row.put("createTime", alert.getCreateTime());
        row.put("handleTime", alert.getHandleTime());
        return row;
    }

    private String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
