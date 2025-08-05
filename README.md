# spring-ecommerce-order

## Features
### Step 1.1
- [x] Replace JdbcTemplate for spring data jpa.
- [x] Use JPA in the entities and services.
- [x] Add logging to JPA
### Step 1.2
- [x] Add Pagination for Products
- [x] Add wishlist items
- [x] Add Pagination for wishlist items
### Step 1.3
- [x] Create an Option entity
- [x] The entity should contain a name up to 50 characters
- [x] The Option quantity must be at least 1 and less than 100_000_000
- [x] Duplicated option names are not allowed within the same product to prevent confusion during purchase.

- [x] Add Admin functionality to create new option for product
- [x] Create an endpoint in the admin controller for adding new options to products

### Step 2.1 (Place Order) 
- [ ] Integrate with the Stripe Payment Intents API to process customer payments. 
- [ ] When a payment is successful:
  - [ ] Decrement the stock quantity of the purchased product option. 
  - [ ] Remove the corresponding product from the user's shopping cart. 
- [ ] When a payment fails:
  - [ ] Ensure that product stock and the user's cart remain unchanged. 
  - [ ] Provide a clear error message to the user that explains the reason for the payment failure.

### Step 2.2 (Order Management)
- [ ] Design and create database tables to store order and payment information. 
- [ ] After a successful payment, create and save a new order record. Saved order must contain the following details:
  - [ ] Order date and time 
  - [ ] Order status (e.g., COMPLETED, PENDING, FAILED)
  - [ ] A list of purchased items and their options 
  - [ ] The final payment amount 
  - [ ] The transaction ID from Stripe
- [ ] Implement an API endpoint for users to view their past orders.

### Future work (maybe?)
- [ ] Subtract the user option choice from stock for a product option

## References
- [REST API URI Naming Conventions and Best Practices](https://restfulapi.net/resource-naming/)
