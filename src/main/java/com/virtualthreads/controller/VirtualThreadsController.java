package com.virtualthreads.controller;

import com.virtualthreads.service.ApiOrchestratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class VirtualThreadsController {

    private final ApiOrchestratorService orchestratorService;

    public VirtualThreadsController(ApiOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @GetMapping("/platform-threads")
    public ResponseEntity<Map<String, Object>> testPlatformThreads(
            @RequestParam(defaultValue = "50") int apiCount,
            @RequestParam(defaultValue = "200") int delayMs) {
        return ResponseEntity.ok(orchestratorService.aggregateWithPlatformThreads(apiCount, delayMs));
    }

    @GetMapping("/virtual-threads")
    public ResponseEntity<Map<String, Object>> testVirtualThreads(
            @RequestParam(defaultValue = "50") int apiCount,
            @RequestParam(defaultValue = "200") int delayMs) {
        return ResponseEntity.ok(orchestratorService.aggregateWithVirtualThreads(apiCount, delayMs));
    }

    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareThreads(
            @RequestParam(defaultValue = "50") int apiCount,
            @RequestParam(defaultValue = "200") int delayMs) {
        return ResponseEntity.ok(orchestratorService.runComparison(apiCount, delayMs));
    }

    @GetMapping("/scalability-test")
    public ResponseEntity<Map<String, Object>> scalabilityTest(
            @RequestParam(defaultValue = "1000") int maxApiCount,
            @RequestParam(defaultValue = "100") int delayMs,
            @RequestParam(defaultValue = "100") int step) {
        return ResponseEntity.ok(orchestratorService.scalabilityTest(maxApiCount, delayMs, step));
    }

    @GetMapping("/thread-info")
    public ResponseEntity<Map<String, Object>> getThreadInfo() {
        Map<String, Object> info = Map.of(
                "thread", Thread.currentThread().toString(),
                "isVirtual", Thread.currentThread().isVirtual()
        );
        return ResponseEntity.ok(info);
    }
}