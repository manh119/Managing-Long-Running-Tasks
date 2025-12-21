package com.example.demo.dto;

import com.example.demo.entity.Job;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

// JobResponse.java
@Data
@Builder
public class JobResponse {
    private String jobId;
    private JobType type;
    private JobStatus status;
    private Integer progress; // 0-100
    private String result;
    private String error;
    private LocalDateTime submittedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Map<String, Object> metadata;
    
    public static JobResponse from(Job job) {
        return JobResponse.builder()
                .jobId(job.getId())
                .type(job.getType())
                .status(job.getStatus())
                .progress(job.getProgress())
                .result(job.getResult())
                .error(job.getError())
                .submittedAt(job.getSubmittedAt())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .metadata(job.getMetadata())
                .build();
    }
}