package com.example.demo.service.templateDesignPattern;

import org.springframework.stereotype.Component;

// Version 3: Kế thừa V2 và thay đổi bước 4, 5
@Component
public class OrderProcessorV3 extends OrderProcessorV2 {
    @Override
    protected void step4_HandlePayment() {
        System.out.println("V3: Crypto Payment");
    }

    @Override
    protected void step5_Notify() {
        System.out.println("V3: Push Notification to App");
    }
}