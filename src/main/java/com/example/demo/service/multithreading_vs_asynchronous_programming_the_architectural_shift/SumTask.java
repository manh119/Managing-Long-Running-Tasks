package com.example.demo.service.multithreading_vs_asynchronous_programming_the_architectural_shift;

import java.util.concurrent.*;

// Task tính tổng một mảng số lớn
class SumTask extends RecursiveTask<Long> {
    private final long[] array;
    private final int start, end;
    private static final int THRESHOLD = 100; // Ngưỡng: Nếu ít hơn 100 phần tử thì tính trực tiếp

    SumTask(long[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        if (end - start <= THRESHOLD) {
            // Công việc đủ nhỏ -> Chạy tuần tự
            long sum = 0;
            for (int i = start; i < end; i++) sum += array[i];
            return sum;
        } else {
            // Công việc quá lớn -> Fork (Chia đôi)
            int mid = (start + end) / 2;
            SumTask leftTask = new SumTask(array, start, mid);
            SumTask rightTask = new SumTask(array, mid, end);

            leftTask.fork(); // Chạy task bên trái bất đồng bộ
            long rightResult = rightTask.compute(); // Chạy task bên phải ngay tại thread hiện tại
            long leftResult = leftTask.join();    // Đợi kết quả task trái và gộp lại

            return leftResult + rightResult;
        }
    }

    public static void main(String[] args) {
        long[] numbers = new long[10000]; // Mảng 10k phần tử
        ForkJoinPool pool = ForkJoinPool.commonPool();
        System.out.println("pool " + pool.getPoolSize()); // Số lượng thread trong pool (mặc định là số lượng core)
        Long totalSum = pool.invoke(new SumTask(numbers, 0, numbers.length));
        System.out.println("Tổng là: " + totalSum);
    }
}
