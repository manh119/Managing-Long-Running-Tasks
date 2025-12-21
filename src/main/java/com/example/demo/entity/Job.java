package com.example.demo.entity;

import com.example.demo.dto.JobStatus;
import com.example.demo.dto.JobType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

// Job Entity
@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_idempotency", columnList = "idempotencyKey", unique = true),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_submitted", columnList = "submittedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private JobType type;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    @Column(columnDefinition = "TEXT")
    private String payload; // JSON serialized

    private Integer progress;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(columnDefinition = "TEXT")
    private String error;

    private Integer retryCount;

    private LocalDateTime submittedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> metadata;
}
