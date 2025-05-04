package com.virtualthreads.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Service
public class LoadTestService {
    private static final Logger logger = LoggerFactory.getLogger(LoadTestService.class);
    
    private final RestTemplate restTemplate;
    
    public LoadTestService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }
    
    public Map<String, Object> runPlatformThreadsLoadTest(int concurrentUsers, int apiCount, int delayMs) {
        String url = String.format("http://localhost:8080/api/platform-threads?apiCount=%d&delayMs=%d", apiCount, delayMs);
        return runLoadTest("Platform Threads", url, concurrentUsers, Executors.newFixedThreadPool(200));
    }
    
    public Map<String, Object> runVirtualThreadsLoadTest(int concurrentUsers, int apiCount, int delayMs) {
        String url = String.format("http://localhost:8080/api/virtual-threads?apiCount=%d&delayMs=%d", apiCount, delayMs);
        return runLoadTest("Virtual Threads", url, concurrentUsers, Executors.newVirtualThreadPerTaskExecutor());
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> runLoadTest(String testName, String url, int concurrentUsers, ExecutorService executor) {
        logger.info("Starting load test: {}, URL: {}, Concurrent Users: {}", testName, url, concurrentUsers);
        
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        IntStream.range(0, concurrentUsers).forEach(i -> {
            futures.add(CompletableFuture.supplyAsync(() -> {
                long requestStart = System.currentTimeMillis();
                try {
                    restTemplate.getForObject(url, Map.class);
                    return System.currentTimeMillis() - requestStart;
                } catch (Exception e) {
                    logger.error("Error in request {}: {}", i, e.getMessage());
                    return -1L; // Mark as failed
                }
            }, executor));
        });
        
        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // Calculate statistics
        List<Long> responseTimes = new ArrayList<>();
        int failedRequests = 0;
        
        for (CompletableFuture<Long> future : futures) {
            try {
                Long responseTime = future.get(1, TimeUnit.SECONDS);
                if (responseTime > 0) {
                    responseTimes.add(responseTime);
                } else {
                    failedRequests++;
                }
            } catch (Exception e) {
                failedRequests++;
            }
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minResponseTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        
        // Shutdown the executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("Load test completed: {}, Total time: {}ms", testName, totalTime);
        
        Map<String, Object> results = new HashMap<>();
        results.put("testName", testName);
        results.put("concurrentUsers", concurrentUsers);
        results.put("totalTimeMs", totalTime);
        results.put("successfulRequests", concurrentUsers - failedRequests);
        results.put("failedRequests", failedRequests);
        results.put("avgResponseTimeMs", avgResponseTime);
        results.put("minResponseTimeMs", minResponseTime);
        results.put("maxResponseTimeMs", maxResponseTime);
        results.put("throughput", (concurrentUsers - failedRequests) * 1000.0 / totalTime);
        
        return results;
    }
    
    public Map<String, Object> compareLoadTests(int concurrentUsers, int apiCount, int delayMs) {
        Map<String, Object> platformResults = runPlatformThreadsLoadTest(concurrentUsers, apiCount, delayMs);
        Map<String, Object> virtualResults = runVirtualThreadsLoadTest(concurrentUsers, apiCount, delayMs);
        
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("platformThreadsResults", platformResults);
        comparison.put("virtualThreadsResults", virtualResults);
        comparison.put("throughputImprovement", 
            (double)virtualResults.get("throughput") - (double)platformResults.get("throughput"));
        comparison.put("throughputImprovementPercentage", 
            100.0 * ((double)virtualResults.get("throughput") - (double)platformResults.get("throughput")) / 
            (double)platformResults.get("throughput"));
            
        return comparison;
    }
}