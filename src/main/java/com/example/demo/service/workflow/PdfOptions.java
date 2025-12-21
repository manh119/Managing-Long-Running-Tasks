package com.example.demo.service.workflow;

import lombok.Data;

@Data
public class PdfOptions {
    private String template;
    private String format; // A4, Letter
    private boolean includeCharts;
}
