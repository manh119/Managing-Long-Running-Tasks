package com.example.demo.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.example.demo.dto.VideoTranscodeRequest;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@Slf4j
public class VideoTranscoder {

    public String transcode(VideoTranscodeRequest request,
                            Consumer<Integer> progressCallback) throws Exception {
        log.info("Starting video transcode: {} -> {}",
                request.getVideoUrl(), request.getTargetFormat());

        for (int i = 0; i <= 100; i += 10) {
            Thread.sleep(5000);
            progressCallback.accept(i);
            log.info("Transcoding progress: {}%", i);
        }

        String outputUrl = "https://cdn.example.com/videos/" + UUID.randomUUID() + "." + request.getTargetFormat();
        log.info("Video transcoded successfully: {}", outputUrl);
        return outputUrl;
    }
}
