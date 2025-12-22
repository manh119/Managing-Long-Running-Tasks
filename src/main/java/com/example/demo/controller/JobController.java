package com.example.demo.controller;

import com.example.demo.dto.JobResponse;
import com.example.demo.dto.VideoTranscodeRequest;
import com.example.demo.entity.Job;
import com.example.demo.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;

// JobController.java
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    /**
     * @param request
     * @param idempotencyKey
     * @return gửi video cần xử lý vào job queue
     */
    @PostMapping("/video-transcode")
    public ResponseEntity<JobResponse> submitVideoTranscode(
            @RequestBody VideoTranscodeRequest request,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        
        // Kiểm tra duplicate submission
        Optional<Job> existing = jobService.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return ResponseEntity.ok(JobResponse.from(existing.get()));
        }
        
        Job job = jobService.submitVideoTranscode(request, idempotencyKey);
        return ResponseEntity.accepted()
                .header("Location", "/api/v1/jobs/" + job.getId())
                .body(JobResponse.from(job));
    }

    /**
     * @param jobId
     * @return lấy trạng thái của một job đang xử lý đến đâu
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJobStatus(@PathVariable String jobId) {
        return jobService.findById(jobId)
                .map(JobResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @param jobId
     * @return Lấy job status theo trạng thái xử lý - ví dụ 25, 50, 75, 100 %
     */
    @GetMapping("/{jobId}/stream")
    public SseEmitter streamJobStatus(@PathVariable String jobId) {
        return jobService.streamJobStatus(jobId);
    }
}


