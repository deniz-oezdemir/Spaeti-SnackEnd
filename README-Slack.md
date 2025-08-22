
# Slack Notification Service Documentation

## 1. Overview

The **Slack Notification Service** extends the Spaeti-SnackEnd platform by providing real-time Slack direct messages (DMs) to users after payment events.  
It improves user engagement by delivering instant, private confirmations directly in Slack.

The service handles two primary scenarios:
- **Order Confirmation**: Sent when a payment succeeds.
- **Order Failure**: Sent when a payment fails (e.g., card declined).

---

## 2. Architecture & Implementation

The service is built using the **Slack Java SDK (`MethodsClient`)**, which provides APIs for
sending messages to Slack users that are already inside the workspace of th company's Slack.

### Key Design Choices:
- **Direct Messages (DMs)**: The bot opens a private Slack channel with the user via
[`conversations.open`](https://api.slack.com/methods/conversations.open) before sending notifications.
- **Rich Message Blocks**: Instead of plain text, the service uses Slack’s
[Block Kit](https://api.slack.com/block-kit) for formatted messages, including order details and contextual styling.
- **Resilient Error Handling**: Failures (e.g., invalid Slack user ID, network errors) are logged, 
and unit tests covers both success and failure scenarios.

---

## 3. Manual Testing with Postman

This section documents the end-to-end testing of Slack notifications using Postman requests.

### Prerequisites
1. The Spring Boot application is running locally on `localhost:8080`.
2. You have Postman or a similar API client installed.
3. The `application.properties` file is configured with a valid Slack **Bot User OAuth Token**.

```properties
slack.bot.token=xoxb-your-slack-bot-token-example
````

---

### Step 1: Register a User

**Request:**
`POST http://localhost:8080/auth/register`

```json
{
  "name": "Jon Doe",
  "email": "jondoe@example.com",
  "password": "password123",
  "role": "USER",
  "slackUserId": "U12345678"
}
```

✅ The `slackUserId` corresponds to the user’s Slack account.

---

### Step 2: Log In and Get JWT Token

**Request:**
`POST http://localhost:8080/auth/login`

```json
{
  "email": "jondoe@example.com",
  "password": "password123"
}
```

PS.: Copy the `accessToken` for use in the following requests.

---

### Step 3: Create a Product

**Request:**
`POST http://localhost:8080/products`

```json
{
  "name": "Club-Mate",
  "price": 2.49,
  "imageUrl": "https://example.com/products/club-mate.png",
  "options": [
    { "name": "500ml", "quantity": 50 }
  ]
}
```

---

### Step 4: Add to Cart

**Header:**
`Authorization: Bearer <PASTE_YOUR_TOKEN_HERE>`

**Request:**
`POST http://localhost:8080/api/protected/cart/created`

```json
{ "productOptionId": 1 }
```

---

### Step 5.1: Successful Order Confirmation

**Request:**
`POST http://localhost:8080/orders/checkout`

```json
{
  "paymentMethod": "pm_card_visa",
  "currency": "usd"
}
```

**Expected Outcome:**
✅ The user receives a **Slack DM** from the bot with the message:<br>
*"Your order has been confirmed!"*, including product details.
---

### Step 5.2: Order Failure Notification

Repeat Step 4 to add the item back into the cart, then:

**Request:**
`POST http://localhost:8080/orders/checkout`

```json
{
  "paymentMethod": "pm_card_chargeDeclined",
  "currency": "usd"
}
```

**Expected Outcome:**
❌ The user receives a **Slack DM** from the bot with the message:<br>
*"Your payment could not be processed."*

---

## 4. Expected Output

Below are sample Slack messages sent by the bot:

* **Order Confirmation:**
  ![Order Confirmation](https://github.com/user-attachments/assets/acd1713f-c480-4d1c-89cf-41b8584fbb3a)

* **Order Failure:**
  ![Order Failure](https://github.com/user-attachments/assets/3b7979b3-73e8-4ac9-b2aa-c49526d96fed)

---

## 5. Reference

* [Slack API Documentation](https://api.slack.com/apis/events-api)
* [Slack Java SDK](https://github.com/slackapi/java-slack-sdk)

---
