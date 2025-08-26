# Concurrency Handling — Preventing Data Conflicts

Concurrency Handling refers to the mechanisms to ensure correctness when multiple requests update the same data at the same time.
This document explains how we prevent data conflicts under concurrent user access in our project. 
It covers why we chose optimistic/pessimistic locking, what we implemented, and briefly how we could add atomic queries later if needed.

## 🔎 The Problem

In an e-commerce system, multiple users can attempt to purchase or update the same product option at the same time. Without concurrency control, this can cause:

    - Lost updates (one update silently overwrites another).

    - Overselling (stock going below zero).

## 🍀 Our Approach

We combined pessimistic locks (for checkout flows) with optimistic locks (as a global safety net).

### 1. Optimistic Locking (@Version)

    - Each row has a version number.

    - Hibernate includes version = ? in UPDATE statements.

    - If another transaction has already updated the row, the second update fails with OptimisticLockingFailureException.

<b> Why we use it: </b>

    - Prevents silent overwrites across the project.

    - Lightweight — no blocking.

    - Perfect for occasional updates (e.g., admin adjustments).

<b> Implementation: </b>

    - Added @Version to Option entity.

    - Added DB migration to include version column.

    - Added integration test to prove that stale updates fail.

    - Added a global handler mapping this exception to HTTP 409 Conflict.

### 2. Pessimistic Locking (PESSIMISTIC_WRITE)

    - Places a row-level lock in the database.

    - Other writers must wait until the first transaction commits or times out.

<b> Why we use it: </b>

    - We need to ensure exactly one buyer can decrement stock for the same Option at a time.

<b> Implementation: </b>

    - Added findWithLockById(id) in OptionRepositoryJpa with @Lock(PESSIMISTIC_WRITE).

    - Used it in all stock decrements inside OrderPersistenceService.

    - Added lock timeout hint to avoid indefinite blocking.

    - Added concurrency test: two buyers racing for 1 unit → only one succeeds, stock never goes negative.

    - Added global handlers for lock timeout / failure scenarios → mapped to HTTP 409 Conflict.

### 3. Atomic Queries (optional, not implemented yet)

As a future extension, we can add a repository method that decrements stock directly in a single conditional SQL update:

```
UPDATE options
SET quantity = quantity - :amount
WHERE id = :id
AND quantity >= :amount;
```

- Prevents negative stock naturally.

- Very high throughput (no entity hydration).

- Useful for flash sales or batch jobs.

Currently not needed in our flow, but easy to add later.

## 🛠️ Error Handling

We added specific exception handlers for concurrency-related issues in our global error handler:

    - Optimistic lock conflict → Logged as a warning and returned as 409 Conflict with a retry message.

    - Pessimistic lock failure → Logged as a warning and returned as 409 Conflict with a message that the resource is locked.

    - Cannot acquire lock (timeout) → Logged as a warning and returned as 409 Conflict with a message that the resource is busy.

This ensures all concurrency errors are consistently logged and documented in a clean, predictable way.

## 🧪 Tests 

    - Pessimistic lock test: two threads racing for one stock → only one succeeds.

    - Optimistic lock test: two stale transactions updating the same entity → second fails with a concurrency conflict.

## 🚀 Summary

- <b> Optimistic lock: </b> prevents lost updates globally.

- <b>  Pessimistic lock: </b> ensures checkout correctness.

- <b>  Atomic queries: </b> optional path for high-throughput flash sales.

- <b>  Global handler: </b> clean 409 Conflict responses for concurrency failures.

- <b>  Tests: </b> proof that stock never goes negative and stale writes fail.

## 📚 References 

- Hibernate Optimistic Locking — https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#locking-optimistic

- Hibernate Pessimistic Locking — https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#locking-pessimistic

- Spring Data JPA Locking and Transactions — https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.locking