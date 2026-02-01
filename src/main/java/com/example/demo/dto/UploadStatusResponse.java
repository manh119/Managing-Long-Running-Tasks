package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadStatusResponse {
    private String uploadId;
    private String status;
    private Long expectedSize;
    private Long actualSize;
    private Integer completedParts;
    private Integer totalParts;
    private String errorMessage;
}
