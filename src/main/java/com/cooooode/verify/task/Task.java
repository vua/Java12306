package com.cooooode.verify.task;

import com.cooooode.verify.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @program: verify
 * @description:
 * @author: vua
 * @create: 2020-02-28 12:07
 */
@Component
@EnableScheduling
public class Task {
    @Autowired
    RecordService recordService;
    @Scheduled(cron="0 0 0 * * *")
    public void clear(){
        recordService.map.clear();
    }
}
