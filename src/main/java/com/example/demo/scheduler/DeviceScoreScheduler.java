package com.example.demo.scheduler;

import com.example.demo.aop.PreventDuplicateMethod;
import com.example.demo.entity.DeviceScore;
import com.example.demo.repository.DeviceScoreRepository;
import com.example.demo.service.DeviceScoreSevice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class DeviceScoreScheduler {
    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(20);
    private final DeviceScoreRepository deviceScoreRepository;
    private final DeviceScoreSevice deviceScoreSevice;
    private final int BATCH_SIZE = 1000;
    private final int MAX_RETRY = 3;

    //@Scheduled(cron = "${custom.properties.device-score.scheduler-cron}")
    @PreventDuplicateMethod(key = "SEND_EMAIL_JOB", leaseTime = 300)
    public void runDeviceScoreJob() {
        Timestamp lastCreatedAt = null;
        String lastId = null; 

        log.info("[DEVICE_SCORE] Starting job...");

        while (true) {
            List<DeviceScore> batch = deviceScoreRepository.findNextBatch(lastCreatedAt, MAX_RETRY, BATCH_SIZE);
            log.info("[DEVICE_SCORE] Lấy batch {} records, lastId: {}", BATCH_SIZE, lastId);

            if (batch.isEmpty()) break;

            List<CompletableFuture<Void>> futures = batch.stream()
                .map(score -> CompletableFuture.runAsync(() -> deviceScoreSevice.executeUpload(score), taskExecutor))
                .toList();

            // Wait for batch to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Save updates
            deviceScoreRepository.saveAllAndFlush(batch);
            log.info("[DEVICE_SCORE] Xử lý thành công batch {} records, lastId: {}", BATCH_SIZE, lastId);

            DeviceScore lastRecord = batch.get(batch.size() - 1);
            lastCreatedAt = new Timestamp(lastRecord.getCreatedAt().getTime());
        }
    }
}