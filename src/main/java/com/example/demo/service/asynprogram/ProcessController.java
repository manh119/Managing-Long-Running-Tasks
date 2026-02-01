package com.example.demo.service.asynprogram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class ProcessController {

    @Autowired
    private DataProcessingService service;

    @GetMapping("/run-parallel")
    public String run() throws Exception {
        long start = System.currentTimeMillis();

        // Kích hoạt 3 task chạy cùng lúc
        CompletableFuture<String> t1 = service.processData("Task 1");
        CompletableFuture<String> t2 = service.processData("Task 2");
        CompletableFuture<String> t3 = service.processData("Task 3");

        // Chờ tất cả hoàn thành
        CompletableFuture.allOf(t1, t2, t3).join();

        long end = System.currentTimeMillis();
        return "Hoàn thành trong: " + (end - start) + "ms"; 
        // Thay vì mất 6s (tuần tự), nó chỉ mất ~2s (song song)
    }

}