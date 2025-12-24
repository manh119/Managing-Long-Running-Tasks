package com.example.demo.dto;

import lombok.Data;

@Data
public class DeviceScoreProperties {
    private String schedulerCron = "0 0/1 * * * ?";
    private int maxRetry = 3;
    private int batchSize = 100;
}
