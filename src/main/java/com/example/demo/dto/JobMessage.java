package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// JobMessage.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobMessage {
    private String jobId;
    private JobType type;
    private String payload;
    private Integer attemptCount;
    private String dlqReason;
    private LocalDateTime enqueuedAt;
}
