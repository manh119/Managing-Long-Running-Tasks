package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

// ImageProcessRequest.java
@Data
public class ImageProcessRequest {

    @NotBlank
    private String imageUrl;

    @NotNull
    private List<ImageOperation> operations; // resize, crop, filter

    private String outputFormat; // jpg, png, webp
}
