package com.example.demo.service.templateDesignPattern;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class templateDesignPatternController {
    @GetMapping("/v1/process")
    public ResponseEntity<String> processOrder() {
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }
}
