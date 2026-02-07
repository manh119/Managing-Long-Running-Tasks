package com.example.demo.service.multithreading_vs_asynchronous_programming_the_architectural_shift;

import java.util.concurrent.*;

public class ThreadPoolExample {
    public static void main(String[] args) {
        // 1. Cấu hình bộ máy "xịn"
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,                      // corePoolSize: Luôn giữ 2 nhân viên trực chiến
                4,                      // maximumPoolSize: Lúc cao điểm, huy động tối đa 4 người
                10,                     // keepAliveTime: Nhân viên tăng cường rảnh 10s là cho nghỉ
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3), // Hàng đợi: Chỉ cho phép 3 khách ngồi chờ
                new ThreadPoolExecutor.AbortPolicy() // Chính sách: Nếu quá tải thì từ chối (ném Exception)
        );

        // 2. Gửi 10 task (đơn hàng) đến trung tâm
        for (int i = 1; i <= 10; i++) {
            int taskId = i;
            try {
                executor.execute(() -> {
                    System.out.println("Task " + taskId + " đang được xử lý bởi: " + Thread.currentThread().getName());
                    try { Thread.sleep(2000); } catch (InterruptedException e) {}
                    System.out.println("Task " + taskId + " xong!");
                });
            } catch (Exception e) {
                System.err.println("Task " + taskId + " bị từ chối! Trung tâm đã quá tải.");
            }
        }

        // Đóng trung tâm sau khi xong việc
        executor.shutdown();
    }
}