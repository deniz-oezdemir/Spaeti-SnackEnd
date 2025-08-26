# Feature Specifications & Test Scenarios

This document outlines the key features of the Spaeti SnackEnd application and the corresponding test scenarios to ensure functionality, reliability, and correctness.

---

## 1. User Authentication

### 1.1. Feature: User Registration
- **Specification:** A new user can register with a unique email, name, password, and an optional Slack User ID. The system assigns a `USER` role by default. Upon successful registration, a JWT is returned.
- **Test Scenarios:**
  - **✅ Happy Path:**
    - A user registers with valid, unique credentials.
    - **Expected:** `200 OK` response with a valid JWT. A new user record is created in the database.
  - **❌ Negative Cases:**
    - Attempt to register with an email that already exists.
    - **Expected:** `400 Bad Request` with an "Email is already registered" error message.
    - Attempt to register with an invalid email format.
    - **Expected:** `400 Bad Request` due to validation failure.
    - Attempt to register with a blank password or name.
    - **Expected:** `400 Bad Request` due to validation failure.

### 1.2. Feature: User Login
- **Specification:** A registered user can log in using their email and password to receive a JWT for accessing protected endpoints.
- **Test Scenarios:**
  - **✅ Happy Path:**
    - A registered user logs in with the correct email and password.
    - **Expected:** `200 OK` response with a valid JWT.
  - **❌ Negative Cases:**
    - Attempt to log in with a correct email but an incorrect password.
    - **Expected:** `401 Unauthorized` with an "Invalid password" error message.
    - Attempt to log in with an email that does not exist.
    - **Expected:** `401 Unauthorized` with a "Member not found" error message.

---

## 2. Product Management (Admin)

### 2.1. Feature: Product Creation
- **Specification:** An admin can create a new product. Product names must be unique and adhere to specific validation rules. Each product must have at least one option (e.g., size, flavor).
- **Validation Rules:**
  - **Name:** 1-15 characters, unique, allows `( ) [ ] + - & / _`.
  - **Price:** Must be greater than 0.
  - **Image URL:** Must start with `http://` or `https://`.
  - **Options:** Name max 50 chars, unique per product. Quantity between 1 and 99,999,999.
- **Test Scenarios:**
  - **✅ Happy Path:**
    - An admin creates a product with valid details and two options.
    - **Expected:** `201 Created` response. The product and its options are saved correctly in the database.
  - **❌ Negative Cases:**
    - Attempt to create a product with a name that already exists.
    - **Expected:** `400 Bad Request` with a "Product name must be unique" error.
    - Attempt to create a product with a name longer than 15 characters or containing invalid symbols (`@`, `!`).
    - **Expected:** `400 Bad Request` with a validation error message.
    - Attempt to create a product with a price of `0` or a negative number.
    - **Expected:** `400 Bad Request` with a "Price must be positive" error.
    - Attempt to create a product with an invalid image URL (e.g., `ftp://...`).
    - **Expected:** `400 Bad Request` with a URL format validation error.
    - Attempt to create a product with no options.
    - **Expected:** `400 Bad Request` or server error due to the business rule violation.

### 2.2. Feature: Product Update & Delete
- **Specification:** An admin can update the details of an existing product or delete it entirely.
- **Test Scenarios:**
  - **✅ Happy Path:**
    - Admin updates the price of an existing product.
    - **Expected:** `200 OK`. The product's price is updated in the database.
    - Admin deletes an existing product.
    - **Expected:** `204 No Content`. The product is removed from the database.
  - **❌ Negative Cases:**
    - Attempt to update a product that does not exist (invalid ID).
    - **Expected:** `404 Not Found`.
    - Attempt to delete a product that does not exist.
    - **Expected:** `404 Not Found`.

---

## 3. Shopping Cart & Ordering

### 3.1. Feature: Add to Cart
- **Specification:** An authenticated user can add a specific product option to their shopping cart. If the item already exists in the cart, its quantity is increased.
- **Test Scenarios:**
  - **✅ Happy Path:**
    - A user adds an item to an empty cart.
    - **Expected:** `201 Created`. A new cart item record is created.
    - A user adds the same item to the cart again.
    - **Expected:** `201 Created`. The quantity of the existing cart item is incremented.
  - **❌ Negative Cases:**
    - An unauthenticated user attempts to add an item to the cart.
    - **Expected:** `401 Unauthorized`.
    - A user attempts to add a product option that does not exist (invalid ID).
    - **Expected:** `404 Not Found`.

### 3.2. Feature: Place Order (from Cart)
- **Specification:** A user can check out all items in their cart, creating a single order. Stock for each item is decreased, and the cart is cleared upon successful payment.
- **Test Scenarios:**
  - **✅ Happy Path:**
    - A user with 3 items in their cart successfully checks out using a valid payment method (`pm_card_visa`).
    - **Expected:** `201 Created` response. An `Order` with 3 `OrderItems` is created. Stock for all three product options is reduced. The user's cart becomes empty. The user receives a confirmation email/Slack DM.
  - **❌ Negative Cases:**
    - A user attempts to check out with an empty cart.
    - **Expected:** `400 Bad Request` with a "Cannot checkout an empty cart" message.
    - A user's payment fails during checkout (e.g., using `pm_card_chargeDeclined`).
    - **Expected:** `5xx Server Error` (or appropriate payment error). No order is created, stock is not decreased, and the cart remains unchanged. The user receives an order failure email/Slack DM.
    - A user attempts to purchase more items than are available in stock for one of the products in the cart.
    - **Expected:** `400 Bad Request` with an "Insufficient stock" message before payment is processed.

---

## 4. Gifting Flow

### 4.1. Feature: Send a Gift
- **Specification:** A user can purchase the contents of their cart as a gift for someone else by providing a recipient's email and an optional message.
- **Notifications:**
  - The **buyer** receives a standard order confirmation email/Slack DM.
  - The **recipient** receives a separate gift notification email/Slack DM with the optional message and product details (no prices).
- **Test Scenarios:**
  - **✅ Happy Path:**
    - A user sends a gift with a valid recipient email and a message.
    - **Expected:** `201 Created`. The API responds quickly. The buyer's cart is cleared.
    - **Asynchronous Verification:**
      - The buyer receives an email with the subject "Your gift purchase is confirmed!".
      - The recipient receives an email with the subject "You’ve received a gift from [Buyer Name]!".
      - If Slack IDs are present, corresponding DMs are sent.
  - **❌ Negative Cases:**
    - Attempt to send a gift with an invalid recipient email format.
    - **Expected:** `400 Bad Request`.
    - The payment fails during the gift purchase.
    - **Expected:** `5xx Server Error`. The buyer receives an order failure notification. The recipient receives nothing.

---

## 5. Notifications (Email & Slack)

### 5.1. Feature: Notification Delivery
- **Specification:** The system sends real-time notifications for key events (order success, order failure, gift sent) via email and Slack DMs.
- **Test Scenarios:**
  - **✅ Email Content Verification:**
    - **Order Success:** Check that the email contains product images, quantities, and the correct total amount.
    - **Gift Notification:** Check that the recipient's email contains the buyer's name, the gift message, and does **not** contain pricing information.
    - **Order Failure:** Check that the email clearly states there was an issue.
  - **✅ Slack Content Verification:**
    - **Order Success:** Check that the Slack DM contains the order number and a summary of items.
    - **Order Failure:** Check that the DM clearly states the payment could not be processed.
  - **❌ Resilience Test:**
    - Manually simulate a failure in the email/Slack sending service.
    - **Expected:** The order placement/gifting API call should still succeed and return a `201 Created` response quickly. The failure to send a notification should be logged but should not fail the core transaction.
