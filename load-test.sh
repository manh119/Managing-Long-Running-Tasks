# Load Testing Script (Gatling/JMeter equivalent in shell)
#!/bin/bash
# load-test.sh

BASE_URL="http://localhost:8081/api/v1/jobs"
CONCURRENT_REQUESTS=100
TOTAL_REQUESTS=10000

echo "Starting load test..."
echo "Concurrent: $CONCURRENT_REQUESTS"
echo "Total: $TOTAL_REQUESTS"

# Function to submit a job
submit_job() {
    local id=$1
    local key="load-test-$id-$(date +%s)"

    curl -s -X POST "$BASE_URL/image-process" \
        -H "Content-Type: application/json" \
        -H "X-Idempotency-Key: $key" \
        -d '{
            "imageUrl": "https://example.com/image-'$id'.jpg",
            "outputFormat": "png",
            "operations": ["resize", "compress"]
        }' > /dev/null

    echo "Submitted job $id"
}

export -f submit_job
export BASE_URL

# Run concurrent requests
seq 1 $TOTAL_REQUESTS | xargs -P $CONCURRENT_REQUESTS -I {} bash -c 'submit_job {}'

echo "Load test completed!"
echo "Check Kafka UI at http://localhost:8080"
echo "Check Grafana at http://localhost:3000"