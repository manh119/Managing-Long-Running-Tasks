package com.example.demo.scheduler;

import com.example.demo.aop.PreventDuplicateMethod;
import com.example.demo.dto.DeviceScoreProperties;
import com.example.demo.entity.DeviceScore;
import com.example.demo.repository.DeviceScoreRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class DeviceScoreScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceScoreScheduler.class);
    private final DeviceScoreRepository deviceScoreRepository;
    //private final OpenApiProxy openApiProxy;
    private DeviceScoreProperties properties;

    // Use a managed ThreadPool
    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(20);

    @Scheduled(cron = "${custom.properties.device-score.scheduler-cron}")
    @PreventDuplicateMethod(key = "SEND_EMAIL_JOB", leaseTime = 300)
    public void runDeviceScoreJob() {
        this.properties = new DeviceScoreProperties();
        Timestamp lastCreatedAt = null;
        String lastId = null; 

        LOGGER.info("[DEVICE_SCORE] Starting job...");

        while (true) {
            List<DeviceScore> batch = deviceScoreRepository.findNextBatch(lastCreatedAt, properties.getMaxRetry(), properties.getBatchSize());
            LOGGER.info("[DEVICE_SCORE] Lấy batch {} records, lastId: {}", properties.getBatchSize(), lastId);

            if (batch.isEmpty()) break;

            List<CompletableFuture<Void>> futures = batch.stream()
                .map(score -> CompletableFuture.runAsync(() -> executeUpload(score), taskExecutor))
                .toList();

            // Wait for batch to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Save updates
            deviceScoreRepository.saveAllAndFlush(batch);
            LOGGER.info("[DEVICE_SCORE] Xử lý thành công batch {} records, lastId: {}", properties.getBatchSize(), lastId);

            DeviceScore lastRecord = batch.get(batch.size() - 1);
            lastCreatedAt = new Timestamp(lastRecord.getCreatedAt().getTime());
        }
    }

    /**
     * xử lý theo Pageble
     */
//    @Scheduled(cron = "${custom.properties.device-score.scheduler-cron}")
//    @PreventDuplicateMethod(key = "SEND_EMAIL_JOB", leaseTime = 300)
    public void runDeviceScoreJobV2() {
        Timestamp lastCreatedAt = null;
        String lastId = null;

        LOGGER.info("[DEVICE_SCORE] Starting job...");

        while (true) {
            List<DeviceScore> batch = deviceScoreRepository.findNextBatch(lastCreatedAt, properties.getMaxRetry(), properties.getBatchSize());
            LOGGER.info("[DEVICE_SCORE] Lấy batch {} records, lastId: {}", properties.getBatchSize(), lastId);

            if (batch.isEmpty()) break;

            List<CompletableFuture<Void>> futures = batch.stream()
                    .map(score -> CompletableFuture.runAsync(() -> executeUpload(score), taskExecutor))
                    .toList();

            // Wait for batch to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Save updates
            deviceScoreRepository.saveAllAndFlush(batch);
            LOGGER.info("[DEVICE_SCORE] Xử lý thành công batch {} records, lastId: {}", properties.getBatchSize(), lastId);

            DeviceScore lastRecord = batch.get(batch.size() - 1);
            lastCreatedAt = new Timestamp(lastRecord.getCreatedAt().getTime());
        }
    }

    // demo sleep 2 seconds between each retry
    private void executeUpload(DeviceScore deviceScore) {
        int currentRetry = deviceScore.getRetryCount();
        boolean success = false;

        while (currentRetry < properties.getMaxRetry() && !success) {
            currentRetry++;
            //success = tryUpload(deviceScore, currentRetry);
            try {
                Thread.sleep(2000); // 1 second
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (!success && currentRetry < properties.getMaxRetry()) {
                //sleepInterval();
            }
        }
        deviceScore.setStatus("SUCCESS");
        deviceScore.setRetryCount(currentRetry);
    }
}