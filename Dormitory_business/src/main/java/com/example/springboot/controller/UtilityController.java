package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.UtilityConfig;
import com.example.springboot.service.UtilityService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/utility")
public class UtilityController {

    @Resource
    private UtilityService utilityService;

    @GetMapping("/find")
    public Result<?> find(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(defaultValue = "") String search,
                          HttpSession session) {
        Page<Map<String, Object>> page = utilityService.findLatest(pageNum, pageSize, search, session);
        return Result.success(page);
    }

    @GetMapping("/history/{dormRoomId}")
    public Result<?> history(@PathVariable Integer dormRoomId, HttpSession session) {
        return Result.success(utilityService.history(dormRoomId, session));
    }

    @GetMapping("/monthly/{dormRoomId}")
    public Result<?> monthly(@PathVariable Integer dormRoomId, HttpSession session) {
        return Result.success(utilityService.monthlyUsage(dormRoomId, session));
    }

    @GetMapping("/config")
    public Result<?> config() {
        return Result.success(utilityService.getConfig());
    }

    @PutMapping("/config")
    public Result<?> updateConfig(@RequestBody UtilityConfig config, HttpSession session) {
        int i = utilityService.updateConfig(config, session);
        return i == 1 ? Result.success() : Result.error("-1", "无权限修改水电配置");
    }

    @PostMapping("/simulate")
    public Result<?> simulate(HttpSession session) {
        int count = utilityService.simulate("MANUAL", session);
        return Result.success(count);
    }

    @GetMapping("/alerts")
    public Result<?> alerts(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            HttpSession session) {
        return Result.success(utilityService.findAlerts(pageNum, pageSize, session));
    }

    @GetMapping("/homeAlerts")
    public Result<?> homeAlerts(HttpSession session) {
        return Result.success(utilityService.homeAlerts(session));
    }

    @PutMapping("/alerts/{id}/handle")
    public Result<?> handleAlert(@PathVariable Integer id, HttpSession session) {
        int i = utilityService.handleAlert(id, session);
        return i == 1 ? Result.success() : Result.error("-1", "告警处理失败");
    }
}
