package com.example.demo.service.asynprogram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/benchmark")
public class BenchmarkController {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkController.class);

    // 1. Thread Pool truyền thống (Tomcat mặc định thường có 200 threads)
    @GetMapping("/traditional")
    public String traditional() throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("Traditional thread: " + Thread.currentThread().getName());
        return "Done";
    }

    // 2. CompletableFuture (Sử dụng ForkJoinPool chung)
    @GetMapping("/completable")
    public CompletableFuture<String> completable() {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            return "Done";
        });
    }

    // 3. Virtual Threads (Phải bật trong application.properties)
    // spring.threads.virtual.enabled=true
    @GetMapping("/virtual")
    public String virtual() throws InterruptedException {
        Thread.sleep(1000);
        log.info("Virtual thread: {}is virtual thread: {}", Thread.currentThread().toString(), Thread.currentThread().isVirtual());
        return "Done";
    }
}