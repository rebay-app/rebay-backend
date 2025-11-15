package com.rebay.rebay_backend.statistics.scheduler;

import com.rebay.rebay_backend.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyStatisticsScheduler {
    private final StatisticsService statisticsService;

    @Scheduled(cron = "0 0 0 * * *")
    public void updateDailyStats() {
        statisticsService.getDailyTop10Keywords();
        statisticsService.getAverageEarningsPerUser();
    }
}
