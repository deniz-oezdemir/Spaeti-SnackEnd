# Gift Purchase Feature

## 1. Overview

The Gift Purchase Feature extends the Spaeti-SnackEnd platform’s checkout flow, allowing users to buy products and send them as a gift to another recipient.

Key highlights:

- Buyers can send items from their cart as a gift.

- The recipient receives a personalized gift notification email, which excludes monetary details.

- Buyers receive their usual order confirmation email (with payment and totals).

- The feature follows the same Stripe-based payment workflow as regular orders.

### API Endpoint:

`POST /orders/gift`

## 2. Architecture & Implementation

### 2.1 Design Choices

- Reused Checkout Patterns – Built on the existing order and payment pipeline to maintain consistency.

- Separate Persistence Method – persistGiftOrderAfterStripeSuccess(...) ensures gift orders store metadata (recipient email, gift message).

- Dual Email Notifications –

    - Buyer: Order confirmation (same as normal checkout).

    - Recipient: Gift notification email with the product list & optional message (no prices shown).

- Email Styling:

    - HTML emails with inline CSS for broad client support.

    - Product images are embedded using public S3 URLs.

    - Optional personal message block from the buyer is displayed with highlighted styling.

### 2.2 Email Service

Gift notification is sent via:

` emailService.sendGiftNotification(buyer, recipientEmail, order, message)`

Buyer order confirmation is sent via the existing confirmation flow:

`handleSuccessfulOrderNotificationEmail(member, order)`

This reuses the standard confirmation template and includes totals and payment details for the buyer only.

### 2.3 Persistence

Gift orders are stored with additional metadata:
```
- isGift = true

- giftRecipientEmail

- giftMessage

```
Stock is decreased per purchased item, payment records are saved, and the cart is cleared after success.

## 3. API Flow

### 3.1 Request Example

```
   POST /orders/gift
   Authorization: Bearer <JWT_TOKEN>
   Content-Type: application/json
```

Body:

```
{
"recipientEmail": "friend@example.com",
"message": "Happy Birthday! 🎉",
"currency": "eur",
"paymentMethod": "pm_card_visa"
}
```
### 3.2 Expected Outcomes

- Buyer Email: Subject → `Your gift purchase is confirmed!`

- Recipient Email: Subject → `You’ve received a gift from <BuyerName>!`

- API Response:

```
{
"orderId": 777,
"paymentStatus": "succeeded",
"message": "Gift order successful."
}
```

## 4. Testing

### 4.1 Unit & Controller Tests

Added tests in `OrderControllerTest` to validate:

- ✅ `201 Created` on success (with Location header & JSON body).

- ✅ `400 Bad Request` for invalid payloads (e.g., bad email).

- ✅ `404 Not Found` if member does not exist.

- ✅ `5xx Server Error` when payment is declined or service throws an error.

### 4.2 Manual Testing with Postman (End-to-End)

- Create Products (e.g., Coca-Cola, Mr Tom, Takis) with S3 image URLs.

- Register & Login a user → obtain JWT token.

- Add to Cart (include multiple options if desired).

- Gift Flow: POST /orders/gift with recipient details → verify two emails (buyer confirmation + recipient gift notification) and cleared cart.

- Failure Flow: simulate declined payment to receive a failure email for the buyer.

- SMTP Setup: Configure `spring.mail.*` with a real SMTP (e.g., Gmail via App Password) in `application.properties`.


## 6. Implementation Notes

- <b> Images: </b> Email <img> tags use product imageUrl fields hosted on S3 (public URLs).

- <b> Inline CSS: </b> Ensures compatibility across Gmail, Outlook, Apple Mail.

- <b> Separation of Concerns: </b> Email construction lives in the email service; order persistence in the persistence/service layer.

- <b> Error Handling: </b> Payment or persistence failures trigger buyer failure emails via the existing failure handler.