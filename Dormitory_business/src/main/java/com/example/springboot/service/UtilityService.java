package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.UtilityConfig;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

public interface UtilityService {

    Page<Map<String, Object>> findLatest(Integer pageNum, Integer pageSize, String search, HttpSession session);

    List<?> history(Integer dormRoomId, HttpSession session);

    Map<String, Object> monthlyUsage(Integer dormRoomId, HttpSession session);

    UtilityConfig getConfig();

    int updateConfig(UtilityConfig config, HttpSession session);

    int simulate(String source, HttpSession session);

    void simulateAllRooms(String source);

    Page<Map<String, Object>> findAlerts(Integer pageNum, Integer pageSize, HttpSession session);

    Map<String, Object> homeAlerts(HttpSession session);

    int handleAlert(Integer id, HttpSession session);
}
