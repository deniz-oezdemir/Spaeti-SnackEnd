# Logging Strategy — Spaeti SnackEnd

This document explains the logging strategy implemented in the project:

- what we log
- where we log it from
- how to configure it per environment
- why these choices were made <br>

It also shows how to enable request correlation (MDC), structured service tracing via an Aspect, and consistent exception logging via a global handler.

## Goals & Principles

- <b> Right level, right place: </b> Use INFO for normal lifecycle signals, WARN for recoverable/expected problems, ERROR for unexpected/unhandled failures. Avoid noisy DEBUG in production but make it one-switch away for troubleshooting.<br>

- <b> Traceability: </b> Every log line carries a requestId (MDC) so we can correlate logs across filters, controllers, services, and repositories.<br>

- <b> Safety: </b> Avoid logging PII/secrets. The service tracing aspect masks emails and truncates long payloads.<br>

- <b> Low overhead: </b> Tracing is opt‑in via logging.tracing.service.enabled. When off, the aspect isn’t active.<br>


## Key Components

### 1) MDC Filter

- MdcFilter assigns a requestId from X-Request-ID header or generates one.

- %X{requestId} added to console pattern for correlation.

### 2) Global Exception Handler

Centralized handling with consistent logs + responses:

- NoSuchElementException → WARN + 404

- ValidationException / MethodArgumentNotValidException → WARN + 400

- AuthorizationException → WARN + 401

- General Exception → ERROR + 500

### 3) Service Tracing Aspect (Optional)

- Logs service method start/done/fail with timing.

- Enabled only when logging.tracing.service.enabled=true and DEBUG level.

- Masks emails, truncates long arguments.

## Configuration

```
File: src/main/resources/application.properties

# Import local secrets (EC2 path); optional if not present
spring.config.import=optional:file:/home/ubuntu/secrets/application-secrets.properties

# Log levels (defaults)
logging.level.root=INFO
logging.level.ecommerce=INFO
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=WARN

# Add requestId (MDC) to every console line
logging.pattern.console=%d{ISO8601} %-5level [%X{requestId:-}] %logger{36} - %msg%n

# AOP service tracing aspect is off by default
logging.tracing.service.enabled=false 

```
- Local debug: set logging.level.ecommerce=DEBUG and enable tracing.

- Production: keep tracing off, use INFO/WARN/ERROR.

## Sample Log Lines in Console

### Normal request

```
2025-08-21T12:34:56.789 INFO  [c1a2b3c4-...] ecommerce.api.OrderController - Placing order
2025-08-21T12:34:56.792 DEBUG [c1a2b3c4-...] ecommerce.service.OrderService - svc_start sig=OrderService.place args=[memberId=42, req=PlaceOrderRequest(...)]
2025-08-21T12:34:56.945 DEBUG [c1a2b3c4-...] ecommerce.service.OrderService - svc_done  sig=OrderService.place took=153ms
```
### Validation failure

```
2025-08-21T12:35:10.101 WARN  [c1a2b3c4-...] e.handler.GlobalExceptionHandler - Validation error: Validation failed for argument [...]
```
### Unhandled failure

```
2025-08-21T12:35:20.222 ERROR [c1a2b3c4-...] e.handler.GlobalExceptionHandler - Unhandled exception: Connection refused

```

## How to Verify Locally

- <b> Run with tracing (optional): </b>

    ` ./gradlew bootRun --args='--logging.tracing.service.enabled=true --logging.level.ecommerce=DEBUG' `

- <b> Send a request from Postman: </b>

    - Add header: X-Request-ID: 11111111-1111-1111-1111-111111111111

    - Observe the same id in all produced logs.

- <b> Trigger validations </b> to see 400 with field errors and a WARN log.

- <b> Trigger not‑found </b> to see 404 and a WARN with stack (from NoSuchElementException).

## Rationale

- <b> Signal over noise: </b> Keep production logs clean, escalate when needed.

- <b> Traceability: </b> RequestId allows end-to-end tracing.

- <b> Optional deep dive: </b> Turn on service tracing for debugging.

- <b> Consistency: </b> Global handler ensures uniform error responses.

## File Logging (Local Development)

In addition to console logs, we also write logs to rolling files for easier debugging and persistence. We use Logback (Spring Boot default).

### Setup 

1. We added a `logback-spring.xml` under `src/main/resources/`.

It defines:

  - Console appender (kept for dev)

  - Rolling appender for all logs (daily + size-based rotation)

  - Rolling appender for error-only logs

  - A root logger that writes to console + files

The file uses `${APP_LOG_FILE}` and `${ERROR_LOG_FILE}` placeholders to decide the log file paths.

2. Add file path overrides in `application.properties` :

```

# file paths used by logback-spring.xml
APP_LOG_FILE=build/logs/app.log
ERROR_LOG_FILE=build/logs/error.log

```

### Run and Verify

1. Run the following command in the project terminal: 

`./gradlew bootRun`

2. Check generated files: 

```
build/logs/app.log
build/logs/error.log
```

### Benefits 

- <b> Rolling policy: </b> Logs rotate daily and on size limit (50MB for app logs, 20MB for error logs).

- <b> Retention: </b> 14 days of history, compressed to save space.

- <b> Separation: </b> Error logs are stored separately for quick diagnosis.

- <b> Consistency: </b> Same MDC pattern with requestId as console logs.

## Viewing and Filtering Logs

When running locally or on a server, you can inspect the log files directly from the terminal:

- Follow logs in real time (like console.log):
 `tail -f build/logs/app.log` <br>
This will continuously stream new log lines as they are written.

- Filter for errors only:
  `tail -f build/logs/app.log | grep "ERROR"`

- Filter for warnings only:
 `tail -f build/logs/app.log | grep "WARN"`

- Filter for info messages only:
  `tail -f build/logs/app.log | grep "INFO"`

- Search inside any log file (replace with an actual path):
  `tail -f /path/to/your/logfile.txt | grep "ERROR"`



