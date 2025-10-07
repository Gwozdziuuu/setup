# RabbitMQ Message Processing Example

## Overview

This project contains a complete example of RabbitMQ message processing using functional error handling with Vavr's `Either` type.

## Example: Order Processing

The `handleOrderMessage` listener in `MessageListener.java` demonstrates the recommended pattern for processing messages:

### Flow

1. **Parse JSON** → Convert message string to `OrderRequest` DTO
2. **Validate** → Check business rules (amount > 0, required fields, etc.)
3. **Check duplicates** → Prevent duplicate order processing
4. **Save to database** → Persist order with PENDING status
5. **Call external API** → Process payment via external service
6. **Update status** → Mark order as COMPLETED

### Key Features

#### ✅ Functional Error Handling

```java
@RabbitListener(queues = "#{rabbitMQProperties.order.name}")
@Transactional  // DB rollback on exception
public void handleOrderMessage(String message) {
    processOrderMessage(message)
        .getOrElseThrow(MessageProcessingException::new);
}

private Either<Failure, Success<Void>> processOrderMessage(String message) {
    return parseOrder(message)
        .flatMap(this::validateOrder)
        .flatMap(this::checkDuplicateOrder)
        .flatMap(this::saveOrder)
        .flatMap(this::callExternalPaymentApi)
        .flatMap(this::updateOrderStatus)
        .peekLeft(failure -> {
            // Different reactions based on error type
            switch (failure.code()) {
                case VALIDATION -> log.error("Will go to DLQ: {}", failure.message());
                case TIMEOUT -> log.warn("Will retry: {}", failure.message());
                // ...
            }
        });
}
```

#### ✅ Automatic Retry with Exponential Backoff

Configured in `RabbitMQConfig.java`:

```java
@Bean
public Advice retryInterceptor(MessageRecoverer messageRecoverer) {
    return RetryInterceptorBuilder.stateless()
        .maxAttempts(3)                    // 3 attempts total
        .backOffOptions(1000, 2.0, 10000)  // 1s → 2s → 4s
        .recoverer(messageRecoverer)       // Send to DLQ after 3 failures
        .build();
}
```

#### ✅ Dead Letter Queue (DLQ)

After 3 failed attempts, messages automatically go to DLQ with metadata:

```java
@Bean
public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
    return (message, cause) -> {
        message.getMessageProperties().getHeaders().put("x-retry-attempts", 3);
        message.getMessageProperties().getHeaders().put("x-error", cause.getMessage());

        rabbitTemplate.send(
            rabbitMQProperties.getExchangeName() + ".dlx",
            queueName + ".dlq",
            message
        );
    };
}
```

#### ✅ Database Transaction Management

`@Transactional` on listener ensures DB rollback when processing fails:

| Scenario | DB Transaction | RabbitMQ | Result |
|----------|---------------|----------|---------|
| Success | ✅ COMMIT | ✅ ACK | Order saved and completed |
| Validation error | ❌ ROLLBACK | ❌ NACK → DLQ | No data in DB, message in DLQ |
| API timeout | ❌ ROLLBACK | ❌ NACK → retry | Retry with backoff |
| Restart during processing | ❌ ROLLBACK | ⏮️ Message requeued | Safe reprocessing |

#### ✅ Restart Safety

```java
factory.setPrefetchCount(1);  // Only 1 message in-flight per consumer
```

On application restart:
- Messages **not yet ACKed** return to queue automatically
- Maximum 1 message per consumer is lost to reprocessing
- No manual ACK/NACK needed

## Error Handling Strategy

Different error types trigger different behaviors:

### VALIDATION Errors
- **Example**: Missing fields, invalid amount, bad JSON
- **Action**: Log error → Goes to DLQ (no retry)
- **Reason**: Retrying won't fix malformed data

### TIMEOUT Errors
- **Example**: External API timeout
- **Action**: Log warning → Retry 3 times with backoff → DLQ
- **Reason**: Temporary issue, might succeed on retry

### CONFLICT Errors
- **Example**: Duplicate order ID
- **Action**: Log warning → Goes to DLQ (no retry)
- **Reason**: Idempotency check - order already processed

### UNAVAILABLE Errors
- **Example**: External service down
- **Action**: Log error → Retry 3 times → DLQ
- **Reason**: Service might come back up

## Testing the Example

### 1. Start infrastructure

```bash
docker-compose -f services/docker-compose.yml up -d
```

### 2. Run the application

```bash
mvn spring-boot:run
```

### 3. Send a valid order message

```bash
curl -X POST http://localhost:8080/setup/api/rabbitmq/send/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001",
    "customerId": "CUST-123",
    "amount": 99.99,
    "productCode": "PROD-ABC"
  }'
```

### 4. Send an invalid message (goes to DLQ)

```bash
curl -X POST http://localhost:8080/setup/api/rabbitmq/send/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-002",
    "customerId": "CUST-456",
    "amount": -50.00,
    "productCode": "PROD-XYZ"
  }'
```

Amount is negative → Validation fails → Goes to DLQ immediately.

### 5. Check logs

```
║ ORDER LISTENER - Message received from queue: orders
║ [ORDER] Step 1/5: Parsing JSON message
║ [ORDER] Step 2/5: Validating business rules
║ [ORDER] Validation failed - message will go to DLQ: Amount must be greater than zero
```

### 6. Monitor RabbitMQ

Visit http://localhost:15672 (guest/guest)
- Check `orders` queue
- Check `orders.dlq` (dead letter queue)
- View message details with error metadata

## Configuration

### Queue Configuration

Queues, exchanges, and bindings are defined in `services/rabbitmq/definitions.json` and loaded automatically on RabbitMQ startup.

### Concurrency

Configured per queue in `application.yml`:

```yaml
rabbitmq:
  listener:
    order-concurrency: 2-5      # 2 consumers, scale to 5
    notification-concurrency: 5-10
    audit-concurrency: 1-3
```

### Retry Policy

Configured in `RabbitMQConfig.java`:

```java
.maxAttempts(3)                    // Total attempts
.backOffOptions(1000, 2.0, 10000)  // Initial delay, multiplier, max delay
```

## Architecture Decisions

### ✅ Why AUTO acknowledge mode?

- Simple, declarative code
- Automatic retry with exponential backoff
- Automatic DLQ routing
- Restart safe with `prefetchCount=1`
- No manual ACK/NACK boilerplate

### ❌ Why NOT MANUAL acknowledge mode?

- Requires manual `channel.basicAck()` in every listener
- Must implement retry logic yourself
- Must implement DLQ routing yourself
- Easy to forget ACK = memory leak
- More complex, error-prone code

### ❌ Why NOT `setChannelTransacted(true)`?

- Creates separate RabbitMQ transaction (not synchronized with DB)
- No atomicity between DB and messaging
- More complex without real benefits
- Conflicts with retry interceptor

### ✅ Why Either + `@Transactional`?

- Clean separation of concerns
- DB rollback on exception (automatic)
- Functional error handling (no try/catch boilerplate)
- Type-safe error propagation
- Each step can fail independently
- Eventually consistent (good enough for 99% of use cases)

## When to Use a Different Approach

If you need **absolute atomicity** between DB and RabbitMQ:

### Outbox Pattern

1. Save message to `outbox` table in same DB transaction
2. Separate worker polls `outbox` table
3. Worker sends to RabbitMQ
4. Mark message as sent

**Guarantees**: Either both DB + RabbitMQ succeed, or neither.

**Tradeoff**: More complexity, eventual delivery (not immediate).

## Summary

This example demonstrates:

✅ Functional error handling with `Either`
✅ Automatic retry with exponential backoff
✅ Dead letter queue for failed messages
✅ Database transaction rollback on failure
✅ Restart safety with prefetch
✅ Different error handling strategies per error type
✅ Clean, maintainable code without boilerplate

Perfect for 99% of message processing scenarios!