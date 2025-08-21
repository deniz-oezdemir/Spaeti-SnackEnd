# Email Service Documentation

## 1. Overview

The Email Service is a critical component of the Spaeti-SnackEnd platform, responsible for sending transactional emails to users for key e-commerce events. It is designed to provide a rich, engaging, and professional user experience by leveraging HTML-formatted emails that include product images and consistent branding.

The service handles three primary scenarios:
-   **Order Confirmation**: Sent to a user after a successful purchase.
-   **Gift Notification**: Sent to a recipient when a user purchases a gift for them.
-   **Order Failure**: Sent to a user if their payment fails during checkout.

---

## 2. Architecture & Implementation

The service is built using the **Spring Boot Starter Mail** framework, which provides robust and configurable email-sending capabilities.

### Key Design Choices:
-   **HTML Emails**: To move beyond basic text, the service uses `MimeMessage` and the `MimeMessageHelper` class. This allows for full control over the email's structure and appearance using HTML.
-   **Inline CSS**: All styling is applied using inline CSS attributes (`style="..."`) to ensure maximum compatibility across a wide range of email clients (like Gmail, Outlook, Apple Mail), which often have inconsistent support for external or `<style>` block CSS.
-   **Dynamic Content**: The HTML templates are constructed as Kotlin multiline strings, with dynamic data (user name, order details, product lists) injected at runtime.
-   **Embedded Images**: Product images are embedded directly into the email body using standard `<img>` tags. The `src` attribute points to the publicly accessible S3 URLs stored in the `Product` entity (`imageUrl` field).
-   **Interface-based Design**: The service adheres to the Dependency Inversion Principle by defining an `EmailService` interface. The `GmailEmailService` class provides the concrete implementation, making the system easier to test and maintain.

---

## 3. Manual Testing with Postman

This guide provides a complete end-to-end flow to test all three email scenarios using Postman.

### Prerequisites
1.  The Spring Boot application is running locally on `localhost:8080`.
2.  You have Postman or a similar API client installed.
3.  **Crucially**, you have configured your `application.properties` file with valid SMTP credentials for a real email account. For Gmail, it's recommended to use an **"App Password"**.

```properties
# src/main/resources/application.properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-real-email@gmail.com
spring.mail.password=your-gmail-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Step 1: Create Products
First, populate the database with some products. Send the following three requests.

**Request:**  
`POST http://localhost:8080/products`

#### Body (Coca-Cola)
```json
{
  "name": "Coca-Cola",
  "price": 1.49,
  "imageUrl": "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/cocacola-original.png",
  "options": [
    { "name": "original", "quantity": 300 }
  ]
}
```

#### Body (Mr Tom)
```json
{
  "name": "Mr Tom",
  "price": 0.99,
  "imageUrl": "[https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/mr-tom-erdnussriegel.png](https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/mr-tom-erdnussriegel.png)",
    "options": [
        { "name": "erdnussriegel", "quantity": 200 }
    ]
}
```

#### Body (Takis)
```json
{
  "name": "Takis",
  "price": 2.49,
  "imageUrl": "[https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/takis-fuego.png](https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/takis-fuego.png)",
    "options": [
        { "name": "fuego", "quantity": 100 }
    ]
}
```

### Step 2: Register a User
Create a user account. The email you provide here will receive the notifications.

**Request:**  
`POST http://localhost:8080/auth/register`

**Note**: Replace with your real email if you want to receive the emails.

```json
{
  "name": "user",
  "email": "user@example.com",
  "password": "password123",
  "role": "USER"
}

```

### Step 3: Log In and Get JWT Token
Log in to get an authentication token, which is required for cart and order operations.

**Request:**  
`POST http://localhost:8080/auth/login`

```json
{
  "name": "user",
  "email": "user@example.com",
  "password": "password123",
  "role": "USER"
}
```

**Action**: From the response, copy the accessToken value. You will need it for the next steps.

### Step 4: Add Items to Cart

Add the three products to the user's cart. To add the item multiple times to the cart, send the same request multiple times.

Remember to add the authorization header to each request.

**Header:**
`Authorization: Bearer <PASTE_YOUR_TOKEN_HERE>`

**Request:**  
`POST http://localhost:8080/api/protected/cart/created`

```json
{ "productOptionId": 1 }
```

```json
{ "productOptionId": 2 }
```

```json
{ "productOptionId": 3 }
```

From this point onward, the following Steps 5.1 to 5.3 all simulate different flows.

### Step 5.1: Successful Order Confirmation

This simulates a successful payment and triggers a confirmation email.

**Header:**
`Authorization: Bearer <PASTE_YOUR_TOKEN_HERE>`

**Request:**  
`POST http://localhost:8080/orders/checkout`

```json
{
  "paymentMethod": "pm_card_visa",
  "currency": "usd"
}
```

**Expected Outcome:**
✅ An email with the subject "Your Order is Confirmed!" is sent to `user@example.com`.
It should contain details and images for all three products (Coca-Cola, Mr Tom, Takis).
The cart is now empty.


### Step 5.2: Successful Gift Purchase

This tests the gift-sending flow, which triggers two separate emails.

**Header:**
`Authorization: Bearer <PASTE_YOUR_TOKEN_HERE>`

**Request:**  
`POST http://localhost:8080/orders/gift`

```json
{
  "recipientEmail": "recipient@mail.com",
  "message": "Happy Birthday!",
  "currency": "usd",
  "paymentMethod": "pm_card_visa"
}
```

**Expected Outcome:**
- **Buyer Confirmation:**
Subject: "Your gift purchase is confirmed!" → sent to user@example.com.

- **Gift Notification:**
Subject: "You’ve received a gift from user!" → sent to recipient@mail.com, including the gift message and product images.


### Step 5.3: Order Failure Notification

This simulates a declined payment and triggers a failure notification.

**Action**: First, repeat Step 4 to add the three items back into the cart.

**Header:**
`Authorization: Bearer <PASTE_YOUR_TOKEN_HERE>`

**Request:**  
`POST http://localhost:8080/orders/checkout`

```json
{
  "paymentMethod": "pm_card_chargeDeclined",
  "currency": "usd"
}
```

**Expected Outcome:**
❌ An email with the subject "There was an issue with your order" is sent to user@example.com.


