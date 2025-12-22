package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.Job;
import com.example.demo.kafka.JobProducer;
import com.example.demo.repository.JobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

// JobService.java
@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {
    private final JobRepository jobRepository;
    private final JobProducer jobProducer;
    private final ObjectMapper objectMapper;
    private final SseService sseService;

    public Job submitVideoTranscode(VideoTranscodeRequest request, String idempotencyKey) {
        // Tạo idempotency key nếu không có
        if (idempotencyKey == null) {
            idempotencyKey = generateIdempotencyKey(request);
        }

        Job job = Job.builder()
                .id(UUID.randomUUID().toString())
                .type(JobType.VIDEO_TRANSCODE)
                .status(JobStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .payload(serializePayload(request))
                .progress(0)
                .retryCount(0)
                .submittedAt(LocalDateTime.now())
                .metadata(new HashMap<>())
                .build();

        // Lưu vào DB trước khi gửi Kafka (đảm bảo tracking)
        job = jobRepository.save(job);

        // Gửi vào Kafka queue - LONG queue cho video transcoding
        JobMessage message = JobMessage.builder()
                .jobId(job.getId())
                .type(job.getType())
                .payload(job.getPayload())
                .attemptCount(0)
                .build();

        jobProducer.sendToLongQueue(message);

        log.info("Submitted video transcode job: {}", job.getId());
        return job;
    }

    public Optional<Job> findById(String jobId) {
        return jobRepository.findById(jobId);
    }


    public Optional<Job> findByIdempotencyKey(String key) {
        return jobRepository.findByIdempotencyKey(key);
    }

    public SseEmitter streamJobStatus(String jobId) {
        return sseService.createEmitter(jobId);
    }

    private String generateIdempotencyKey(Object request) {
        try {
            // Tạo key từ user context + request + time window (5 phút)
            String userId = "user id 1";
            long timeWindow = System.currentTimeMillis() / (5 * 60 * 1000); // 5 min window
            String content = userId + ":" + objectMapper.writeValueAsString(request) + ":" + timeWindow;
            return DigestUtils.sha256Hex(content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate idempotency key", e);
        }
    }

    private String serializePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }
    }
}
