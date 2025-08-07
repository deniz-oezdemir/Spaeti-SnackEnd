# spring-ecommerce-order (New Features Implemented)

## Project Setup -> `Step 1-0`

- [x] Set up local development environment based on mission guide
- [x] Update README with structured development plan

## Refactor JDBC to Spring Data JPA -> `Step 1-1`

- [x] Convert all existing models into JPA entities
- [x] Create a new Option JPA entity
- [x] Implement JPA relationships between all entities (e.g., @OneToMany, @ManyToOne, etc.)
- [x] Replace JdbcTemplate repositories with Spring Data JPA repositories (JpaRepository)
- [x] Write learning tests using @DataJpaTest to validate entity mappings and repository functionality

## Pagination Support -> `Step 1-2`

- [x] Product List Pagination
- [x] Implement pageable API for product listing
- [x] Page size and number as request parameters
- [x] Support sorting by name and price
- [x] Cart-items View Pagination
- [x] Implement pagination for cart item list
- [x] Support page size, number, and sort parameters

## Product Options Validation and Logic -> `Step 1-3`
- [x] Validation Rules for Options
- [x] Each Product must have at least one Option
- [x] Validation for Option name
  - [x] Max 50 characters (incl. spaces)
  - [x] Allowed special characters: ( ) [ ] + - & / _
  - [x] All other special characters are not allowed
- [x] Must be unique per Product
- [x] Option quantity
  - [x] Must be ≥ 1
  - [x] Must be < 100,000,000
- [x] Logic for Quantity Decrease
    - [x] Method to decrease quantity of an Option
- [x] Implement in Service/Entity class (not API)

# spring-ecommerce-product (Previously Merged PR)

## CRUD Operations for `Product` -> `Step 1-1`
### Model
- [x] Product
    - [x] id: long?
    - [x] name: String
    - [x] price: Double
    - [x] imageUrl: String

### Controller
- [x] Create
    - [x] Create and returns the new Product
- [x] Read All
    - [x] Returns the products
- [x] Update
    - [x] Returns the Updated Product
    - [x] Throws NotFoundException if Product not found
- [x] Delete
    - [x] Returns ok status if deleted
    - [x] Throws NotFoundException if Product not found
- private fun findProduct
    - [x] finds the product
    - [x] Throws notFoundException

### Exceptions
- [x] NotFoundException
    - [x] Handled using ControllerAdvice

## Admin Interface Implementation for `Product` -> `Step 1-2`

### Views
- [x] View all products
- [x] added new page for `products.html`
- [x] Changed Read All for ThymeLeaf returns the page as String
- [x] Add a new product
    - [x] `products/new` method for showing the new product form
    - [x] create request handled by JS
- [x] Update a product
    - [x] `products/edit/${id}` method for showing the product form to edit
    - [x] update request handled by JS
- [x] Delete a product
    - [x] Send delete request using JS
- [x] Template for styling Footer and Header

## Integrate DB in Project `Product` -> `Step 1-3`
- [x] install db dependency
- [x] create schema.sql
- [x] create data.sql
- [x] add rules in application.properties
- [x] create table
- [x] Convert Controller for db usage
- [x] Create Product Repository to handle DB logic

## Product Validation -> `Step 2-1`

### Validation Rules
- [x] Product Name
    - [x] Max 15 characters (including spaces)
    - [x] Allowed special characters: ( ), [ ], +, -, &, /, _
    - [x] No other special characters allowed
    - [x] Must be unique
- [x] Product Price
    - [x] Must be greater than 0
- [x] Product Image URL
    - [x] Must start with `http://` or `https://`

### Error Handling
- [x] Invalid data returns appropriate error messages
- [x] Handled using validation annotations and ControllerAdvice

## User Authentication -> `Step 2-2`

### Member Registration and Login
- [x] Register
    - [x] User can register with email and password
- [x] Login
    - [x] Validates credentials
    - [x] Issues token upon successful login
- [x] Access Token
    - [x] Token is required for accessing member-only features in future

## Cart Functionality for Members -> `Step 2-3`

### Member Cart Features
- [x] Add Product to Cart
    - [x] Authenticated user can add products to their cart using token
- [x] View Cart
    - [x] Authenticated user can retrieve list of products in their cart
- [x] Remove from Cart
    - [x] Authenticated user can remove products from their cart

## Admin Analytics Features -> `Step 2-4`

### Analytics for Admin
- [x] Top 5 Most Added Products in Last 30 Days
    - [x] Returns product name, add count, and most recent added time
    - [x] If counts are equal, most recently added appears first
- [x] Members Who Added Items in Last 7 Days
    - [x] Returns member ID, name, and email
    - [x] Each member appears only once even if multiple adds
- [x] Role-Based Access
    - [x] (Optional) Restrict stat APIs to `ADMIN` role only