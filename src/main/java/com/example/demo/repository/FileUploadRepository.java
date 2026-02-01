package com.example.demo.repository;

import com.example.demo.storage.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, String> {

    Optional<FileUpload> findByStorageKey(String storageKey);

    List<FileUpload> findByUserIdAndStatus(String userId, FileUpload.UploadStatus status);

    @Query("SELECT f FROM FileUpload f WHERE " +
           "(f.status = 'INITIATED' OR f.status = 'UPLOADING' OR f.status = 'FAILED') " +
           "AND f.createdAt < :cutoff")
    List<FileUpload> findExpiredOrFailedUploads(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT f.storageKey FROM FileUpload f WHERE f.status = 'VERIFIED'")
    Set<String> findAllVerifiedStorageKeys();

    @Query("SELECT f FROM FileUpload f WHERE f.status = 'VERIFIED' AND f.completedAt >= :cutoff")
    List<FileUpload> findRecentVerifiedUploads(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT COUNT(f) FROM FileUpload f WHERE f.userId = :userId " +
           "AND f.createdAt >= :since AND f.status = 'VERIFIED'")
    long countUserUploadsInTimeframe(@Param("userId") String userId, 
                                     @Param("since") LocalDateTime since);
}