package com.example.demo.repository;


import com.example.demo.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {
    
    Optional<Job> findByIdempotencyKey(String idempotencyKey);

    @Query(
            value = """
    SELECT COUNT(*)
    FROM jobs j
    WHERE j.metadata ->> 'userId' = :userId
      AND j.status = :status
    """,
            nativeQuery = true
    )
    long countByUserAndStatus(
            @Param("userId") String userId,
            @Param("status") String status
    );

}


