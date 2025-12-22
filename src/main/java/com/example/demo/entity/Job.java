package com.example.demo.entity;

import com.example.demo.dto.JobStatus;
import com.example.demo.dto.JobType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

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

    @Column(name = "idempotency_key", unique = true, nullable = false)
    private String idempotencyKey;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private Integer progress;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(columnDefinition = "TEXT")
    private String error;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
}
