package com.example.demo.scheduler;

import com.example.demo.entity.DeviceScore;
import com.example.demo.service.DeviceScoreSevice;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.HttpServerErrorException;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DeviceScoreBatchConfig {

    private final JobRepository jobRepository; // Spring Boot auto-config
    private final PlatformTransactionManager transactionManager; // Spring Boot auto-config
    private final EntityManagerFactory entityManagerFactory;
    private final DeviceScoreSevice deviceScoreSevice;
    private final int BATCH_SIZE = 1000;
    private final int MAX_RETRY = 3;

    public JpaPagingItemReader<DeviceScore> deviceScoreReader() {
        return new JpaPagingItemReaderBuilder<DeviceScore>()
                .name("deviceScoreReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT ds FROM DeviceScore ds WHERE ds.status = 'INIT' AND ds.retryCount < :maxRetry ORDER BY ds.createdAt ASC, ds.id ASC")
                .parameterValues(Map.of("maxRetry", MAX_RETRY))
                .pageSize(BATCH_SIZE) // Chunk size
                .build();
    }

    @Bean
    public ItemProcessor<DeviceScore, DeviceScore> uploadProcessor() {
        return deviceScore -> {
            deviceScoreSevice.executeUpload(deviceScore);
            return deviceScore;
        };
    }

    @Bean
    public JpaItemWriter<DeviceScore> deviceScoreWriter() {
        return new JpaItemWriterBuilder<DeviceScore>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step uploadStep() {
        return new StepBuilder("uploadStep", jobRepository)
                .<DeviceScore, DeviceScore>chunk(BATCH_SIZE, transactionManager)
                .reader(deviceScoreReader())
                .processor(uploadProcessor())
                .writer(deviceScoreWriter())
                .faultTolerant()
                // Retry cho transient error (ví dụ timeout, network)
                .retryLimit(3)
                .retry(ConnectException.class)        // Các exception retryable
                .retry(HttpServerErrorException.class)
                .retry(TimeoutException.class)
                // Skip cho lỗi vĩnh viễn (ví dụ validation fail, business error)
                .skipLimit(10000) // Số record max được skip
                .skip(MyPermanentUploadException.class) // Custom exception của bạn
                //.listener(new MySkipListener()) // Optional: log skip
                .build();
    }

    @Bean
    public Job deviceScoreUploadJob() {
        return new JobBuilder("deviceScoreUploadJob", jobRepository)
                .start(uploadStep())
                .build();
    }
}