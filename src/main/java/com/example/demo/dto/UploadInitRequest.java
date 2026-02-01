package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadInitRequest {
    
    @NotBlank(message = "Original filename is required")
    @Size(max = 255)
    private String originalFilename;
    
    @NotNull(message = "File size is required")
    @Min(value = 1, message = "File size must be greater than 0")
    @Max(value = 26843545600L, message = "File size exceeds 25GB limit")
    private Long fileSize;
    
    @NotBlank(message = "Content type is required")
    private String contentType;
    
    @NotBlank(message = "SHA-256 checksum is required")
    @Pattern(regexp = "^[a-fA-F0-9]{64}$", message = "Invalid SHA-256 checksum")
    private String checksum;
}

