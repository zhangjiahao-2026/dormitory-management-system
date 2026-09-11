package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.common.SessionAuth;
import com.example.springboot.entity.Repair;
import com.example.springboot.entity.DormRoom;
import com.example.springboot.service.DormRoomService;
import com.example.springboot.service.RepairService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/repair")
public class RepairController {

    @Resource
    private RepairService repairService;

    @Resource
    private DormRoomService dormRoomService;

    /**
     * 添加订单
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Repair repair, HttpSession session) {
        SessionAuth.requireRole(session, "stu");
        repair.setRepairer(SessionAuth.username(session));
        DormRoom room = dormRoomService.judgeHadBed(SessionAuth.username(session));
        if (room == null) return Result.error("-1", "当前学生尚未分配宿舍");
        repair.setDormBuildId(room.getDormBuildId());
        repair.setDormRoomId(room.getDormRoomId());
        int i = repairService.addNewOrder(repair);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "添加失败");
        }
    }

    /**
     * 更新订单
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Repair repair, HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        Repair stored = repairService.getById(repair.getId());
        if (stored == null) return Result.error("-1", "报修工单不存在");
        SessionAuth.requireManagedBuild(session, stored.getDormBuildId());
        repair.setDormBuildId(stored.getDormBuildId());
        repair.setDormRoomId(stored.getDormRoomId());
        repair.setRepairer(stored.getRepairer());
        int i = repairService.updateNewOrder(repair);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id, HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        Repair stored = repairService.getById(id);
        if (stored == null) return Result.error("-1", "报修工单不存在");
        SessionAuth.requireManagedBuild(session, stored.getDormBuildId());
        int i = repairService.deleteOrder(id);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 查找订单
     */
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search,
                              HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        Page page = SessionAuth.hasRole(session, "admin")
                ? repairService.find(pageNum, pageSize, search)
                : repairService.findByDormBuild(pageNum, pageSize, search, SessionAuth.dormBuildId(session));
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 个人申报报修 分页查询
     */
    @GetMapping("/find/{name}")
    public Result<?> individualFind(@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(defaultValue = "") String search,
                                    @PathVariable String name,
                                    HttpSession session) {
        if (SessionAuth.hasRole(session, "stu")) {
            SessionAuth.requireSelfOrAdmin(session, name);
        } else {
            SessionAuth.requireRole(session, "admin");
        }
        // 个人报修查询
        Page page = repairService.individualFind(pageNum, pageSize, search, name);
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 首页顶部：报修统计
     */
    @GetMapping("/orderNum")
    public Result<?> orderNum(HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        int num = SessionAuth.hasRole(session, "admin")
                ? repairService.showOrderNum()
                : repairService.showOrderNumByDormBuild(SessionAuth.dormBuildId(session));
        if (num >= 0) {
            return Result.success(num);
        } else {
            return Result.error("-1", "报修统计查询失败");
        }
    }
}
