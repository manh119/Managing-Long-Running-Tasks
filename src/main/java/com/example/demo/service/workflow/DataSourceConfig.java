package com.example.demo.service.workflow;

import lombok.Data;

import java.util.Map;

@Data
public class DataSourceConfig {
    private String query;
    private String database;
    private Map<String, Object> parameters;
}
