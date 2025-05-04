package com.virtualthreads.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/mock-api")
public class MockApiController {
    
    private final Random random = new Random();
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMockData(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int delayMs) {
        
        // Simulate processing time if specified
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Generate random response data
        Map<String, Object> response = Map.of(
            "id", id,
            "timestamp", LocalDateTime.now().toString(),
            "value", random.nextInt(1000),
            "threadInfo", Thread.currentThread().toString(),
            "isVirtual", Thread.currentThread().isVirtual()
        );
        
        return ResponseEntity.ok(response);
    }
}