package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

// VideoTranscodeRequest.java
@Data
public class VideoTranscodeRequest {

    @NotBlank
    private String videoUrl;

    @NotNull
    private String targetFormat; // mp4, webm, hls

    private String resolution; // 1080p, 720p, 480p

    private Map<String, Object> options;
}
