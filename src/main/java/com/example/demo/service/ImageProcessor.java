package com.example.demo.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.example.demo.dto.ImageProcessRequest;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@Slf4j
public class ImageProcessor {

    public String process(ImageProcessRequest request,
            Consumer<Integer> progressCallback) throws Exception {
        log.info("Starting image processing: {}", request.getImageUrl());

        for (int i = 0; i <= 100; i += 25) {
            Thread.sleep(500);
            progressCallback.accept(i);
        }

        String outputUrl = "https://cdn.example.com/images/" + UUID.randomUUID() + "." + request.getOutputFormat();
        log.info("Image processed successfully: {}", outputUrl);
        return outputUrl;
    }
}
