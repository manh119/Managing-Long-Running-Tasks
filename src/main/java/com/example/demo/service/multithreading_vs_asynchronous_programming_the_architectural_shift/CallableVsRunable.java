package com.example.demo.service.multithreading_vs_asynchronous_programming_the_architectural_shift;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CallableVsRunable {

  public static void main(String[] args) throws ExecutionException, InterruptedException {
      // Tạo một ThreadPool có cố định 3 nhân viên (Thread)
      // newFixedThreadPool	-> Dùng implementation ThreadPoolExecutor	LinkedBlockingQueue (Vô hạn)
      // ExecutorService chỉ là interface, dùng mặc định implementation ThreadPoolExecutor
      ExecutorService executor = Executors.newFixedThreadPool(3);

      // 1. Gửi một Runnable (không lấy kết quả)
      executor.execute(() -> System.out.println("Nhân viên 1 đang lau bàn..."));

      // 2. Gửi một Callable (lấy kết quả qua Future)
      Future<String> futureResult = executor.submit(() -> {
          Thread.sleep(2000);
          return "Nước cam đã ép xong!";
      });

      // Làm việc khác trong lúc chờ nước cam...
      System.out.println("Chủ quán đang lướt điện thoại...");

      // Lấy kết quả từ Future (Đây là lúc bị chặn/blocking)
      String juice = futureResult.get();
      System.out.println("Kết quả nhận được: " + juice);

      // Luôn nhớ đóng executor khi không dùng nữa
      executor.shutdown();
  }
}

