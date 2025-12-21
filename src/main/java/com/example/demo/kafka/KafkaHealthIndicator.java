package com.example.demo.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

// Simplified health check - does not depend on actuator types
@Component
public class KafkaHealthIndicator {

    @Autowired
    private AdminClient adminClient;

    public boolean isHealthy() {
        try {
            adminClient.describeCluster().clusterId().get(5, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
