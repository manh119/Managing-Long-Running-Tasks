package com.example.demo.service.asynprogram;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class DataProcessingService {

    @Async("taskExecutor")
    public CompletableFuture<String> processData(String input) throws InterruptedException {
        System.out.println("Đang xử lý: " + input + " trên thread: " + Thread.currentThread().getName());
        
        // Giả lập tác vụ nặng (ví dụ gọi API mất 2 giây)
        Thread.sleep(2000); 
        
        return CompletableFuture.completedFuture("Kết quả cho: " + input);
    }
}