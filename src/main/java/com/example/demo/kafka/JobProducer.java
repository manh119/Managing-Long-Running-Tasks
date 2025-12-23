package com.example.demo.kafka;

import com.example.demo.dto.JobMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.jobs.long}")
    private String longQueueTopic;

    private final ObjectMapper objectMapper;

    public void sendToLongQueue(JobMessage message) {
        send(longQueueTopic, message);
    }

    private void send(String topic, JobMessage jobMessage) {
        try {
            String message = objectMapper.writeValueAsString(jobMessage);

            var future = kafkaTemplate.send(topic, jobMessage.getJobId(), message);

            if (future instanceof java.util.concurrent.CompletableFuture) {
                java.util.concurrent.CompletableFuture<?> cf = (java.util.concurrent.CompletableFuture<?>) future;
                cf.whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Sent job {} to {}", jobMessage.getJobId(), topic);
                    } else {
                        log.error("Failed to send job {} to {}", jobMessage.getJobId(), topic, ex);
                    }
                });
            } else {
                // Fallback: log send initiated
                log.info("Initiated send for job {} to {}", jobMessage.getJobId(), topic);
            }
        } catch (Exception e) {
            log.error("Error sending message to Kafka", e);
            throw new RuntimeException("Failed to enqueue job", e);
        }
    }

    public void sendToDLQ(JobMessage message, String reason) {
        String dlqTopic = "jobs.dlq";
        message.setDlqReason(reason);
        send(dlqTopic, message);
        log.warn("Sent job {} to DLQ: {}", message.getJobId(), reason);
    }
}
