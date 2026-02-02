package com.example.demo.service.asynprogram;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);      // Số thread luôn duy trì
        executor.setMaxPoolSize(100);     // Số thread tối đa khi hàng đợi đầy
        executor.setQueueCapacity(100);  // Số lượng task đợi trong hàng hàng đợi
        executor.setThreadNamePrefix("SpringAsync-");
        executor.initialize();
        return executor;
    }
}