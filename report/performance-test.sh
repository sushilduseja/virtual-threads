#!/bin/bash

echo "Starting Virtual Threads Performance Test"

# Basic test
echo "Running basic comparison test..."
curl -s "http://localhost:8080/api/compare?apiCount=50&delayMs=100" > basic_test.json

# Scalability test with increasing API counts
echo "Running scalability test..."
curl -s "http://localhost:8080/api/scalability-test?maxApiCount=500&delayMs=100&step=100" > scalability_test.json

# Load test with concurrent users
echo "Running load test..."
curl -s "http://localhost:8080/api/load-test/compare?concurrentUsers=100&apiCount=20&delayMs=100" > load_test.json

# Extreme load test
echo "Running extreme load test..."
curl -s "http://localhost:8080/api/load-test/compare?concurrentUsers=1000&apiCount=10&delayMs=50" > extreme_load_test.json

echo "Tests completed! Results saved as JSON files."