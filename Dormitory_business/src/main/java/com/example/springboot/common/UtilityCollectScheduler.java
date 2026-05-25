package com.example.springboot.common;

import com.example.springboot.service.UtilityService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UtilityCollectScheduler {

    @Resource
    private UtilityService utilityService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void collectDailyUtilityUsage() {
        utilityService.simulateAllRooms("SCHEDULED");
    }
}
