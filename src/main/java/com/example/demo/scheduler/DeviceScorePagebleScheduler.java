package com.example.demo.scheduler;

import com.example.demo.aop.PreventDuplicateMethod;
import com.example.demo.entity.DeviceScore;
import com.example.demo.repository.DeviceScoreRepository;
import com.example.demo.service.DeviceScoreSevice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class DeviceScorePagebleScheduler {
    private final int BATCH_SIZE = 1000;
    private final int MAX_RETRY = 3;
    private final DeviceScoreRepository deviceScoreRepository;
    private final DeviceScoreSevice deviceScoreSevice;
    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(20);

    /**
     * xử lý theo Pageble của spring
     */
    @Scheduled(cron = "${custom.properties.device-score.scheduler-cron}")
    @PreventDuplicateMethod(key = "DEVICE_SCORE_JOB", leaseTime = 300)
    public void runDeviceScoreJobPageable() {
        log.info("[DEVICE_SCORE] Starting job...");
        int page = 0;

        while (true) {
            // Sử dụng Pageable với sort theo createdAt ASC, rồi id ASC để đảm bảo thứ tự ổn định
            Pageable pageable = PageRequest.of(page, BATCH_SIZE, Sort.by("createdAt").ascending().and(Sort.by("id")));

            Page<DeviceScore> deviceScorePage = deviceScoreRepository.findByStatusAndRetryCountLessThan(
                    "INIT", MAX_RETRY, pageable);

            List<DeviceScore> batch = deviceScorePage.getContent();

            if (batch.isEmpty()) {
                log.info("[DEVICE_SCORE] No more records to process. Job completed.");
                break;
            }

            log.info("[DEVICE_SCORE] Processing batch of {} records (page {})", batch.size(), page);

            // Xử lý async song song trong batch
            List<CompletableFuture<Void>> futures = batch.stream()
                    .map(score -> CompletableFuture.runAsync(() -> deviceScoreSevice.executeUpload(score), taskExecutor))
                    .toList();

            // Đợi toàn bộ batch hoàn thành
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Lưu kết quả sau khi upload
            deviceScoreRepository.saveAllAndFlush(batch);

            log.info("[DEVICE_SCORE] Successfully processed batch of {} records", batch.size());

            page++;

            // Nếu trang hiện tại không đầy (lấy ít hơn batchSize) thì chắc chắn đã hết dữ liệu
            if (batch.size() < BATCH_SIZE) {
                break;
            }
        }

        log.info("[DEVICE_SCORE] Job completed.");
    }
}
