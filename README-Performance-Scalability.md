# 🚀 Production, Performance & Scalability

This document provides an overview of the production readiness, performance optimizations, and scalability planning implemented in the project.  
Each section links to a dedicated resource for more details.

---

## 1. CI/CD Pipeline
- Automated **GitHub Actions** workflow for continuous integration.
- Runs on every push and pull request.
- Steps include:
    - Code checkout
    - Java & Gradle setup
    - Build and test execution
- Ensures the main branch always remains stable.
- Real-time status can be viewed directly in GitHub Actions.

🔗 [GitHub Actions — CI/CD Workflows](https://github.com/deniz-oezdemir/Spaeti-SnackEnd/actions)

---

## 2. Structured Logging
- **Structured JSON logging** with clear log levels (INFO, WARN, ERROR).
- **MDC (`requestId`)** included in every log line for end-to-end request tracing.
- **Service Tracing Aspect** is enabled in **debug mode only**, allowing detailed tracing of service calls without adding overhead in production.
- Sensitive data (PII) excluded or masked.
- Logs stored in:
    - Local environment → `build/logs/`
    - Production server → `/var/log/spaeti/`
- Logs are also **integrated with AWS CloudWatch**, enabling centralized monitoring and analysis across environments.
- Consistent global exception handling ensures clean and predictable log outputs.

🔗 [README-Logging-Strategy.md](https://github.com/deniz-oezdemir/Spaeti-SnackEnd/blob/main/README-Logging-Strategy.md)

---

## 3. Concurrency Handling
- Prevents data conflicts when multiple users update the same resource simultaneously.
- **Optimistic Locking** → `@Version` prevents lost updates.
- **Pessimistic Locking** → `PESSIMISTIC_WRITE` in checkout ensures only one buyer decrements stock.
- **Error Handling** → conflicts return `HTTP 409 Conflict`.
- **Testing** → verified prevention of overselling and stale writes.

🔗 [README-Concurrency-Handling.md](https://github.com/deniz-oezdemir/Spaeti-SnackEnd/blob/main/README-Concurrency-Handling.md)

---

## 4. Event-Driven Gift Flow
- Implemented using an **event-driven architecture**.
- Decouples services by publishing and consuming events.
- Flow example:
    - `GiftRequested` event published after checkout.
    - Event handlers process the gift request.
    - Follow-up events (`GiftProcessed`, `GiftFailed`) published for downstream consumers.
- Improves scalability, resilience, and extensibility of the gift order process.

🔗 [README-Event-Driven-Gift-Flow.md](https://github.com/deniz-oezdemir/Spaeti-SnackEnd/blob/main/README-Event-Driven-Gift-Flow.md)

---

## 5. Scalability Research & Planning

### ✅ Implemented
- **Concurrency control** with optimistic and pessimistic locking to prevent overselling and lost updates.
- **Global error handling** for concurrency conflicts (`409 Conflict`).
- **Testing strategy** to validate concurrent user scenarios under load.

### 🔮 Planned / Future Improvements
- **Atomic SQL updates** to handle high-throughput cases such as flash sales or batch operations.
- **Further optimization** with caching strategies and batched writes for performance under heavy load.
- **Horizontal scaling roadmap** (services and database) based on expected traffic growth.

🔗 [README-Research-Performance-Optimization-&-Scalability-Planning.md](https://github.com/deniz-oezdemir/Spaeti-SnackEnd/blob/main/README-Research-Performance-Optimization-&-Scalability-Planning.md)

---

## 6. Summary
- **CI/CD pipeline** ensures stability and quick feedback.
- **Structured logging** improves observability and debugging, integrated with **AWS CloudWatch** and enhanced by **Service Tracing Aspect** in debug mode.
- **Concurrency handling** prevents lost updates and overselling.
- **Event-driven architecture** enhances scalability and decoupling.
- **Research & planning** distinguish between what has been implemented today and what is prepared for future scaling.  
