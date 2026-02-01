package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadInitResponse {
    private String uploadId;
    private String storageKey;
    private String presignedUrl;
    private String s3UploadId;
    private Boolean multipart;
    private Integer totalParts;
    private Long partSize;
    private Long expiresIn;
}
