package com.example.demo.storage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_uploads", indexes = {
    @Index(name = "idx_user_status", columnList = "userId,status"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_storage_key", columnList = "storageKey", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false, unique = true)
    private String storageKey;

    @Column(nullable = false)
    private Long expectedSize;

    @Column
    private Long actualSize;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false, length = 64)
    private String expectedChecksum; // SHA-256

    @Column(length = 64)
    private String actualChecksum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadStatus status;

    @Column
    private String uploadId; // For multipart uploads

    @Column
    private Integer completedParts;

    @Column
    private Integer totalParts;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime lastModifiedAt;

    @Column
    private LocalDateTime expiresAt;

    @Column(length = 1000)
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastModifiedAt = LocalDateTime.now();
        if (status == null) {
            status = UploadStatus.INITIATED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedAt = LocalDateTime.now();
    }

    public enum UploadStatus {
        INITIATED,      // Presigned URL generated
        UPLOADING,      // Upload in progress (multipart)
        UPLOADED,       // Upload complete, awaiting verification
        VERIFIED,       // File verified and ready
        FAILED,         // Upload failed
        EXPIRED,        // Upload expired
        ABORTED         // Upload aborted by user
    }
}