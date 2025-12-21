package com.example.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// QueueMonitoringService.java
@Service
@RequiredArgsConstructor
@Slf4j
public class QueueMonitoringService {
    private final MeterRegistry meterRegistry;
    private final AdminClient adminClient;

    @Scheduled(fixedRate = 30000) // Every 30s
    public void monitorQueueDepth() {
        try {
            Map<String, ConsumerGroupDescription> groups = adminClient.describeConsumerGroups(Arrays.asList(
                    "job-worker-fast", "job-worker-long"))
                    .all().get();

            for (Map.Entry<String, ConsumerGroupDescription> entry : groups.entrySet()) {
                String groupId = entry.getKey();

                Map<TopicPartition, OffsetAndMetadata> offsets = adminClient.listConsumerGroupOffsets(groupId)
                        .partitionsToOffsetAndMetadata().get();

                long totalLag = calculateTotalLag(offsets);

                meterRegistry.gauge("kafka.consumer.lag",
                        Tags.of("group", groupId), totalLag);

                // Alert if lag > threshold
                if (totalLag > 1000) {
                    log.warn("🚨 High queue depth for {}: {} messages", groupId, totalLag);
                    // Trigger autoscaling or alert
                }

                log.info("Queue depth for {}: {}", groupId, totalLag);
            }
        } catch (Exception e) {
            log.error("Error monitoring queue depth", e);
        }
    }

    private long calculateTotalLag(Map<TopicPartition, OffsetAndMetadata> offsets) {
        // Calculate lag = high water mark - committed offset
        // Implementation depends on your Kafka setup
        return 0; // Simplified
    }

    @Bean
    public AdminClient adminClient(@Value("${spring.kafka.bootstrap-servers}") String servers) {
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        return AdminClient.create(config);
    }
}
