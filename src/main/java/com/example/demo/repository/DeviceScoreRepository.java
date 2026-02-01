package com.example.demo.repository;

import com.example.demo.entity.DeviceScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface DeviceScoreRepository extends JpaRepository<DeviceScore, String> {
    /** Phiên bản ko tốt
     * @param lastCreatedAt
     * @param maxRetry
     * @param limit
     * @return
     */
    // Version 1 :
    @Query(value = """ 
            SELECT * FROM device_score
            WHERE
                (status = 'INIT'
                OR
                (status = 'OPENAPI_FAILED' AND retry_count < :maxRetry))
            AND
                (CAST(:lastCreatedAt AS timestamp) IS NULL
                OR 
                created_at >= CAST(:lastCreatedAt AS timestamp)) 
            ORDER BY created_at ASC 
            LIMIT :limit 
            """,
            nativeQuery = true)
    List<DeviceScore> findNextBatch(@Param("lastCreatedAt") Timestamp lastCreatedAt, @Param("maxRetry") int maxRetry, @Param("limit") int limit );

    /**
     * Version 1 : Tối ưu truy vấn bằng chỉ mục (index) vì có OR nên Postgree ko ăn index
     * @param lastCreatedAt
     * @param maxRetry
     * @param limit
     * @return
     */
    @Query(value = """ 
            (
                SELECT * FROM device_score
                WHERE status = 'INIT'
                  AND (:lastCreatedAt IS NULL OR created_at >= CAST(:lastCreatedAt AS timestamp))
                ORDER BY created_at ASC
                LIMIT :limit
            )
            
            UNION ALL
            
            (
                SELECT * FROM device_score
                WHERE status = 'OPENAPI_FAILED'
                  AND retry_count < :maxRetry
                  AND (:lastCreatedAt IS NULL OR created_at >= CAST(:lastCreatedAt AS timestamp))
                ORDER BY created_at ASC
                LIMIT :limit
            )
            
            ORDER BY created_at ASC
            LIMIT :limit; 
            """,
            nativeQuery = true)
    List<DeviceScore> findNextBatchV1(@Param("lastCreatedAt") Timestamp lastCreatedAt, @Param("maxRetry") int maxRetry, @Param("limit") int limit );

    /**
     * Phiên bản sử dụng pagable chuẩn của java để phân trang kết quả
     * @param status
     * @param maxRetry
     * @param pageable
     * @return
     */
    @Query("SELECT ds FROM DeviceScore ds WHERE ds.status = :status AND ds.retryCount < :maxRetry ORDER BY ds.createdAt ASC, ds.id ASC")
    Page<DeviceScore> findByStatusAndRetryCountLessThan(
            @Param("status") String status,
            @Param("maxRetry") int maxRetry,
            Pageable pageable);
}
