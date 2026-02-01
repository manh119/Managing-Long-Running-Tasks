package com.example.demo.controller;


import com.example.demo.dto.*;
import com.example.demo.storage.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// TODO : test với front end ở https://claude.ai/chat/9a54274a-3e38-42e3-a28f-60617912f685
@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    /**
     * Initiate file upload
     * POST /api/storage/uploads/init
     */
    @PostMapping("/uploads/init")
    public ResponseEntity<UploadInitResponse> initiateUpload(
            @Valid @RequestBody UploadInitRequest request) {
        String username = "admin";
        UploadInitResponse response = storageService.initiateUpload(request, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Get presigned URL for a specific part (multipart upload)
     * GET /api/storage/uploads/{uploadId}/parts/{partNumber}/url
     */
    @GetMapping("/uploads/{uploadId}/parts/{partNumber}/url")
    public ResponseEntity<PartUploadUrlResponse> getPartUploadUrl(
            @PathVariable String uploadId,
            @PathVariable int partNumber) {

        PartUploadUrlResponse response = storageService.generatePartUploadUrl(uploadId, partNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Complete multipart upload
     * POST /api/storage/uploads/{uploadId}/complete
     */
    @PostMapping("/uploads/{uploadId}/complete")
    public ResponseEntity<Void> completeUpload(
            @PathVariable String uploadId,
            @Valid @RequestBody CompleteUploadRequest request) {

        storageService.completeMultipartUpload(uploadId, request.getParts());
        return ResponseEntity.ok().build();
    }

    /**
     * Verify upload completion
     * POST /api/storage/uploads/{uploadId}/verify
     */
    @PostMapping("/uploads/{uploadId}/verify")
    public ResponseEntity<Void> verifyUpload(@PathVariable String uploadId) {
        storageService.verifyUpload(uploadId);
        return ResponseEntity.ok().build();
    }

    /**
     * Abort upload
     * DELETE /api/storage/uploads/{uploadId}
     */
    @DeleteMapping("/uploads/{uploadId}")
    public ResponseEntity<Void> abortUpload(@PathVariable String uploadId) {
        storageService.abortUpload(uploadId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get download URL
     * GET /api/storage/uploads/{uploadId}/download
     */
    @GetMapping("/uploads/{uploadId}/download")
    public ResponseEntity<DownloadUrlResponse> getDownloadUrl(
            @PathVariable String uploadId,
            @RequestParam(required = false) Long rangeStart,
            @RequestParam(required = false) Long rangeEnd) {

        String url = storageService.generateDownloadUrl(uploadId, rangeStart, rangeEnd);
        return ResponseEntity.ok(DownloadUrlResponse.builder()
                .downloadUrl(url)
                .expiresIn(3600L)
                .build());
    }

    /**
     * Get upload status
     * GET /api/storage/uploads/{uploadId}/status
     */
    @GetMapping("/uploads/{uploadId}/status")
    public ResponseEntity<UploadStatusResponse> getUploadStatus(@PathVariable String uploadId) {
        UploadStatusResponse response = storageService.getUploadStatus(uploadId);
        return ResponseEntity.ok(response);
    }
}