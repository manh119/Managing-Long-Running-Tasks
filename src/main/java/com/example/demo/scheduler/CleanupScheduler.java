package com.example.demo.scheduler;

import com.example.demo.repository.FileUploadRepository;
import com.example.demo.storage.FileUpload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupScheduler {

    private final S3Client s3Client;
    private final FileUploadRepository uploadRepository;
    private final String bucketName;

    /**
     * Clean up expired and failed uploads
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredUploads() {
        log.info("Starting cleanup of expired uploads");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusDays(2);

        // Find expired or failed uploads older than 2 days
        List<FileUpload> expiredUploads = uploadRepository.findExpiredOrFailedUploads(cutoff);

        for (FileUpload upload : expiredUploads) {
            try {
                // Abort multipart upload if exists
                if (upload.getUploadId() != null) {
                    abortMultipartUpload(upload);
                }

                // Delete file from S3 if exists
                deleteFromS3(upload.getStorageKey());

                // Mark as expired in database
                upload.setStatus(FileUpload.UploadStatus.EXPIRED);
                uploadRepository.save(upload);

                log.info("Cleaned up expired upload: {}", upload.getId());
            } catch (Exception e) {
                log.error("Failed to cleanup upload: {}", upload.getId(), e);
            }
        }

        log.info("Completed cleanup of {} expired uploads", expiredUploads.size());
    }

    /**
     * Reconcile S3 and database state
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void reconcileStorageState() {
        log.info("Starting storage reconciliation");

        // Get all storage keys from database
        Set<String> dbKeys = uploadRepository.findAllVerifiedStorageKeys();

        // List all objects in S3
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix("uploads/")
                .build();

        ListObjectsV2Response listResponse;
        int orphanedCount = 0;
        int reconciledCount = 0;

        do {
            listResponse = s3Client.listObjectsV2(listRequest);

            for (S3Object s3Object : listResponse.contents()) {
                String key = s3Object.key();

                if (!dbKeys.contains(key)) {
                    // Orphaned file in S3 - not in database
                    if (isOlderThan(s3Object.lastModified(), 2)) {
                        deleteFromS3(key);
                        orphanedCount++;
                        log.info("Deleted orphaned file from S3: {}", key);
                    }
                } else {
                    // Verify file metadata matches
                    FileUpload upload = uploadRepository.findByStorageKey(key)
                            .orElse(null);

                    if (upload != null && upload.getActualSize() == null) {
                        upload.setActualSize(s3Object.size());
                        uploadRepository.save(upload);
                        reconciledCount++;
                    }
                }
            }

            listRequest = listRequest.toBuilder()
                    .continuationToken(listResponse.nextContinuationToken())
                    .build();

        } while (listResponse.isTruncated());

        log.info("Storage reconciliation completed. Orphaned: {}, Reconciled: {}", 
                orphanedCount, reconciledCount);
    }

    /**
     * Clean up incomplete multipart uploads
     * Runs daily at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupIncompleteMultipartUploads() {
        log.info("Starting cleanup of incomplete multipart uploads");

        ListMultipartUploadsRequest listRequest = ListMultipartUploadsRequest.builder()
                .bucket(bucketName)
                .build();

        ListMultipartUploadsResponse listResponse = s3Client.listMultipartUploads(listRequest);

        int abortedCount = 0;

        for (MultipartUpload upload : listResponse.uploads()) {
            // Abort uploads older than 2 days
            if (isOlderThan(upload.initiated(), 2)) {
                try {
                    AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                            .bucket(bucketName)
                            .key(upload.key())
                            .uploadId(upload.uploadId())
                            .build();

                    s3Client.abortMultipartUpload(abortRequest);
                    abortedCount++;

                    log.info("Aborted incomplete multipart upload: {}", upload.uploadId());
                } catch (Exception e) {
                    log.error("Failed to abort multipart upload: {}", upload.uploadId(), e);
                }
            }
        }

        log.info("Cleaned up {} incomplete multipart uploads", abortedCount);
    }

    /**
     * Scan files for security threats (placeholder for virus scanning)
     * Runs every 6 hours
     */
    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void scanRecentUploads() {
        log.info("Starting security scan of recent uploads");

        LocalDateTime cutoff = LocalDateTime.now().minusHours(6);
        List<FileUpload> recentUploads = uploadRepository.findRecentVerifiedUploads(cutoff);

        for (FileUpload upload : recentUploads) {
            try {
                // Placeholder for actual virus scanning logic
                // You would integrate with ClamAV, VirusTotal API, etc.
                boolean isSafe = scanFile(upload);

                if (!isSafe) {
                    // Quarantine or delete the file
                    deleteFromS3(upload.getStorageKey());
                    upload.setStatus(FileUpload.UploadStatus.FAILED);
                    upload.setErrorMessage("Security threat detected");
                    uploadRepository.save(upload);

                    log.warn("Security threat detected in file: {}", upload.getId());
                }
            } catch (Exception e) {
                log.error("Failed to scan file: {}", upload.getId(), e);
            }
        }

        log.info("Completed security scan of {} files", recentUploads.size());
    }

    private void abortMultipartUpload(FileUpload upload) {
        try {
            AbortMultipartUploadRequest request = AbortMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(upload.getStorageKey())
                    .uploadId(upload.getUploadId())
                    .build();

            s3Client.abortMultipartUpload(request);
        } catch (NoSuchUploadException e) {
            log.debug("Multipart upload already completed or aborted: {}", upload.getUploadId());
        }
    }

    private void deleteFromS3(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
        } catch (Exception e) {
            log.error("Failed to delete object from S3: {}", key, e);
        }
    }

    private boolean isOlderThan(java.time.Instant instant, int days) {
        return instant.isBefore(java.time.Instant.now().minus(java.time.Duration.ofDays(days)));
    }

    private boolean scanFile(FileUpload upload) {
        // Placeholder implementation
        // In production, integrate with:
        // - ClamAV for virus scanning
        // - VirusTotal API
        // - AWS Macie for sensitive data detection
        // - Custom ML models for content moderation
        
        return true; // Assume safe for now
    }
}