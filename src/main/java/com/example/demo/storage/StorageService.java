package com.example.demo.storage;


import com.example.demo.dto.*;
import com.example.demo.repository.FileUploadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final FileUploadRepository uploadRepository;
    private final String bucketName;

    @Value("${storage.presigned-url-duration:3600}") // 1 hour default
    private long presignedUrlDuration;

    @Value("${storage.multipart-threshold:10485760}") // 10MB default
    private long multipartThreshold;

    @Value("${storage.max-file-size:26843545600}") // 25GB default
    private long maxFileSize;

    @Value("${storage.allowed-content-types:'image/*,video/*,audio/*,application/pdf'}")
    private Set<String> allowedContentTypes;

    /**
     * Generate presigned URL for direct upload (files < 10MB)
     */
    @Transactional
    public UploadInitResponse initiateUpload(UploadInitRequest request, String userId) {
        validateUploadRequest(request);

        // Generate secure storage key
        String storageKey = generateStorageKey(userId, request.getOriginalFilename());

        // Create database record
        FileUpload upload = FileUpload.builder()
                .userId(userId)
                .originalFilename(request.getOriginalFilename())
                .storageKey(storageKey)
                .expectedSize(request.getFileSize())
                .contentType(request.getContentType())
                .expectedChecksum(request.getChecksum())
                .status(FileUpload.UploadStatus.INITIATED)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        upload = uploadRepository.save(upload);

        if (request.getFileSize() < multipartThreshold) {
            // Simple presigned URL for small files
            String presignedUrl = generatePresignedPutUrl(storageKey, request);
            return UploadInitResponse.builder()
                    .uploadId(upload.getId())
                    .storageKey(storageKey)
                    .presignedUrl(presignedUrl)
                    .expiresIn(presignedUrlDuration)
                    .multipart(false)
                    .build();
        } else {
            // Multipart upload for large files
            return initiateMultipartUpload(upload, request);
        }
    }

    /**
     * Initiate multipart upload for large files
     */
    private UploadInitResponse initiateMultipartUpload(FileUpload upload, UploadInitRequest request) {
        CreateMultipartUploadRequest multipartRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(upload.getStorageKey())
                .contentType(request.getContentType())
                .metadata(Map.of(
                        "user-id", upload.getUserId(),
                        "original-filename", upload.getOriginalFilename(),
                        "expected-checksum", upload.getExpectedChecksum()
                ))
                .build();

        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(multipartRequest);

        upload.setUploadId(response.uploadId());
        upload.setStatus(FileUpload.UploadStatus.UPLOADING);

        // Calculate number of parts (5MB minimum per part)
        long partSize = 5 * 1024 * 1024; // 5MB
        int totalParts = (int) Math.ceil((double) request.getFileSize() / partSize);
        upload.setTotalParts(totalParts);
        upload.setCompletedParts(0);

        uploadRepository.save(upload);

        return UploadInitResponse.builder()
                .uploadId(upload.getId())
                .storageKey(upload.getStorageKey())
                .s3UploadId(response.uploadId())
                .multipart(true)
                .totalParts(totalParts)
                .partSize(partSize)
                .expiresIn(presignedUrlDuration)
                .build();
    }

    /**
     * Generate presigned URL for a specific part
     */
    public PartUploadUrlResponse generatePartUploadUrl(String uploadId, int partNumber) {
        FileUpload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found"));

        if (upload.getStatus() != FileUpload.UploadStatus.UPLOADING) {
            throw new IllegalStateException("Upload is not in UPLOADING state");
        }

        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucketName)
                .key(upload.getStorageKey())
                .uploadId(upload.getUploadId())
                .partNumber(partNumber)
                .build();

        UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignedUrlDuration))
                .uploadPartRequest(uploadPartRequest)
                .build();

        PresignedUploadPartRequest presigned = s3Presigner.presignUploadPart(presignRequest);

        return PartUploadUrlResponse.builder()
                .partNumber(partNumber)
                .presignedUrl(presigned.url().toString())
                .expiresIn(presignedUrlDuration)
                .build();
    }

    /**
     * Complete multipart upload
     */
    @Transactional
    public void completeMultipartUpload(String uploadId, List<CompletedPartDto> parts) {
        FileUpload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found"));

        List<CompletedPart> completedParts = parts.stream()
                .map(p -> CompletedPart.builder()
                        .partNumber(p.getPartNumber())
                        .eTag(p.getETag())
                        .build())
                .collect(Collectors.toList());

        CompletedMultipartUpload completedUpload = CompletedMultipartUpload.builder()
                .parts(completedParts)
                .build();

        CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(upload.getStorageKey())
                .uploadId(upload.getUploadId())
                .multipartUpload(completedUpload)
                .build();

        s3Client.completeMultipartUpload(completeRequest);

        upload.setStatus(FileUpload.UploadStatus.UPLOADED);
        upload.setCompletedAt(LocalDateTime.now());
        uploadRepository.save(upload);

        log.info("Multipart upload completed for uploadId: {}", uploadId);
    }

    /**
     * Verify upload completion (called by client after upload)
     */
    @Transactional
    public void verifyUpload(String uploadId) {
        FileUpload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found"));

        try {
            // Check if file exists in S3
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(upload.getStorageKey())
                    .build();

            HeadObjectResponse headResponse = s3Client.headObject(headRequest);

            upload.setActualSize(headResponse.contentLength());

            // Verify checksum if available
            String s3Checksum = headResponse.checksumSHA256();
            if (s3Checksum != null) {
                upload.setActualChecksum(s3Checksum);
                if (!s3Checksum.equals(upload.getExpectedChecksum())) {
                    upload.setStatus(FileUpload.UploadStatus.FAILED);
                    upload.setErrorMessage("Checksum mismatch");
                    uploadRepository.save(upload);
                    throw new IllegalStateException("Checksum verification failed");
                }
            }

            // Verify size
            if (!upload.getActualSize().equals(upload.getExpectedSize())) {
                upload.setStatus(FileUpload.UploadStatus.FAILED);
                upload.setErrorMessage("Size mismatch");
                uploadRepository.save(upload);
                throw new IllegalStateException("Size verification failed");
            }

            upload.setStatus(FileUpload.UploadStatus.VERIFIED);
            upload.setCompletedAt(LocalDateTime.now());
            uploadRepository.save(upload);

            log.info("Upload verified successfully: {}", uploadId);

        } catch (Exception e) {
            upload.setStatus(FileUpload.UploadStatus.FAILED);
            upload.setErrorMessage(e.getMessage());
            uploadRepository.save(upload);
            throw new RuntimeException("Upload verification failed", e);
        }
    }

    /**
     * Generate presigned URL for download with range support
     */
    public String generateDownloadUrl(String uploadId, Long rangeStart, Long rangeEnd) {
        FileUpload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found"));

        if (upload.getStatus() != FileUpload.UploadStatus.VERIFIED) {
            throw new IllegalStateException("File is not ready for download");
        }

        GetObjectRequest.Builder builder = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(upload.getStorageKey());

        if (rangeStart != null && rangeEnd != null) {
            builder.range(String.format("bytes=%d-%d", rangeStart, rangeEnd));
        }

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(builder.build())
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * Abort multipart upload
     */
    @Transactional
    public void abortUpload(String uploadId) {
        FileUpload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found"));

        if (upload.getUploadId() != null) {
            AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(upload.getStorageKey())
                    .uploadId(upload.getUploadId())
                    .build();

            s3Client.abortMultipartUpload(abortRequest);
        }

        upload.setStatus(FileUpload.UploadStatus.ABORTED);
        uploadRepository.save(upload);
    }

    private void validateUploadRequest(UploadInitRequest request) {
        if (request.getFileSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed");
        }

        if (!allowedContentTypes.contains(request.getContentType())) {
            throw new IllegalArgumentException("Content type not allowed");
        }

        if (request.getChecksum() == null || request.getChecksum().length() != 64) {
            throw new IllegalArgumentException("Valid SHA-256 checksum required");
        }
    }

    private String generateStorageKey(String userId, String filename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString();
        String sanitizedFilename = sanitizeFilename(filename);
        return String.format("uploads/%s/%s/%s/%s", userId, timestamp, uuid, sanitizedFilename);
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String generatePresignedPutUrl(String key, UploadInitRequest request) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(request.getContentType())
                .contentLength(request.getFileSize())
                .checksumSHA256(request.getChecksum())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignedUrlDuration))
                .putObjectRequest(putRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    public UploadStatusResponse getUploadStatus(String uploadId) {
        return null;
    }
}