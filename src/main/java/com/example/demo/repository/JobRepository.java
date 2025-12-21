package com.example.demo.repository;


import com.example.demo.dto.JobStatus;
import com.example.demo.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {
    
    Optional<Job> findByIdempotencyKey(String idempotencyKey);
    
    long countByIdempotencyKey(String idempotencyKey);
    
    @Query("SELECT COUNT(j) FROM Job j WHERE j.metadata->>'userId' = :userId AND j.status = :status")
    long countByUserAndStatus(@Param("userId") String userId, @Param("status") JobStatus status);
    
    @Query("SELECT j FROM Job j WHERE j.metadata->>'workflowId' = :workflowId " +
           "AND j.metadata->>'parentJobId' = :parentJobId")
    Optional<Job> findByWorkflowAndParent(@Param("workflowId") String workflowId, 
                                          @Param("parentJobId") String parentJobId);
}


