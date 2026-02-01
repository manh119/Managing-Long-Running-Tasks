package com.example.demo.service.templateDesignPattern;

public abstract class OrderProcessor {
    // Template Method: Cố định các bước thực hiện
    // mặc dù có thể dùng override thuần túy,
    // ko cần template design pattern nhưng dùng final ở đây giúp ko bị override bước quan trọng
    public final void processOrder() {
        step1_Validate();
        step2_EnrichData();
        step3_ApplyDiscount();
        step4_HandlePayment();
        step5_Notify();
    }

    // Các bước mặc định (Default implementation)
    protected void step1_Validate() { System.out.println("V1: Standard Validation"); }
    protected void step2_EnrichData() { System.out.println("V1: Standard Enrichment"); }
    protected void step3_ApplyDiscount() { System.out.println("V1: No Discount"); }
    protected void step4_HandlePayment() { System.out.println("V1: Credit Card Payment"); }
    protected void step5_Notify() { System.out.println("V1: Email Notification"); }
}