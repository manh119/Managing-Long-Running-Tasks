package com.example.demo.kafka;

import com.example.demo.dto.*;
import com.example.demo.entity.Job;
import com.example.demo.repository.JobRepository;
import com.example.demo.service.ImageProcessor;
import com.example.demo.service.SseService;
import com.example.demo.service.VideoTranscoder;
import jakarta.persistence.OptimisticLockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// JobConsumer.java
@Service
@RequiredArgsConstructor
@Slf4j
public class JobConsumer {
    private final JobRepository jobRepository;
    private final JobProducer jobProducer;
    private final SseService sseService;
    private final VideoTranscoder videoTranscoder;
    private final ImageProcessor imageProcessor;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRY_COUNT = 3;

    // Worker cho FAST queue (image processing, PDF gen, etc.)
    @KafkaListener(topics = "${kafka.topics.jobs.fast}", groupId = "job-worker-fast", concurrency = "5", // 5 consumer
                                                                                                         // threads
            properties = {
                    "session.timeout.ms=30000", // 30s session timeout
                    "heartbeat.interval.ms=10000", // 10s heartbeat
                    "max.poll.interval.ms=300000" // 5 min max processing
            })
    public void processFastJob(JobMessage message, Acknowledgment ack) {
        processJob(message, ack, false);
    }

    // Worker cho LONG queue (video transcoding, ML inference)
    @KafkaListener(topics = "${kafka.topics.jobs.long}", groupId = "job-worker-long", concurrency = "3", // Ít worker
                                                                                                         // hơn vì job
                                                                                                         // lâu
            properties = {
                    "session.timeout.ms=60000", // 60s session timeout
                    "heartbeat.interval.ms=20000", // 20s heartbeat
                    "max.poll.interval.ms=1800000" // 30 min max processing
            })
    public void processLongJob(JobMessage message, Acknowledgment ack) {
        processJob(message, ack, true);
    }

    private void processJob(JobMessage message, Acknowledgment ack, boolean isLongJob) {
        String jobId = message.getJobId();

        try {
            // 1. Kiểm tra duplicate work (idempotent check)
            Optional<Job> jobOpt = jobRepository.findById(jobId);
            if (jobOpt.isEmpty()) {
                log.warn("Job {} not found in database", jobId);
                ack.acknowledge();
                return;
            }

            Job job = jobOpt.get();

            // Nếu job đã hoàn thành hoặc đang xử lý bởi worker khác -> skip
            if (job.getStatus() == JobStatus.COMPLETED ||
                    job.getStatus() == JobStatus.DEAD_LETTER) {
                log.info("Job {} already processed, skipping", jobId);
                ack.acknowledge();
                return;
            }

            // 2. Cập nhật trạng thái PROCESSING với optimistic locking
            if (!updateJobStatus(job, JobStatus.PROCESSING)) {
                log.info("Job {} already being processed by another worker", jobId);
                // Không ACK -> message sẽ retry và có thể được worker khác xử lý
                return;
            }

            // 3. Thực thi job
            log.info("Processing job {}: type={}, attempt={}",
                    jobId, job.getType(), message.getAttemptCount());

            String result = executeJob(job);

            // 4. Cập nhật kết quả thành công
            job.setStatus(JobStatus.COMPLETED);
            job.setResult(result);
            job.setProgress(100);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);

            // 5. Thông báo qua SSE
            sseService.sendUpdate(jobId, JobResponse.from(job));

            // 6. ACK message
            ack.acknowledge();

            log.info("Job {} completed successfully", jobId);

        } catch (Exception e) {
            log.error("Error processing job {}: {}", jobId, e.getMessage(), e);
            handleJobFailure(message, ack, e);
        }
    }

    private boolean updateJobStatus(Job job, JobStatus newStatus) {
        try {
            // Sử dụng optimistic locking hoặc atomic update
            job.setStatus(newStatus);
            job.setStartedAt(LocalDateTime.now());
            jobRepository.save(job);
            return true;
        } catch (OptimisticLockException e) {
            // Một worker khác đã cập nhật job này
            return false;
        }
    }

    private String executeJob(Job job) throws Exception {
        switch (job.getType()) {
            case VIDEO_TRANSCODE:
                VideoTranscodeRequest videoReq = deserialize(
                        job.getPayload(), VideoTranscodeRequest.class);
                return videoTranscoder.transcode(videoReq,
                        progress -> updateProgress(job.getId(), progress));

            case IMAGE_PROCESS:
                ImageProcessRequest imageReq = deserialize(
                        job.getPayload(), ImageProcessRequest.class);
                return imageProcessor.process(imageReq,
                        progress -> updateProgress(job.getId(), progress));

            default:
                throw new UnsupportedOperationException(
                        "Job type not supported: " + job.getType());
        }
    }

    private void updateProgress(String jobId, int progress) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setProgress(progress);
            jobRepository.save(job);
            sseService.sendUpdate(jobId, JobResponse.from(job));
        });
    }

    private void handleJobFailure(JobMessage message, Acknowledgment ack, Exception error) {
        String jobId = message.getJobId();
        int attemptCount = message.getAttemptCount() + 1;

        try {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalStateException("Job not found"));

            job.setRetryCount(attemptCount);
            job.setError(error.getMessage());

            if (attemptCount >= MAX_RETRY_COUNT) {
                // Quá số lần retry -> DLQ
                job.setStatus(JobStatus.DEAD_LETTER);
                jobRepository.save(job);

                String reason = String.format(
                        "Max retries exceeded (%d). Error: %s",
                        MAX_RETRY_COUNT, error.getMessage());
                jobProducer.sendToDLQ(message, reason);

                // Alert monitoring system
                alertDLQ(job, reason);

                ack.acknowledge(); // ACK để không retry nữa

                log.error("Job {} moved to DLQ after {} attempts", jobId, attemptCount);
            } else {
                // Retry
                job.setStatus(JobStatus.PENDING);
                jobRepository.save(job);

                // Gửi lại vào queue với backoff
                message.setAttemptCount(attemptCount);
                scheduleRetry(message, attemptCount);

                ack.acknowledge();

                log.warn("Job {} will be retried (attempt {})", jobId, attemptCount);
            }

            // Thông báo lỗi qua SSE
            sseService.sendUpdate(jobId, JobResponse.from(job));

        } catch (Exception e) {
            log.error("Error handling job failure for {}", jobId, e);
            // Không ACK -> Kafka sẽ tự động retry
        }
    }

    private void scheduleRetry(JobMessage message, int attemptCount) {
        // Exponential backoff: 10s, 30s, 90s
        long delaySeconds = (long) (10 * Math.pow(3, attemptCount - 1));

        CompletableFuture.delayedExecutor(delaySeconds, TimeUnit.SECONDS)
                .execute(() -> {
                    if (message.getType() == JobType.VIDEO_TRANSCODE) {
                        jobProducer.sendToLongQueue(message);
                    } else {
                        jobProducer.sendToFastQueue(message);
                    }
                });

        log.info("Scheduled retry for job {} in {}s", message.getJobId(), delaySeconds);
    }

    private void alertDLQ(Job job, String reason) {
        // Gửi alert đến monitoring system (Slack, PagerDuty, etc.)
        log.error("🚨 DLQ ALERT - Job {}: {} - Reason: {}",
                job.getId(), job.getType(), reason);
        // Implement actual alerting logic here
    }

    private <T> T deserialize(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}
