# Virtual Threads Demo Application

This is a production-grade Spring Boot application that demonstrates the benefits of Java 21 Virtual Threads for I/O-bound workloads. The application simulates an API aggregation service that makes multiple parallel API calls - a common real-world scenario where virtual threads can significantly improve performance.

## Requirements

- Java 21 or higher
- Gradle 8.0+ (or use the Gradle wrapper included)

## How to Run

### Using Gradle

```bash
./gradlew bootRun -Djdk.tracePinnedThreads=full
```

The `-Djdk.tracePinnedThreads=full` JVM argument will help identify situations where virtual threads get "pinned" to platform threads, which can reduce their effectiveness.

## Key Components

- **VirtualThreadsApplication**: Main application class that configures Spring Boot to use virtual threads for HTTP request handling
- **ExternalApiService**: Simulates external API calls with configurable latency
- **ApiOrchestratorService**: Aggregates multiple API calls using both platform and virtual threads
- **LoadTestService**: Simulates concurrent users to test throughput and response times

## API Endpoints

### Basic Endpoint Tests

- **GET /api/thread-info**: Returns information about the current thread
- **GET /api/platform-threads?apiCount=50&delayMs=200**: Makes 50 parallel API calls using platform threads with 200ms simulated latency per call
- **GET /api/virtual-threads?apiCount=50&delayMs=200**: Makes 50 parallel API calls using virtual threads with 200ms simulated latency per call
- **GET /api/compare?apiCount=50&delayMs=200**: Runs both thread types and provides a comparison

### Load Testing Endpoints

- **GET /api/load-test/platform-threads?concurrentUsers=100&apiCount=20&delayMs=100**: Load test with platform threads
- **GET /api/load-test/virtual-threads?concurrentUsers=100&apiCount=20&delayMs=100**: Load test with virtual threads
- **GET /api/load-test/compare?concurrentUsers=100&apiCount=20&delayMs=100**: Compare load test results

### Scalability Test

- **GET /api/scalability-test?maxApiCount=1000&delayMs=100&step=100**: Tests how both thread types scale as the number of concurrent API calls increases

## Understanding the Results

The application demonstrates the benefits of virtual threads in several ways:

1. **Parallelism**: With platform threads, you're limited by the thread pool size (usually a few hundred threads at most). Virtual threads allow thousands or even millions of concurrent operations.

2. **Resource Efficiency**: Platform threads are heavyweight (approximately 1MB stack size each). Virtual threads have minimal overhead.

3. **Scalability**: As the number of concurrent operations increases, platform threads hit their limit, while virtual threads continue to scale.

4. **Latency**: For I/O-bound workloads, virtual threads typically provide lower latency under high concurrency.

## Example Test Scenarios

### Basic Comparison

1. First, try a modest workload:
   ```
   GET http://localhost:8080/api/compare?apiCount=50&delayMs=200
   ```

   You should see similar performance between platform and virtual threads.

2. Now increase the workload:
   ```
   GET http://localhost:8080/api/compare?apiCount=500&delayMs=200
   ```

   Virtual threads should now show a significant advantage.

### Scalability Test

```
GET http://localhost:8080/api/scalability-test?maxApiCount=1000&delayMs=100&step=100
```

This will run tests with increasing concurrency (100, 200, 300, etc. up to 1000), showing how the performance gap widens as concurrency increases.

### Load Test

```
GET http://localhost:8080/api/load-test/compare?concurrentUsers=200&apiCount=20&delayMs=100
```

This simulates 200