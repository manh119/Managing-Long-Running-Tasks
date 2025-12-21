package com.example.demo.service.workflow;

import lombok.Data;

import java.util.List;

@Data
public class EmailConfig {
    private List<String> recipients;
    private String subject;
    private String body;
}
