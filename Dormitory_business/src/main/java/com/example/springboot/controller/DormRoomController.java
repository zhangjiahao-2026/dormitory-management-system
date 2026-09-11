package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.common.SessionAuth;
import com.example.springboot.entity.DormRoom;
import com.example.springboot.service.DormRoomService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;

@RestController
@RequestMapping("/room")
public class DormRoomController {

    @Resource
    private DormRoomService dormRoomService;

    /**
     * 添加房间
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody DormRoom dormRoom, HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        SessionAuth.requireManagedBuild(session, dormRoom.getDormBuildId());
        int i = dormRoomService.addNewRoom(dormRoom);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "添加失败");
        }
    }

    /**
     * 更新房间
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody DormRoom dormRoom, HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        DormRoom stored = dormRoomService.getById(dormRoom.getDormRoomId());
        if (stored == null) return Result.error("-1", "房间不存在");
        SessionAuth.requireManagedBuild(session, stored.getDormBuildId());
        dormRoom.setDormBuildId(stored.getDormBuildId());
        int i = dormRoomService.updateNewRoom(dormRoom);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    /**
     * 删除房间
     */
    @DeleteMapping("/delete/{dormRoomId}")
    public Result<?> delete(@PathVariable Integer dormRoomId, HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        DormRoom stored = dormRoomService.getById(dormRoomId);
        if (stored == null) return Result.error("-1", "房间不存在");
        SessionAuth.requireManagedBuild(session, stored.getDormBuildId());
        int i = dormRoomService.deleteRoom(dormRoomId);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 查找房间
     */
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search,
                              HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        Page page = SessionAuth.hasRole(session, "admin")
                ? dormRoomService.find(pageNum, pageSize, search)
                : dormRoomService.findByDormBuild(pageNum, pageSize, search, SessionAuth.dormBuildId(session));
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 首页顶部：空宿舍统计
     */
    @GetMapping("/noFullRoom")
    public Result<?> noFullRoom() {
        int num = dormRoomService.notFullRoom();
        if (num >= 0) {
            return Result.success(num);
        } else {
            return Result.error("-1", "空宿舍查询失败");
        }
    }

    /**
     * 删除床位学生信息
     */
    @DeleteMapping("/delete/{bedName}/{dormRoomId}/{calCurrentNum}")
    public Result<?> deleteBedInfo(@PathVariable String bedName, @PathVariable Integer dormRoomId,
                                   @PathVariable int calCurrentNum, HttpSession session) {
        SessionAuth.requireRole(session, "admin", "dormManager");
        DormRoom stored = dormRoomService.getById(dormRoomId);
        if (stored == null) return Result.error("-1", "房间不存在");
        SessionAuth.requireManagedBuild(session, stored.getDormBuildId());
        if (!java.util.Set.of("first_bed", "second_bed", "third_bed", "fourth_bed").contains(bedName)) {
            return Result.error("400", "床位名称不合法");
        }
        int i = dormRoomService.deleteBedInfo(bedName, dormRoomId, stored.getCurrentCapacity());
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 床位信息，查询该学生是否已有床位
     */
    @GetMapping("/judgeHadBed/{value}")
    public Result<?> judgeHadBed(@PathVariable String value) {
        DormRoom dormRoom = dormRoomService.judgeHadBed(value);
        if (dormRoom == null) {
            return Result.success();
        } else {
            return Result.error("-1", "该学生已有宿舍");
        }
    }

    /**
     * 主页 住宿人数
     */
    @GetMapping("/selectHaveRoomStuNum")
    public Result<?> selectHaveRoomStuNum() {
        Long count = dormRoomService.selectHaveRoomStuNum();
        if (count >= 0) {
            return Result.success(count);
        } else {
            return Result.error("-1", "查询首页住宿人数失败");
        }
    }

    /**
     * 住宿分布人数
     */
    @GetMapping("/getEachBuildingStuNum/{num}")
    public Result<?> getEachBuildingStuNum(@PathVariable int num) {
        ArrayList<Long> arrayList = new ArrayList();
        for (int i = 1; i <= num; i++) {
            Long eachBuildingStuNum = dormRoomService.getEachBuildingStuNum(i);
            arrayList.add(eachBuildingStuNum);
        }

        if (!arrayList.isEmpty()) {
            return Result.success(arrayList);
        } else {
            return Result.error("-1", "获取人数失败");
        }
    }

    /**
     * 学生功能： 我的宿舍
     */
    @GetMapping("/getMyRoom/{name}")
    public Result<?> getMyRoom(@PathVariable String name, HttpSession session) {
        if (SessionAuth.hasRole(session, "stu")) SessionAuth.requireSelfOrAdmin(session, name);
        DormRoom dormRoom = dormRoomService.judgeHadBed(name);
        if (dormRoom != null) {
            return Result.success(dormRoom);
        } else {
            return Result.error("-1", "不存在该生");
        }
    }

    /**
     * 检查房间是否满员
     */
    @GetMapping("/checkRoomState/{dormRoomId}")
    public Result<?> checkRoomState(@PathVariable Integer dormRoomId) {
        DormRoom dormRoom = dormRoomService.checkRoomState(dormRoomId);
        if (dormRoom != null) {
            return Result.success(dormRoom);
        } else {
            return Result.error("-1", "该房间人满了");
        }
    }

    /**
     * 检查床位是否已经有人
     */
    @GetMapping("/checkBedState/{dormRoomId}/{bedNum}")
    public Result<?> getMyRoom(@PathVariable Integer dormRoomId, @PathVariable int bedNum) {
        DormRoom dormRoom = dormRoomService.checkBedState(dormRoomId, bedNum);
        if (dormRoom != null) {
            return Result.success(dormRoom);
        } else {
            return Result.error("-1", "该床位已有人");
        }
    }

    /**
     * 检查房间是否满员
     */
    @GetMapping("/checkRoomExist/{dormRoomId}")
    public Result<?> checkRoomExist(@PathVariable Integer dormRoomId) {
        DormRoom dormRoom = dormRoomService.checkRoomExist(dormRoomId);
        if (dormRoom != null) {
            return Result.success(dormRoom);
        } else {
            return Result.error("-1", "不存在该房间");
        }
    }
}
