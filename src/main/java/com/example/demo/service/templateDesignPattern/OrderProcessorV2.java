package com.example.demo.service.templateDesignPattern;

import org.springframework.stereotype.Component;

// Version 2: Chỉ thay đổi bước 3
@Component
public class OrderProcessorV2 extends OrderProcessor {
    @Override
    protected void step3_ApplyDiscount() {
        System.out.println("impl version 2 : step 3");
    }
}

