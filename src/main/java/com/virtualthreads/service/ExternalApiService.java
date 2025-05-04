package com.virtualthreads.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ExternalApiService {
    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);

    private final WebClient webClient;
    private final ExecutorService platformThreadPool;

    // Host and port for our local service
    private final String baseUrl = "http://localhost:8080";

    public ExternalApiService(WebClient webClient) {
        this.webClient = webClient;
        // Traditional fixed thread pool with 200 threads
        this.platformThreadPool = Executors.newFixedThreadPool(200);
    }

    // Method using platform threads
    public CompletableFuture<Map<String, Object>> fetchDataWithPlatformThreads(String apiUrl, int delayMs) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            String fullUrl = baseUrl + "/mock-api/" + extractApiId(apiUrl) + "?delayMs=" + delayMs;
            logger.info("Starting platform thread request to {} with delay {}ms", fullUrl, delayMs);

            try {
                // Make actual HTTP request to our mock service
                Map<String, Object> response = webClient.get()
                        .uri(fullUrl)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                // Add additional thread info
                Map<String, Object> enhancedResponse = Map.of(
                        "url", fullUrl,
                        "data", response,
                        "threadInfo", Thread.currentThread().toString(),
                        "isVirtual", Thread.currentThread().isVirtual(),
                        "delay", delayMs
                );

                long endTime = System.currentTimeMillis();
                logger.info("Completed platform thread request to {} in {}ms", fullUrl, (endTime - startTime));

                return enhancedResponse;
            } catch (Exception e) {
                logger.error("Error making request to {}: {}", fullUrl, e.getMessage());
                throw new RuntimeException("Request failed", e);
            }
        }, platformThreadPool);
    }

    // Method using virtual threads
    public CompletableFuture<Map<String, Object>> fetchDataWithVirtualThreads(String apiUrl, int delayMs) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            String fullUrl = baseUrl + "/mock-api/" + extractApiId(apiUrl) + "?delayMs=" + delayMs;
            logger.info("Starting virtual thread request to {} with delay {}ms", fullUrl, delayMs);

            try {
                // Make actual HTTP request to our mock service
                Map<String, Object> response = webClient.get()
                        .uri(fullUrl)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                // Add additional thread info
                Map<String, Object> enhancedResponse = Map.of(
                        "url", fullUrl,
                        "data", response,
                        "threadInfo", Thread.currentThread().toString(),
                        "isVirtual", Thread.currentThread().isVirtual(),
                        "delay", delayMs
                );

                long endTime = System.currentTimeMillis();
                logger.info("Completed virtual thread request to {} in {}ms", fullUrl, (endTime - startTime));

                return enhancedResponse;
            } catch (Exception e) {
                logger.error("Error making request to {}: {}", fullUrl, e.getMessage());
                throw new RuntimeException("Request failed", e);
            }
        }, Executors.newVirtualThreadPerTaskExecutor());
    }

    // Helper method to extract API ID from the URL
    private String extractApiId(String apiUrl) {
        // Extract the last part of the URL as the ID
        String[] parts = apiUrl.split("/");
        return parts[parts.length - 1];
    }
}