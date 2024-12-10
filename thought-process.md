### Thought Process for Verve Technical Assignemnt

#### **Problem Statement**
The goal is to build a highly performant REST service capable of handling at least 10,000 requests per second. The service will:
- Accept requests at a `/api/verve/accept` endpoint.
- Log the count of unique requests based on the `id` query parameter every minute.
- Optionally, send the count to a specified endpoint via an HTTP request.

Three extensions were also requested:
1. Use POST instead of GET for the external HTTP request.
2. Ensure deduplication works with multiple service instances behind a load balancer.
3. Replace logging with publishing to a distributed streaming service.

---

### Design Considerations

#### **Performance**
- **Asynchronous Processing**: Leverage non-blocking libraries (e.g., Spring WebFlux, CompletableFuture).
- **In-Memory Data Structures**: Use a thread-safe structure like `ConcurrentHashMap` for storing unique IDs.
- **Rate Limiting**: No explicit rate limiting to support high request throughput.

#### **Scalability**
- Implement distributed deduplication using Redis for a single source of truth.
- Support deployment behind a load balancer, ensuring all instances synchronize unique request IDs.

#### **Reliability**
- Use robust logging frameworks (e.g., SLF4J) with fallback mechanisms for external communication failures.
- Include retries and exponential backoff for external HTTP requests.

#### **Extensions**
- **POST Requests**: Use JSON payloads for transmitting the count to external services.
- **Distributed Deduplication**: Use Redis to store and query unique request IDs across instances.
- **Streaming Integration**: Integrate with a distributed service like Apache Kafka for publishing metrics.

---

### **Implementation Approach**

#### **Core Service**
1. **Endpoint**: Implement a GET endpoint (`/api/verve/accept`). Parse `id` (mandatory) and `endpoint` (optional) query parameters.
2. **Deduplication**: Use a `ConcurrentHashMap` for local deduplication and Redis for distributed deduplication.
3. **Logging**: Count unique IDs every minute using a scheduled task and log the count.
4. **HTTP Requests**: If the `endpoint` parameter is provided, send the unique count as a query parameter in a GET request.

#### **Extension 1**
- Modify the external request to use POST with a JSON payload:
  ```json
  {
    "uniqueRequestCount": 123
  }
  ```

#### **Extension 2**
- Replace `ConcurrentHashMap` with Redis for deduplication:
  - Use a Redis `SET` data structure.
  - Expire keys after 1 minute to match the logging interval.

#### **Extension 3**
- Integrate with Apache Kafka to publish the count of unique IDs instead of logging.

---


#### Key Features**
- **Thread-Safe Data Structures**: Use `ConcurrentHashMap` for local deduplication.
- **Distributed Deduplication**: Redis for ensuring unique IDs across instances.
- **Asynchronous HTTP Calls**: Use WebClient for non-blocking external requests.
- **Metrics**: Publish unique request count to Kafka.

---

### **Deployment Steps**

1. **Build the Application**:
   ```bash
   mvn clean package
   ```

2. **Run the Executable Jar**:
java -jar target/verve-0.0.1-SNAPSHOT.jar

3. **Validate**:
   - Test deduplication via Redis CLI.
   - Verify Kafka messages using a consumer CLI.

4. **Use Postman or cURL to validate the Requests**:
curl -X GET 'http://localhost:8080/api/verve/accept?id=1'
{
    "message": "ok"
}

curl -X GET 'http://localhost:8080/api/verve/accept?id=1&endpoint=http://example.com&uniqueRequestCount=123'
{
    "message": "ok"
}

---

### **Conclusion**
This implementation balances high throughput with scalability and reliability. The extensions ensure the service can operate effectively in distributed environments while supporting integration with modern streaming platforms like Kafka.

