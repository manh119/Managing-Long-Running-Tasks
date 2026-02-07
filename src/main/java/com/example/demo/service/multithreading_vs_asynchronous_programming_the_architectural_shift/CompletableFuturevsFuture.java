package com.example.demo.service.multithreading_vs_asynchronous_programming_the_architectural_shift;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CompletableFuturevsFuture {
    public static void main(String[] args) throws InterruptedException {
        //runWithFuture();

        // là cải tiến của Future, vì Future chỉ có thể trả về giá trị sau khi hoàn thành và block luồng Main
        // CompletableFuture có thể trả về giá trị bất cứ lúc nào
        // Có thể dùng CompletableFuture.allOf() để đợi nhiều task hoàn thành
        runWithCompatableFuture();
    }

    private static void runWithCompatableFuture() throws InterruptedException {
        CompletableFuture.supplyAsync(() -> {
                    // Bước 1: Sửa xe (supplyAsync) - Supply không nhận gì nhưng trả về giá trị
                    System.out.println("Đang sửa xe...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                    }
                    return "Xe SH";
                })
                .thenApply(xe -> {
                    // Bước 2: Nhận xe đã sửa và Thay dầu (Chuyển đổi dữ liệu - Function)
                    System.out.println("Đang thay dầu cho " + xe);
                    return xe + " đã thay dầu";
                })
                .thenAccept(result -> {
                    // Bước 3: Nhận kết quả và in hóa đơn (Tiêu thụ dữ liệu - Consumer)
                    System.out.println("Gửi SMS: " + result + ". Mời bạn đến nhận!");
                })
                .thenRun(() -> {
                    // Bước 4: Việc cuối cùng không cần quan tâm kết quả (Runnable)
                    System.out.println("Dọn dẹp xưởng sửa xe.");
                })
                .exceptionally(ex -> {
                    // Xử lý lỗi nếu bất kỳ bước nào ở trên "tạch"
                    System.out.println("Lỗi rồi: " + ex.getMessage());
                    return null;
                });

        // Luồng Main cực kỳ rảnh rang
        System.out.println("Tôi đi chơi đây, lúc nào xong tự khắc có thông báo!");
        Thread.sleep(3000); // Giữ main thread để chờ xem kết quả async
    }

    private static void runWithFuture() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Gửi task đi sửa xe
        Future<String> future = executor.submit(() -> {
            Thread.sleep(2000); // Giả lập sửa xe mất 2s
            return "Xe đã sửa xong!";
        });

        // Trong lúc này bạn muốn làm việc khác nhưng...
        System.out.println("Tôi đang đợi xe...");

        try {
            // Dòng này sẽ KHÓA (Block) luồng main lại cho đến khi thợ sửa xong
            String result = future.get();
            System.out.println(result);
            System.out.println("Lấy xe và đi về: ");
        } catch (Exception e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }
}
