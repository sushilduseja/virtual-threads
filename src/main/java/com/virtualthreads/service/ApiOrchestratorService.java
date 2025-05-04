package com.virtualthreads.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ApiOrchestratorService {
    private static final Logger logger = LoggerFactory.getLogger(ApiOrchestratorService.class);

    private final ExternalApiService externalApiService;

    public ApiOrchestratorService(ExternalApiService externalApiService) {
        this.externalApiService = externalApiService;
    }

    public Map<String, Object> aggregateWithPlatformThreads(int apiCount, int delayMs) {
        long startTime = System.currentTimeMillis();
        logger.info("Starting aggregation with platform threads. API count: {}, delay: {}ms", apiCount, delayMs);

        List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();

        for (int i = 0; i < apiCount; i++) {
            // Use the mock-api endpoint instead of example.com
            String apiUrl = "/mock-api/" + i;
            futures.add(externalApiService.fetchDataWithPlatformThreads(apiUrl, delayMs));
        }

        List<Map<String, Object>> results;
        try {
            results = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream()
                            .map(this::getMapFromFuture)
                            .collect(Collectors.toList()))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to aggregate API results", e);
        }

        long totalTime = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new HashMap<>();
        response.put("executionTimeMs", totalTime);
        response.put("apiCount", apiCount);
        response.put("results", results);
        response.put("threadType", "platform");

        logger.info("Completed aggregation with platform threads in {}ms", totalTime);
        return response;
    }

    public Map<String, Object> aggregateWithVirtualThreads(int apiCount, int delayMs) {
        long startTime = System.currentTimeMillis();
        logger.info("Starting aggregation with virtual threads. API count: {}, delay: {}ms", apiCount, delayMs);

        List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();

        for (int i = 0; i < apiCount; i++) {
            // Use the mock-api endpoint instead of example.com
            String apiUrl = "/mock-api/" + i;
            futures.add(externalApiService.fetchDataWithVirtualThreads(apiUrl, delayMs));
        }

        List<Map<String, Object>> results;
        try {
            results = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream()
                            .map(this::getMapFromFuture)
                            .collect(Collectors.toList()))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to aggregate API results", e);
        }

        long totalTime = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new HashMap<>();
        response.put("executionTimeMs", totalTime);
        response.put("apiCount", apiCount);
        response.put("results", results);
        response.put("threadType", "virtual");

        logger.info("Completed aggregation with virtual threads in {}ms", totalTime);
        return response;
    }

    private Map<String, Object> getMapFromFuture(CompletableFuture<Map<String, Object>> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to get result from future", e);
        }
    }

    public Map<String, Object> runComparison(int apiCount, int delayMs) {
        Map<String, Object> platformResult = aggregateWithPlatformThreads(apiCount, delayMs);
        Map<String, Object> virtualResult = aggregateWithVirtualThreads(apiCount, delayMs);

        Map<String, Object> comparison = new HashMap<>();
        comparison.put("platformThreads", platformResult);
        comparison.put("virtualThreads", virtualResult);
        comparison.put("improvement", (long)platformResult.get("executionTimeMs") - (long)virtualResult.get("executionTimeMs"));
        comparison.put("improvementPercentage",
                100.0 * ((long)platformResult.get("executionTimeMs") - (long)virtualResult.get("executionTimeMs")) /
                        (long)platformResult.get("executionTimeMs"));

        return comparison;
    }

    // Method to test performance with increasing concurrent requests
    public Map<String, Object> scalabilityTest(int maxApiCount, int delayMs, int step) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (int count = step; count <= maxApiCount; count += step) {
            results.add(runComparison(count, delayMs));
        }

        Map<String, Object> scalabilityResults = new HashMap<>();
        scalabilityResults.put("testParameters", Map.of(
                "maxApiCount", maxApiCount,
                "delayMs", delayMs,
                "step", step
        ));
        scalabilityResults.put("results", results);

        return scalabilityResults;
    }
}