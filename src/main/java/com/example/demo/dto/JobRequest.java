package com.example.demo.dto;

import lombok.Data;
import java.util.Map;

@Data
public class JobRequest {
    private JobType type;
    private Map<String, Object> payload;
    private String idempotencyKey;
}
