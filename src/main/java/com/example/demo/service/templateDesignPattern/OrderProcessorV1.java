package com.example.demo.service.templateDesignPattern;

import org.springframework.stereotype.Component;

// Version 2: Chỉ thay đổi bước 3
@Component
public class OrderProcessorV1 extends OrderProcessor {

    @Override
    protected void step1_Validate() {
        System.out.println("implement version 1 : step 1");
    }

    @Override
    protected void step2_EnrichData() {
        System.out.println("implement version 1 : step 2");
    }

    @Override
    protected void step4_HandlePayment() {
        System.out.println("impl version 1 : step 4");
    }

    @Override
    protected void step3_ApplyDiscount() {
        System.out.println("impl version 1: step 3");
    }

    @Override
    protected void step5_Notify() {
        System.out.println("impl version 1: step 5");
    }

    public static void main(String[] args) {
        OrderProcessorV1 processorV1 = new OrderProcessorV1();
        processorV1.processOrder();
    }
}