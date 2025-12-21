package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/// Async Job Processor Application
/// Main Spring Boot application for processing jobs asynchronously using Kafka.
/// Features:
/// - Async job submission with immediate response
/// - Kafka-based message queue (fast & long queues)
/// - Auto retry with exponential backoff
/// - Dead Letter Queue (DLQ) for failed jobs
/// - SSE for real-time job status updates
/// - Idempotency to prevent duplicate processing
/// - Workflow orchestration for dependent jobs
/// - Prometheus metrics & monitoring
///
/// @author Your Name
@SpringBootApplication
@EnableKafka
@EnableScheduling
public class AsyncJobProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsyncJobProcessorApplication.class, args);
        System.out.println("""
            
            ========================================
            🚀 Async Job Processor Started
            ========================================
            
            API Endpoints:
            - POST /api/v1/jobs/video-transcode
            - POST /api/v1/jobs/image-process
            - GET  /api/v1/jobs/{jobId}
            - GET  /api/v1/jobs/{jobId}/stream (SSE)
            - POST /api/v1/workflows/reports
            - GET  /actuator/health
            - GET  /actuator/metrics
            - GET  /actuator/prometheus
            
            Monitoring:
            - Application: http://localhost:8081
            - Kafka UI:    http://localhost:8080
            - Prometheus:  http://localhost:9090
            - Grafana:     http://localhost:3000
            
            ========================================
            """);
    }
}