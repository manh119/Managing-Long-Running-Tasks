package com.example.demo.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceScoreJobLauncher {

    private final Job deviceScoreUploadJob;
    private final JobLauncher jobLauncher;

    @Scheduled(cron = "${custom.properties.device-score.scheduler-cron}")
    public void runJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(deviceScoreUploadJob, params);
    }
}