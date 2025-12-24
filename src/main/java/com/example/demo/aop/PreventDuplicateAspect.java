package com.example.demo.aop;

import com.esotericsoftware.minlog.Log;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class PreventDuplicateAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(preventDuplicate)")
    public Object handleDuplicateMethod(ProceedingJoinPoint joinPoint, PreventDuplicateMethod preventDuplicate) throws Throwable {
        // 1. Xác định lock key (Dùng key config hoặc tên method)
        String lockKey = preventDuplicate.key().isEmpty() 
                         ? joinPoint.getSignature().toShortString() 
                         : preventDuplicate.key();
        
        RLock lock = redissonClient.getLock("LOCK:" + lockKey);
        Log.info("Thử lấy lock: {}", lockKey);

        // 2. Thử lấy lock
        // tryLock(waitTime, leaseTime, unit)
        boolean isAcquired = lock.tryLock(preventDuplicate.waitTime(), preventDuplicate.leaseTime(), TimeUnit.SECONDS);

        if (isAcquired) {
            Log.info("Lấy lock thành công: {}", lockKey);
            try {
                return joinPoint.proceed(); // Chạy nội dung method chính
            } finally {
                // 3. Giải phóng lock sau khi xong việc
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
                Log.info("Giải phóng lock: {}", lockKey);
            }
        } else {
            // Nếu không lấy được lock (job khác đang chạy)
            Log.info("Job [{}] đang chạy. Skipping...", lockKey);
            return null; 
        }
    }
}