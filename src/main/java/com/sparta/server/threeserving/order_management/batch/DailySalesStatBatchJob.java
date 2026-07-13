package com.sparta.server.threeserving.order_management.batch;

import com.sparta.server.threeserving.order_management.service.DailySalesStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DailySalesStatBatchJob {

    private final DailySalesStatService dailySalesStatService;


    @Scheduled(cron = "0 0 2 * * *")
    public void execute() {

        LocalDate yesterday = LocalDate.now().minusDays(1);

        dailySalesStatService.createDailySalesStat(yesterday);
    }
}
