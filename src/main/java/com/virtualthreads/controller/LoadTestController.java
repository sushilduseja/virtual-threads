package com.virtualthreads.controller;

import com.virtualthreads.service.LoadTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/load-test")
public class LoadTestController {

    private final LoadTestService loadTestService;
    
    public LoadTestController(LoadTestService loadTestService) {
        this.loadTestService = loadTestService;
    }
    
    @GetMapping("/platform-threads")
    public ResponseEntity<Map<String, Object>> testPlatformThreads(
            @RequestParam(defaultValue = "100") int concurrentUsers,
            @RequestParam(defaultValue = "20") int apiCount,
            @RequestParam(defaultValue = "100") int delayMs) {
        return ResponseEntity.ok(loadTestService.runPlatformThreadsLoadTest(concurrentUsers, apiCount, delayMs));
    }
    
    @GetMapping("/virtual-threads")
    public ResponseEntity<Map<String, Object>> testVirtualThreads(
            @RequestParam(defaultValue = "100") int concurrentUsers,
            @RequestParam(defaultValue = "20") int apiCount,
            @RequestParam(defaultValue = "100") int delayMs) {
        return ResponseEntity.ok(loadTestService.runVirtualThreadsLoadTest(concurrentUsers, apiCount, delayMs));
    }
    
    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareLoadTests(
            @RequestParam(defaultValue = "100") int concurrentUsers,
            @RequestParam(defaultValue = "20") int apiCount,
            @RequestParam(defaultValue = "100") int delayMs) {
        return ResponseEntity.ok(loadTestService.compareLoadTests(concurrentUsers, apiCount, delayMs));
    }
}