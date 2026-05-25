package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.LargeItemRecord;
import com.example.springboot.entity.StudentReturnRecord;
import com.example.springboot.service.AccessService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/access")
public class AccessController {

    @Resource
    private AccessService accessService;

    @GetMapping("/returns")
    public Result<?> returns(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(defaultValue = "") String search,
                             HttpSession session) {
        return Result.success(accessService.findReturnRecords(pageNum, pageSize, search, session));
    }

    @PostMapping("/returns")
    public Result<?> addReturn(@RequestBody StudentReturnRecord record, HttpSession session) {
        int i = accessService.addReturnRecord(record, session);
        return i == 1 ? Result.success() : Result.error("-1", "返寝登记失败");
    }

    @PutMapping("/returns/{id}")
    public Result<?> updateReturn(@PathVariable Integer id, @RequestBody StudentReturnRecord record, HttpSession session) {
        int i = accessService.updateReturnRecord(id, record, session);
        return i == 1 ? Result.success() : Result.error("-1", "返寝记录更新失败");
    }

    @DeleteMapping("/returns/{id}")
    public Result<?> deleteReturn(@PathVariable Integer id, HttpSession session) {
        int i = accessService.deleteReturnRecord(id, session);
        return i == 1 ? Result.success() : Result.error("-1", "返寝记录删除失败");
    }

    @GetMapping("/lateAlerts")
    public Result<?> lateAlerts(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                HttpSession session) {
        return Result.success(accessService.findLateAlerts(pageNum, pageSize, session));
    }

    @GetMapping("/homeAlerts")
    public Result<?> homeAlerts(HttpSession session) {
        return Result.success(accessService.homeAlerts(session));
    }

    @PutMapping("/lateAlerts/{id}/handle")
    public Result<?> handleLateAlert(@PathVariable Integer id, HttpSession session) {
        int i = accessService.handleLateAlert(id, session);
        return i == 1 ? Result.success() : Result.error("-1", "晚归告警处理失败");
    }

    @GetMapping("/items")
    public Result<?> items(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(defaultValue = "") String search,
                           HttpSession session) {
        return Result.success(accessService.findLargeItemRecords(pageNum, pageSize, search, session));
    }

    @PostMapping("/items")
    public Result<?> addItem(@RequestBody LargeItemRecord record, HttpSession session) {
        int i = accessService.addLargeItemRecord(record, session);
        return i == 1 ? Result.success() : Result.error("-1", "大件物品登记失败");
    }

    @PutMapping("/items/{id}")
    public Result<?> updateItem(@PathVariable Integer id, @RequestBody LargeItemRecord record, HttpSession session) {
        int i = accessService.updateLargeItemRecord(id, record, session);
        return i == 1 ? Result.success() : Result.error("-1", "大件物品更新失败");
    }

    @DeleteMapping("/items/{id}")
    public Result<?> deleteItem(@PathVariable Integer id, HttpSession session) {
        int i = accessService.deleteLargeItemRecord(id, session);
        return i == 1 ? Result.success() : Result.error("-1", "大件物品删除失败");
    }
}
