package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.common.SessionAuth;
import com.example.springboot.entity.DormBuild;
import com.example.springboot.service.DormBuildService;
import com.example.springboot.service.DormRoomService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/building")
public class DormBuildController {

    @Resource
    private DormBuildService dormBuildService;

    @Resource
    private DormRoomService dormRoomService;

    /**
     * 楼宇添加
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody DormBuild dormBuild, HttpSession session) {
        SessionAuth.requireRole(session, "admin");
        int i = dormBuildService.addNewBuilding(dormBuild);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "添加失败");
        }
    }

    /**
     * 楼宇信息更新
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody DormBuild dormBuild, HttpSession session) {
        SessionAuth.requireRole(session, "admin");
        int i = dormBuildService.updateNewBuilding(dormBuild);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    /**
     * 楼宇删除
     */
    @DeleteMapping("/delete/{dormBuildId}")
    public Result<?> delete(@PathVariable Integer dormBuildId, HttpSession session) {
        SessionAuth.requireRole(session, "admin");
        int i = dormBuildService.deleteBuilding(dormBuildId);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 楼宇查找
     */
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search) {
        Page page = dormBuildService.find(pageNum, pageSize, search);
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 首页Echarts 获取楼宇信息
     */
    @GetMapping("/getBuildingName")
    public Result<?> getBuildingName() {
        List<DormBuild> buildingName = dormBuildService.getBuildingId();
        List<Integer> buildingId = buildingName.stream()
                .map(dormBuildId -> dormBuildId.getDormBuildId())
                .collect(Collectors.toList());
        return !buildingId.isEmpty() ?
                Result.success(buildingId) : Result.error("-1", "查询失败");
    }

    /**
     * 首页图表：返回去重、排序后的楼栋及入住人数。
     */
    @GetMapping("/occupancy")
    public Result<?> occupancy() {
        List<Map<String, Object>> distribution = dormBuildService.getBuildingId().stream()
                .map(building -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("dormBuildId", building.getDormBuildId());
                    item.put("dormBuildName", building.getDormBuildName() == null
                            ? building.getDormBuildId() + "号楼"
                            : building.getDormBuildName());
                    item.put("studentCount", dormRoomService.getEachBuildingStuNum(building.getDormBuildId()));
                    return item;
                })
                .collect(Collectors.toList());
        return Result.success(distribution);
    }
}
