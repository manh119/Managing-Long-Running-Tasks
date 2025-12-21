package com.example.demo.dto;

import lombok.Data;
import com.example.demo.service.workflow.DataSourceConfig;
import com.example.demo.service.workflow.EmailConfig;
import com.example.demo.service.workflow.PdfOptions;

// ReportRequest.java
@Data
public class ReportRequest {
    private DataSourceConfig dataSource;
    private PdfOptions pdfOptions;
    private EmailConfig emailConfig;
}
