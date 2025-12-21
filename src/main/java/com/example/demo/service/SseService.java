package com.example.demo.service;

import com.example.demo.dto.JobResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// SseService.java
@Service
@Slf4j
public class SseService {
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String jobId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30 min timeout

        emitter.onCompletion(() -> removeEmitter(jobId, emitter));
        emitter.onTimeout(() -> removeEmitter(jobId, emitter));
        emitter.onError(e -> removeEmitter(jobId, emitter));

        emitters.computeIfAbsent(jobId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        return emitter;
    }

    public void sendUpdate(String jobId, JobResponse update) {
        List<SseEmitter> jobEmitters = emitters.get(jobId);
        if (jobEmitters != null) {
            List<SseEmitter> deadEmitters = new ArrayList<>();

            for (SseEmitter emitter : jobEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("job-update")
                            .data(update));
                } catch (Exception e) {
                    deadEmitters.add(emitter);
                }
            }

            deadEmitters.forEach(e -> removeEmitter(jobId, e));
        }
    }

    private void removeEmitter(String jobId, SseEmitter emitter) {
        List<SseEmitter> jobEmitters = emitters.get(jobId);
        if (jobEmitters != null) {
            jobEmitters.remove(emitter);
            if (jobEmitters.isEmpty()) {
                emitters.remove(jobId);
            }
        }
    }
}
