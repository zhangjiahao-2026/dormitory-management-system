package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.common.SessionAuth;
import com.example.springboot.entity.AdjustRoom;
import com.example.springboot.entity.DormRoom;
import com.example.springboot.entity.Student;
import com.example.springboot.service.AdjustRoomService;
import com.example.springboot.service.DormRoomService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/adjustRoom")
public class AdjustRoomController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private AdjustRoomService adjustRoomService;

    @Resource
    private DormRoomService dormRoomService;


    /**
     * 添加订单
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody AdjustRoom adjustRoom, HttpSession session) {
        SessionAuth.requireRole(session, "stu");
        Student student = (Student) session.getAttribute("User");
        adjustRoom.setId(null);
        adjustRoom.setUsername(student.getUsername());
        adjustRoom.setName(student.getName());
        DormRoom currentRoom = dormRoomService.judgeHadBed(adjustRoom.getUsername());
        if (currentRoom == null) return Result.error("-1", "当前学生尚未分配宿舍");
        adjustRoom.setCurrentRoomId(currentRoom.getDormRoomId());
        adjustRoom.setCurrentBedId(findBedNumber(currentRoom, student.getUsername()));
        if (currentRoom.getDormRoomId().equals(adjustRoom.getTowardsRoomId())) {
            return Result.error("-1", "目标房间不能与当前房间相同");
        }
        DormRoom targetRoom = dormRoomService.getById(adjustRoom.getTowardsRoomId());
        if (targetRoom == null) {
            return Result.error("-1", "目标房间不存在");
        }
        if (!isValidAndEmptyBed(targetRoom, adjustRoom.getTowardsBedId())) {
            return Result.error("-1", "目标床位不存在或已被占用");
        }
        adjustRoom.setState("未处理");
        adjustRoom.setApplyTime(LocalDateTime.now().format(TIME_FORMATTER));
        adjustRoom.setFinishTime(null);

        int result = adjustRoomService.addApply(adjustRoom);
        if (result == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "查询失败");
        }
    }


    /**
     * 更新订单
     */
    @PutMapping("/update/{state}")
    public Result<?> update(@RequestBody AdjustRoom adjustRoom, @PathVariable Boolean state, HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        AdjustRoom stored = adjustRoomService.getById(adjustRoom.getId());
        if (stored == null) return Result.error("-1", "调宿申请不存在");
        requireRoomAccess(stored.getCurrentRoomId(), session);
        requireRoomAccess(stored.getTowardsRoomId(), session);
        adjustRoom.setUsername(stored.getUsername());
        adjustRoom.setCurrentRoomId(stored.getCurrentRoomId());
        adjustRoom.setCurrentBedId(stored.getCurrentBedId());
        adjustRoom.setTowardsRoomId(stored.getTowardsRoomId());
        adjustRoom.setTowardsBedId(stored.getTowardsBedId());
        if (state) {
            // 更新房间表信息
            int i;
            try {
                i = dormRoomService.adjustRoomUpdate(adjustRoom);
            } catch (IllegalArgumentException | IllegalStateException e) {
                return Result.error("-1", e.getMessage());
            }
            if (i != 1) {
                return Result.error("-1", "重复操作");
            }
        }
        //更新调宿表信息
        int i = adjustRoomService.updateApply(adjustRoom);
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
        AdjustRoom stored = adjustRoomService.getById(id);
        if (stored == null) return Result.error("-1", "调宿申请不存在");
        requireRoomAccess(stored.getCurrentRoomId(), session);
        int i = adjustRoomService.deleteAdjustment(id);
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
                ? adjustRoomService.find(pageNum, pageSize, search)
                : adjustRoomService.findByDormBuild(pageNum, pageSize, search, SessionAuth.dormBuildId(session));
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    private void requireRoomAccess(Integer roomId, HttpSession session) {
        DormRoom room = dormRoomService.getById(roomId);
        if (room == null) throw new IllegalArgumentException("房间不存在");
        SessionAuth.requireManagedBuild(session, room.getDormBuildId());
    }

    private int findBedNumber(DormRoom room, String username) {
        if (username.equals(room.getFirstBed())) return 1;
        if (username.equals(room.getSecondBed())) return 2;
        if (username.equals(room.getThirdBed())) return 3;
        if (username.equals(room.getFourthBed())) return 4;
        throw new IllegalStateException("当前床位信息异常");
    }

    private boolean isValidAndEmptyBed(DormRoom room, int bedNumber) {
        switch (bedNumber) {
            case 1: return room.getFirstBed() == null;
            case 2: return room.getSecondBed() == null;
            case 3: return room.getThirdBed() == null;
            case 4: return room.getFourthBed() == null;
            default: return false;
        }
    }
}
