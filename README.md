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
- [x] Integrate with the Stripe Payment Intents API to process customer payments.
- [x] When a payment is successful:
  - [x] Decrement the stock quantity of the purchased product option. 
  - [x] Remove the corresponding product from the user's shopping cart. 
- [x] When a payment fails:
  - [x] Ensure that product stock and the user's cart remain unchanged. 
  - [x] Provide a clear error message to the user that explains the reason for the payment failure.

### Step 2.2 (Order Management)
- [x] Design and create database tables to store order and payment information. 
- [x] After a successful payment, create and save a new order record. Saved order must contain the following details:
  - [x] Order date and time 
  - [x] Order status (e.g., COMPLETED, PENDING, FAILED)
  - [x] A list of purchased items and their options 
  - [x] The final payment amount 
  - [x] The transaction ID from Stripe
- [x] Implement an API endpoint for users to view their past orders

### Step 2.3 (Deployment)
- [x] Configure Cross-Origin Resource Sharing (CORS) to allow a web client to interact with the API
- [x] Create an automated deployment script to manage starting and stopping the application on a server
- [x] Prepare application for deployment to a live environment
- [x] Deploy on AWS EC2 

## References
- [REST API URI Naming Conventions and Best Practices](https://restfulapi.net/resource-naming/)
