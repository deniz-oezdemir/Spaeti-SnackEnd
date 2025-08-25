# Event-Driven Architecture for Gift Sending

This document outlines the event-driven architecture (EDA) implemented for the gift ordering feature. It details the motivation, the new asynchronous flow, and the key Spring concepts used to achieve it.

---

## Overview & Motivation (The "Why")

The original gift ordering process was **synchronous**. When a user placed a gift, the API would perform all tasks sequentially before returning a response:

1.  Process payment with Stripe.
2.  Save the order to the database.
3.  Send a confirmation email to the buyer.
4.  Send a gift notification to the recipient.
5.  Optionally send a Slack a notification to the buyer.

This approach had two major drawbacks:
* **Poor Performance:** The user had to wait for slow operations like sending emails to complete, resulting in a slow API response time.
* **Low Resilience:** If a notification service (e.g., the email server) was down or slow, it would delay or even fail the entire order process for the user.

By refactoring to an **event-driven architecture**, we now only perform the critical, transactional tasks synchronously. All non-essential side effects (like sending notifications) are handled asynchronously in the background. This results in a significantly **faster** and **more resilient** application.

---

## The New Asynchronous Flow (The "How")

The new flow is orchestrated by publishing an event after the core transaction is complete.

**1. The Event: `GiftOrderPlacedEvent`**
A simple data class that acts as a message, containing only the `orderId`. This signals that a gift order was successfully created.

**2. The Producer: `OrderService`**
After successfully processing the payment and saving the order to the database, the `OrderService` publishes the `GiftOrderPlacedEvent`. It no longer has any direct knowledge of the email or Slack services.

**3. The Consumers: `GiftNotificationListeners`**
A dedicated service contains listener methods that subscribe to the `GiftOrderPlacedEvent`. When the event is published, these methods run automatically in the background to handle the notifications:
* One listener sends the confirmation email to the buyer.
* One listener sends the gift notification email to the recipient.
* One listener sends the Slack notification to the buyer.

This decouples the core ordering logic from the notification side effects.

---

## Key Concepts & Implementation Details

We used several core Spring Framework features to build this architecture:

* **`@EnableAsync`:** Added to the main `Application` class, this annotation enables Spring's asynchronous method execution capabilities.

* **`@Async`:** Applied to our listener methods, this annotation tells Spring to execute them on a separate background thread pool, so they don't block the main user request.

* **`ApplicationEventPublisher`:** This Spring component is injected into the `OrderService` to publish events to the application context.

* **`@TransactionalEventListener`:** This is a crucial replacement for the standard `@EventListener`. It instructs Spring to wait until the main database transaction has **successfully committed** before publishing the event. This solves two critical problems:
  1.  It prevents race conditions where listeners would try to fetch an order before it was saved.
  2.  It ensures notifications are only sent for orders that were successfully persisted.

## Performance Improvements

During this refactoring, we also identified and solved a classic **N+1 Query Problem** in the listeners. By creating a custom repository method with `JOIN FETCH`, we now load all required order data in a single, efficient query per listener, significantly reducing database round trips.
