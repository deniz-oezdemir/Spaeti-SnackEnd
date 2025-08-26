# How to Read Our Gift Feature Tests

This guide explains the structure and intent behind our API tests for the gift purchase feature, which are located in the `OrderControllerTest.kt` file.

---

## Core Concepts

Our API tests are designed to validate the behavior of our controllers, ensuring they handle requests correctly and respond as expected.

### Test Structure: Arrange-Act-Assert

We follow the **Arrange-Act-Assert** (AAA) pattern, a standard for writing clean and understandable tests:

1.  **Arrange (or Given):** This is the setup phase. We define the inputs for our test (like a request body) and configure our mocked services. For instance, we tell the mocked `OrderService` what to return when it's called.

2.  **Act (or When):** This is the main action. We use `MockMvc` to perform an HTTP request to the controller's endpoint, simulating a real API call.
  * `mockMvc.perform(post("/orders/gift")...)`

3.  **Assert (or Then):** This is the verification phase. We check that the controller produced the correct results. We assert things like:
  * The HTTP status code is correct (e.g., `isCreated()`, `isBadRequest()`).
  * The response headers and body are what we expect.
  * We can also verify that our controller called the service method as intended.

### Test Environment

* **Slice Test:** These tests use `@WebMvcTest`, which only loads the web layer of our application. This makes them very fast.
* **Mocked Services:** All dependencies of the controller, like `OrderService`, are mocked using `@MockitoBean`. This allows us to isolate the controller and test its logic without needing a database or other components.

---

## Key Test Scenario Breakdowns

Our main API tests for the gifting flow are located in `src/test/kotlin/ecommerce/controller/OrderControllerTest.kt`. Below are the breakdowns of the most important tests.

### 1. Test: Successful Gift Purchase ("Happy Path")

* **Goal:** Verify the API responds correctly when a gift purchase is successful.
* **Location & Name:**
  * **File:** `ecommerce/controller/OrderControllerTest.kt`
  * **Test Name:** `placeGiftOrder - returns 201 with Location and response body()`
* **Arrange:**
  1.  We define a valid `GiftCheckoutRequest` object.
  2.  We configure the mocked `orderService.placeGift()` method to return a successful `PlaceOrderResponse` when called.
* **Act:**
  1.  We perform a `POST` request to the `/orders/gift` endpoint with the request object.
* **Assert:**
  1.  The HTTP response status is **201 Created**.
  2.  The `Location` header is correctly set to the new order's URL.
  3.  The JSON response body contains the expected `orderId` and `paymentStatus`.

### 2. Test: Payment Failure During Gift Purchase

* **Goal:** Ensure the API handles errors from the service layer gracefully (like a failed payment).
* **Location & Name:**
  * **File:** `ecommerce/controller/OrderControllerTest.kt`
  * **Test Name:** `placeGiftOrder - service error returns 5xx with message()`
* **Arrange:**
  1.  We define a valid `GiftCheckoutRequest`.
  2.  We configure the mocked `orderService.placeGift()` to **throw an exception** when called, simulating a payment failure.
* **Act:**
  1.  We perform a `POST` request to `/orders/gift`.
* **Assert:**
  1.  The HTTP response status is a **5xx Server Error**.
  2.  The response body contains the error message from the exception ("Payment not approved").

### 3. Test: Attempting to Gift with Invalid Data

* **Goal:** Verify that our API's input validation is working correctly.
* **Location & Name:**
  * **File:** `ecommerce/controller/OrderControllerTest.kt`
  * **Test Name:** `placeGiftOrder - when request body is invalid then 400()`
* **Arrange:**
  1.  We create a `GiftCheckoutRequest` with invalid data (e.g., a badly formatted recipient email).
* **Act:**
  1.  We perform a `POST` request to `/orders/gift` with the invalid request body.
* **Assert:**
  1.  The HTTP response status is **400 Bad Request**.
  2.  We verify that the `orderService` was **never called**, proving that the request was rejected early by the validation layer.
