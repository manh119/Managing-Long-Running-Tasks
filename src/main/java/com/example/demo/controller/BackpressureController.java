package com.example.demo.controller;

import com.example.demo.dto.JobRequest;
import com.example.demo.dto.JobStatus;
import com.example.demo.repository.JobRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// BackpressureController.java
@RestController
@RequestMapping("/api/v1/jobs")
@AllArgsConstructor
public class BackpressureController {
        private final JobRepository jobRepository;

        @PostMapping("/submit")
        public ResponseEntity<?> submitJob(@RequestBody JobRequest request) {
                // Kiểm tra queue depth
                long queueDepth = getCurrentQueueDepth();

                // Threshold: 10,000 messages
                if (queueDepth > 10000) {
                        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                        .body(Map.of(
                                                        "error", "System overloaded",
                                                        "message", "Too many jobs in queue. Please try again later.",
                                                        "queueDepth", queueDepth,
                                                        "retryAfter", 300 // 5 minutes
                                        ));
                }

                // Kiểm tra rate limit per user
                String userId = getCurrentUserId();
                long userPendingJobs = jobRepository.countByUserAndStatus(
                                userId, JobStatus.PENDING.name());

                if (userPendingJobs > 50) {
                        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                                        .body(Map.of(
                                                        "error", "Too many pending jobs",
                                                        "message", "You have " + userPendingJobs
                                                                        + " pending jobs. Please wait for some to complete.",
                                                        "pendingJobs", userPendingJobs));
                }

                // Process job normally
                return ResponseEntity.ok("Job accepted");
        }

        private long getCurrentQueueDepth() {
                // Get from monitoring service
                return 0; // Simplified
        }

        private String getCurrentUserId() {
                return "current user id 1";
        }
}
