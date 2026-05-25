package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.LargeItemRecord;
import com.example.springboot.entity.StudentReturnRecord;

import javax.servlet.http.HttpSession;
import java.util.Map;

public interface AccessService {

    Page<StudentReturnRecord> findReturnRecords(Integer pageNum, Integer pageSize, String search, HttpSession session);

    int addReturnRecord(StudentReturnRecord record, HttpSession session);

    int updateReturnRecord(Integer id, StudentReturnRecord record, HttpSession session);

    int deleteReturnRecord(Integer id, HttpSession session);

    Page<Map<String, Object>> findLateAlerts(Integer pageNum, Integer pageSize, HttpSession session);

    Map<String, Object> homeAlerts(HttpSession session);

    int handleLateAlert(Integer id, HttpSession session);

    Page<LargeItemRecord> findLargeItemRecords(Integer pageNum, Integer pageSize, String search, HttpSession session);

    int addLargeItemRecord(LargeItemRecord record, HttpSession session);

    int updateLargeItemRecord(Integer id, LargeItemRecord record, HttpSession session);

    int deleteLargeItemRecord(Integer id, HttpSession session);
}
