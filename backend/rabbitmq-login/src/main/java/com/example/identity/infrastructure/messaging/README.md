# RabbitMQ Login Queue Implementation

This module implements RabbitMQ for handling high concurrent login traffic in the identity service.

## Overview

The implementation uses RabbitMQ to queue and process login requests asynchronously, which helps:
- Handle high concurrent login traffic
- Prevent service overload during peak times
- Implement priority-based processing
- Retry failed requests automatically
- Track and manage login request status

## Architecture

### Components

1. **RabbitMQConfiguration** (`configuration/RabbitMQConfiguration.java`)
   - Defines the exchange, queues, and bindings
   - Configures the connection factory and listener container
   - Sets up dead letter queue for failed requests
   - Configures retry queue with delayed processing

2. **LoginMessageSender** (`infrastructure/messaging/LoginMessageSender.java`)
   - Sends login requests to RabbitMQ
   - Supports priority-based messaging
   - Handles retry queue messaging

3. **LoginMessageListener** (`infrastructure/messaging/LoginMessageListener.java`)
   - Listens for login requests on the queue
   - Processes requests asynchronously using thread pool
   - Implements retry logic for failed requests
   - Handles dead letter queue messages

4. **LoginMessageService** (`application/LoginMessageService.java`)
   - Business logic interface for sending login messages
   - Provides methods for different priority levels

5. **AsyncLoginController** (`presentation/controller/AsyncLoginController.java`)
   - REST endpoint for async login requests
   - Provides endpoints for:
     - `/api/async-account/login` - Queue login request
     - `/api/async-account/login-priority` - Queue priority login request
     - `/api/async-account/login-status` - Check login request status

6. **LoginResultCache** (`infrastructure/cache/LoginResultCache.java`)
   - In-memory cache for storing login results
   - Allows clients to poll for their login status
   - Supports TTL-based expiration

7. **Message DTOs** (`presentation/record/`)
   - `LoginRequestMessage` - Login request DTO
   - `LoginResponseMessage` - Login response DTO

## Configuration

Add the following to your `application.properties`:

```properties
# RabbitMQ Connection
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USERNAME:guest}
spring.rabbitmq.password=${RABBITMQ_PASSWORD}

# Login Queue Configuration
rabbitmq.login.prefetch.count=10
rabbitmq.login.concurrent.consumers=5
rabbitmq.login.max.concurrent.consumers=10
rabbitmq.login.queue.capacity=1000

# Cache Configuration
login.result.cache.ttl.seconds=300
```

## Usage

### Sending a Login Request

```java
// Basic login request
String requestId = loginMessageService.sendLoginRequest(email, password);

// With IP and User-Agent
String requestId = loginMessageService.sendLoginRequest(email, password, ipAddress, userAgent);

// With priority (0-10, where 10 is highest)
String requestId = loginMessageService.sendLoginRequestWithPriority(email, password, 8);
```

### REST API

**Queue a login request:**
```bash
POST /api/async-account/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
HTTP 202 Accepted
{
  "requestId": "uuid-here",
  "message": "Login request queued successfully"
}
```

**Queue a priority login request:**
```bash
POST /api/async-account/login-priority?priority=8
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Check login status:**
```bash
GET /api/async-account/login-status?requestId=uuid-here

Response:
{
  "requestId": "uuid-here",
  "status": "PROCESSING|COMPLETED|FAILED",
  "token": "jwt-token-if-completed",
  "errorMessage": "error-message-if-failed"
}
```

## Exchange and Queue Setup

### Exchanges
- `identity.exchange` - Main direct exchange for identity module
- `identity.login.deadletter.exchange` - Dead letter exchange for failed login attempts

### Queues
- `identity.login.queue` - Main login request queue with priority support
- `identity.login.deadletter.queue` - Dead letter queue for permanently failed requests
- `identity.login.retry.queue` - Retry queue with TTL for delayed retry

### Routing Keys
- `identity.login.routingkey` - Main routing key for login requests
- `identity.login.routingkey.retry` - Routing key for retry queue

## Retry Logic

- Failed requests are automatically retried up to 3 times
- Retry delay increases with each attempt:
  - First retry: 1 second
  - Second retry: 2 seconds
  - Third retry: 4 seconds
- After max retries, requests are sent to dead letter queue

## Priority Handling

- Messages can have priority from 0-10
- Higher priority messages are processed first
- Use `sendLoginRequestWithPriority()` to set custom priority
- Default priority is 5

## Monitoring

The implementation includes logging at various levels:
- INFO: Processing start/end, successful logins
- WARN: Invalid credentials, account not active
- ERROR: Unexpected errors, max retries exceeded

## Scaling

To handle higher concurrent traffic:
1. Increase `rabbitmq.login.concurrent.consumers`
2. Increase `rabbitmq.login.max.concurrent.consumers`
3. Adjust `rabbitmq.login.prefetch.count` based on processing time
4. Add more consumer instances (scale horizontally)

## Dependencies

Ensure your `pom.xml` includes:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.amqp</groupId>
    <artifactId>spring-rabbit-stream</artifactId>
</dependency>
```
