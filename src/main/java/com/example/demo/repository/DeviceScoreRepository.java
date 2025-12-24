package com.example.demo.repository;

import com.example.demo.entity.DeviceScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface DeviceScoreRepository extends JpaRepository<DeviceScore, String> {
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
}
