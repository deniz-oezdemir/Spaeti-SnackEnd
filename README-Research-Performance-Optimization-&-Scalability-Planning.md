# Research: Performance Optimization & Scalability Planning

This guide outlines key areas for improving the performance, resilience, and scalability of our application. The topics are prioritized based on their immediate impact on correctness and user experience.

We have determined the below topics to be in the priority as listed (most important first):

1.  **Concurrency Handling:** Prevent data conflicts (overselling, lost updates) under concurrent access using locking and atomic updates.
2.  **Event-Driven Architecture:** Refactor features into an asynchronous, event-driven structure to improve responsiveness and resilience.
3.  **Zero-Downtime Deployment:** Implement a deployment strategy like Blue-Green to avoid service interruption during updates.
4.  **Query Optimization:** Insert large-volume test data, measure query performance, and apply indexes to speed up database reads.
5.  **Server Tuning:** Define a target TPS and adjust server/database connection pool settings to handle production load.
6.  **GraphQL Implementation:** Implement GraphQL to improve data-fetching flexibility and reduce client-server coupling.

---

## 1. Concurrency Handling

### What is it?
Concurrency handling involves mechanisms to ensure data correctness and consistency when multiple requests from different users try to update the same piece of data at the same time. Without proper handling, this can lead to issues like overselling stock or creating duplicate orders. The primary techniques are **Optimistic Locking**, **Pessimistic Locking**, and **Atomic Conditional Updates**.

### How it works
* **Optimistic Locking:** Assumes conflicts are rare. An entity is given a `@Version` number. When an update occurs, the database checks if the version number has changed since the data was read. If it has, a conflict is detected, and the transaction fails, typically requiring a retry.
* **Pessimistic Locking:** Assumes conflicts are likely. The database physically locks a data row when it's read for an update (`SELECT ... FOR UPDATE`). No other transaction can modify that row until the first one is complete. This prevents conflicts but can reduce throughput as other requests must wait.
* **Atomic Conditional Update:** A single SQL/JPQL `UPDATE` statement that includes a condition in its `WHERE` clause. The update only succeeds if the condition is met (e.g., `...WHERE stock >= ?`). This is extremely fast and doesn't require application-level locking.

### Why do it?
The checkout process, especially stock reservation, is a critical "hot spot" for concurrent requests. We must implement these strategies to:
* **Prevent Overselling:** Ensure we don't sell more items than we have in stock.
* **Ensure Idempotency:** Prevent duplicate orders if a user accidentally submits a payment request twice.
* **Maintain Data Integrity:** Avoid "lost updates" where one user's changes are overwritten by another's.

### How to apply it to our project
We can apply different strategies depending on the use case.

**A) Atomic Conditional Update (Recommended for stock)**
This is the fastest path for updating counters like inventory.
* **Repository (JPQL):**
    ```kotlin
    interface OptionRepositoryJpa : JpaRepository<Option, Long> {
        @Modifying
        @Query("""
            UPDATE Option o
            SET o.quantity = o.quantity - :qty
            WHERE o.id = :id AND o.quantity >= :qty
        """)
        fun tryReserveStock(@Param("id") id: Long, @Param("qty") qty: Long): Int
    }
    ```
* **Service:**
    ```kotlin
    @Transactional
    fun reserveItems(optionId: Long, quantity: Long) {
        val updatedRows = optionRepository.tryReserveStock(optionId, quantity)
        if (updatedRows == 0) {
            throw InsufficientStockException("Not enough stock for option ID: $optionId")
        }
    }
    ```

**B) Pessimistic Locking (For short, critical sections)**
We already use this to ensure only one writer can modify an option at a time. This is good for complex operations that cannot be done in a single atomic update.
* **Existing Repository Method:**
    ```kotlin
    interface OptionRepositoryJpa : JpaRepository<Option, Long> {
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        fun findWithLockById(id: Long): Option?
    }
    ```
* **Service Logic:**
    ```kotlin
    @Transactional
    fun reserveWithPessimisticLock(optionId: Long, quantity: Long) {
        val option = optionRepository.findWithLockById(optionId) ?: throw NotFoundException()
        // Complex logic here...
        option.decreaseQuantity(quantity)
    }
    ```
  *Note: To avoid deadlocks when locking multiple items, always lock them in a consistent order (e.g., by their ID).*

**C) Optimistic Locking (For general updates with low conflict risk)**
Ideal for entities like `Order` where status updates might happen but are unlikely to conflict.
* **Entity:**
    ```kotlin
    @Entity
    class Order(
        @Id val id: Long,
        var status: OrderStatus,
        @Version var version: Long? = null
    )
    ```
* **Service:** When updating, catch the `OptimisticLockException` and either inform the user of the conflict (HTTP 409) or retry the operation.

**D) Idempotency (For payments and orders)**
Prevent duplicate order creation by adding a unique constraint on a stable identifier from the payment provider.
* **Database Schema:**
    ```sql
    ALTER TABLE payments
    ADD CONSTRAINT uq_payment_stripe_session UNIQUE (stripe_session_id);
    ```
* **Service Logic:** Before creating a new order/payment, check if one already exists for the given `stripe_session_id`. If so, return the existing order instead of creating a new one.

---

## 2. Event-Driven Architecture

### What is it?
Event-Driven Architecture is a design pattern where loosely-coupled services communicate by producing and consuming **events**. Instead of one service directly calling another (a synchronous request), a service emits an event (e.g., `OrderPlaced`) to a central message broker. Other services that are interested in that event can subscribe to it and react accordingly, without the original service knowing who they are.



### How it works
The system is composed of three main parts:
1.  **Event Producer:** A component that creates and sends an event. In our project, the `OrderService` would be a producer when it successfully processes an order.
2.  **Message Broker / Event Bus:** A central channel that receives all events and routes them to the correct consumers. This can be a dedicated tool like RabbitMQ/Kafka or, for simpler cases, Spring's built-in `ApplicationEventPublisher`.
3.  **Event Consumer / Listener:** A component that subscribes to specific events. When it receives an event, it executes a piece of business logic. For example, a `NotificationService` would listen for an `OrderPlacedEvent` to send a confirmation email.

### Why do it?
* **Decoupling & Maintainability:** Services don't need direct knowledge of each other. We can add, remove, or change a consumer (e.g., add a new Slack notification service) without ever touching the `OrderService`.
* **Improved Responsiveness:** By offloading slow operations (like sending emails or calling external APIs) to be handled asynchronously in the background, the initial user-facing request (placing an order) becomes much faster.
* **Resilience & Fault Tolerance:** If the email service is temporarily down, the `OrderPlacedEvent` can be held and processed later when the service recovers. The core order placement process is unaffected.
* **Scalability:** Each consumer service can be scaled independently based on its specific workload.

### How to apply it to our project
The order placement flow is a perfect candidate. The core, synchronous task is processing the payment and reserving stock. The side-effects—sending confirmation emails, Slack DMs, and gift notifications—can all be handled asynchronously via events.

1.  **Enable Asynchronous Capabilities:** Add `@EnableAsync` to a configuration class.

2.  **Define an Event Class:** Create a simple data class to represent the event.
    ```kotlin
    data class OrderPlacedEvent(val orderId: Long)
    ```

3.  **Publish the Event:** Inject Spring's `ApplicationEventPublisher` into the `OrderService` and publish the event after the order transaction is successfully committed.
    ```kotlin 
    @Service
    class OrderService(private val eventPublisher: ApplicationEventPublisher /*...other deps*/) {
    
        @Transactional
        fun place(member: Member, req: PlaceOrderRequest): PlaceOrderResponse {
            // ... core logic to process payment and save the order ...
            val savedOrder = orderRepository.save()
    
            // Publish an event for other services to react to
            eventPublisher.publishEvent(OrderPlacedEvent(savedOrder.id!!))
            
            return PlaceOrderResponse()
        }
    }
    ```

4.  **Create Asynchronous Listeners:** Create methods in other services that listen for the event. The `@Async` annotation ensures they run in a separate thread, so they don't block the main order placement flow.
    ```kotlin
    @Service
    class NotificationService(private val emailService: EmailService) {
    
        @Async
        @EventListener
        fun handleOrderPlacedSendEmail(event: OrderPlacedEvent) {
            // ... logic to fetch order details by event.orderId and send email ...
        }
    }
    
    @Service
    class SlackNotificationService(private val slackService: SlackService) {
    
        @Async
        @EventListener
        fun handleOrderPlacedSendSlackDm(event: OrderPlacedEvent) {
            // ... logic to fetch order and member details and send Slack DM ...
        }
    }
    ```

---

## 3. Zero-Downtime Deployment

### What is it?
Zero-downtime deployment is a strategy for updating an application without causing any service interruption for users. It ensures continuous availability, which is critical for an e-commerce platform where downtime means lost revenue.

### How it works
The most common method is **Blue-Green Deployment**. This works by running two identical production environments, nicknamed "Blue" and "Green."
1.  **Live Traffic:** The "Blue" environment runs the current stable version of the application and handles all live user traffic, directed by a load balancer.
2.  **Deploy New Version:** The new version of the application is deployed to the idle "Green" environment. This environment can be fully tested and validated without impacting users.
3.  **Switch Traffic:** Once the "Green" environment is confirmed to be healthy, the load balancer is reconfigured to instantly switch all incoming traffic from Blue to Green. Green is now the live environment.
4.  **Standby/Decommission:** The "Blue" environment, running the old version, is kept on standby for a quick rollback if needed, or is eventually decommissioned.

### Why do it?
* **Continuous Availability:** Eliminates the need for "maintenance windows," preventing lost sales and building customer trust.
* **Low-Risk Releases:** New versions can be fully tested in a production-like environment before they are exposed to users.
* **Instant Rollback:** If a critical bug is found in the new version, rolling back is as simple as switching the load balancer back to the old environment, minimizing impact.

### How to apply it to our project
For our Spaeti Shop hosted on AWS, we can implement this strategy using core AWS services:
1.  **Setup:** Use an **Application Load Balancer (ALB)** to route traffic to an **Auto Scaling Group (ASG)** of EC2 instances. This ASG is our "Blue" environment.
2.  **Deployment:** When deploying a new version, a new ASG ("Green") is created with the updated application code.
3.  **Testing:** We can run automated health checks and integration tests against the Green environment.
4.  **Traffic Switch:** We update the ALB's listener rules to redirect 100% of the traffic from the Blue ASG to the Green ASG.
5.  **Rollback:** The Blue ASG is kept running for a period of time. If any issues arise with the Green deployment, we can instantly revert the ALB's listener rules back to the Blue ASG.

---

## 4. Query Optimization with Indexing

### What is it?
Query optimization is the process of making database queries run faster, primarily by creating **indexes**. Without an index, the database must perform a slow "full table scan" (reading every row) to find data. An index provides a direct pointer to the data's location, making lookups dramatically faster.

### How it works
An index is a separate data structure (typically a B-Tree) that stores sorted column values and pointers to the full data rows. When a query uses an indexed column in a `WHERE` or `ORDER BY` clause, the database uses this efficient structure to find the data's location instantly, avoiding a full table scan.

### Why do it?
The primary advantage is a massive performance improvement, which leads to:
* **Better User Experience:** Faster page loads and API responses.
* **Improved Scalability:** The application can handle significant growth in data without slowing down.
* **Reduced Server Load:** Less CPU and I/O usage on the database server.

### How to apply it to our project
This is a five-step process:

1.  **Generate Test Data:** Create a script to populate the database with a large volume of test data (e.g., 100k Members, 1M Orders) to simulate a production environment.
2.  **Measure Performance:** Identify slow queries using `EXPLAIN ANALYZE` in a SQL client or by enabling Hibernate statistics in `application.properties`.
3.  **Identify Columns to Index:** Find columns frequently used in `WHERE` clauses for searching or joining. Good candidates in our project are:
    * `Order(memberId)`
    * `Member(email)`
    * `CartItem(cart_id)`
4.  **Apply Indexes in JPA Entities:** Use the `@Table` and `@Index` annotations on the entity. For example, to index the `memberId` in the `Order` entity:
    ```kotlin
    // File: src/main/kotlin/ecommerce/entity/Order.kt
    
    @Entity
    @Table(name = "orders", indexes = [
        Index(name = "idx_order_memberid", columnList = "memberId")
    ])
    class Order()
    ```
5.  **Re-measure Performance:** Run the same queries from Step 2 and confirm a significant reduction in execution time.

---

## 5. Server Tuning

### What is it?
Server tuning is the process of configuring our application server and database connection pool to handle a target amount of traffic, measured in **Transactions Per Second (TPS)**, efficiently. It's about balancing resources to maximize performance without waste.

### How it works
We primarily adjust two resource pools:

1.  **Web Server Thread Pool (`server.tomcat.threads.max`):** This controls the number of concurrent HTTP requests the server can handle. If all threads are busy, new requests must wait.
2.  **Database Connection Pool (`spring.datasource.hikari.maximum-pool-size`):** A pool of ready-to-use database connections. Borrowing a connection from the pool is much faster than creating a new one for each query.

### Why do it?
The default Spring Boot settings are not optimized for production traffic. Tuning provides:
* **Stability:** Prevents the server from crashing or becoming unresponsive under high load.
* **Performance:** Ensures the application can meet its target TPS.
* **Resource Efficiency:** Avoids wasting CPU and memory on idle threads or connections.

### How to apply it to our project
Tuning is an iterative cycle of testing and adjusting.

1.  **Define a Target TPS:** Set a business goal (e.g., "the system must handle 150 checkouts per second").
2.  **Conduct Load Testing:** Use tools like **JMeter**, **Gatling**, or **k6** to simulate realistic user traffic against the server.
3.  **Monitor Server Resources:** While the test runs, monitor CPU, memory, thread usage, and connection pool activity to find the bottleneck (the resource that maxes out first).
4.  **Tune Settings:** Adjust the configuration in `application.properties` based on your findings.
    ```properties
    # Server thread pool settings
    server.tomcat.threads.max=100
    server.tomcat.threads.min-spare=15

    # HikariCP database connection pool settings
    spring.datasource.hikari.maximum-pool-size=25
    spring.datasource.hikari.minimum-idle=5
    spring.datasource.hikari.connection-timeout=30000
    ```
5.  **Repeat:** Tuning is a cycle. After making an adjustment, run the load test again to measure the impact. Continue this process until the target TPS is met reliably.

---

## 6. GraphQL Implementation

### What is it?
GraphQL is a query language for APIs that allows clients to request exactly the data they need, and nothing more. Unlike REST, where the server defines the structure of each endpoint's response, GraphQL empowers the client to specify the fields it wants, often consolidating data from multiple resources into a single request.

### How it works
1.  **Schema Definition:** We define a strongly-typed schema (`.graphqls` file) that describes all possible data types and operations (Queries for reading, Mutations for writing).
2.  **Client Query:** The client sends a single `POST` request to a `/graphql` endpoint with a query string that specifies the desired fields.
3.  **Server Resolvers:** The server (using `spring-boot-starter-graphql`) maps each field in the schema to a "resolver" function. When a query comes in, the server executes only the resolvers for the requested fields.
4.  **N+1 Prevention:** `DataLoader` is used to batch and cache database lookups within a single request, preventing common performance problems.

### Why do it?
* **Efficient Data Fetching:** Eliminates over-fetching (getting more data than needed) and under-fetching (needing to make multiple requests to get all required data).
* **Faster Client Development:** Frontend teams can modify their data requirements without needing backend changes, as long as the fields are available in the schema.
* **Strong Typing:** The schema acts as a contract between the client and server, reducing guesswork and runtime errors.

### How to apply it to our project
1.  **Add Dependencies:** Include `org.springframework.boot:spring-boot-starter-graphql` in `build.gradle.kts`.

2.  **Define a Schema:** Create a `schema.graphqls` file in `src/main/resources/graphql/`.
    ```graphql
    type Query {
        product(id: ID!): Product
        products(page: Int = 0, size: Int = 20): [Product!]!
    }

    type Mutation {
        placeOrder(input: PlaceOrderInput!): Order!
    }

    type Product {
        id: ID!
        name: String!
        price: Float!
        options: [Option!]!
    }
    # ... other types
    ```

3.  **Implement Resolvers:** Create a `@GraphQlController` to map schema fields to our existing service methods.
    ```kotlin
    @GraphQlController
    class ProductGraphqlController(private val productService: ProductService) {

        @QueryMapping
        fun product(@Argument id: Long): Product {
            return productService.findById(id)
        }

        // ... other mappings for queries and mutations
    }
    ```
4.  **Reuse Business Logic:** The GraphQL resolvers should call our existing service methods, ensuring that all business rules, validation, and concurrency controls are reused.
