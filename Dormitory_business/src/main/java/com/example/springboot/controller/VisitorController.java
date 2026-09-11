package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.common.SessionAuth;
import com.example.springboot.entity.DormRoom;
import com.example.springboot.entity.Visitor;
import com.example.springboot.service.DormRoomService;
import com.example.springboot.service.VisitorService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Objects;

@RestController
@RequestMapping("/visitor")
public class VisitorController {

    @Resource
    private VisitorService visitorService;

    @Resource
    private DormRoomService dormRoomService;

    /**
     * 访客添加
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Visitor visitor, HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        applyVisitorScope(visitor, session, null);
        int i = visitorService.addNewVisitor(visitor);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "添加失败");
        }
    }

    /**
     * 访客信息更新
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Visitor visitor, HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        Visitor stored = visitorService.getById(visitor.getId());
        if (stored == null) return Result.error("-1", "访客记录不存在");
        SessionAuth.requireManagedBuild(session, stored.getDormBuildId());
        applyVisitorScope(visitor, session, stored);
        int i = visitorService.updateNewVisitor(visitor);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    /**
     * 访客删除
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id, HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        Visitor stored = visitorService.getById(id);
        if (stored == null) return Result.error("-1", "访客记录不存在");
        SessionAuth.requireManagedBuild(session, stored.getDormBuildId());
        int i = visitorService.deleteVisitor(id);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 访客查询
     */
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search,
                              HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        Page page = SessionAuth.hasRole(session, "admin")
                ? visitorService.find(pageNum, pageSize, search)
                : visitorService.findByDormBuild(pageNum, pageSize, search, SessionAuth.dormBuildId(session));
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    private void applyVisitorScope(Visitor visitor, HttpSession session, Visitor stored) {
        Integer roomId = visitor.getDormRoomId() != null
                ? visitor.getDormRoomId()
                : stored == null ? null : stored.getDormRoomId();
        if (roomId == null) throw new IllegalArgumentException("请选择被访宿舍");

        DormRoom room = dormRoomService.getById(roomId);
        if (room == null) throw new IllegalArgumentException("被访宿舍不存在");
        if (visitor.getDormBuildId() != null
                && !Objects.equals(visitor.getDormBuildId(), room.getDormBuildId())) {
            throw new IllegalArgumentException("楼栋号与房间不匹配");
        }
        SessionAuth.requireManagedBuild(session, room.getDormBuildId());
        visitor.setDormBuildId(room.getDormBuildId());
        visitor.setDormRoomId(roomId);
        visitor.setRegistrar(stored == null || stored.getRegistrar() == null
                ? SessionAuth.username(session)
                : stored.getRegistrar());
    }
}
