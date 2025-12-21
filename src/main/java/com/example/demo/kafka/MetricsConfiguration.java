package com.example.demo.kafka;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// MetricsConfiguration.java
@Configuration
public class MetricsConfiguration {

    // Custom metrics
    @Bean
    public Counter jobSubmittedCounter(MeterRegistry registry) {
        return Counter.builder("jobs.submitted")
                .description("Total jobs submitted")
                .tag("type", "all")
                .register(registry);
    }

    @Bean
    public Counter jobCompletedCounter(MeterRegistry registry) {
        return Counter.builder("jobs.completed")
                .description("Total jobs completed")
                .tag("status", "success")
                .register(registry);
    }

    @Bean
    public Counter jobFailedCounter(MeterRegistry registry) {
        return Counter.builder("jobs.failed")
                .description("Total jobs failed")
                .tag("status", "error")
                .register(registry);
    }

    @Bean
    public Timer jobProcessingTimer(MeterRegistry registry) {
        return Timer.builder("jobs.processing.duration")
                .description("Job processing duration")
                .register(registry);
    }
}
