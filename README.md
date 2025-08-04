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

### Future work (maybe?)
- [ ] Subtract the user option choice from stock for a product option

## References
- [REST API URI Naming Conventions and Best Practices](https://restfulapi.net/resource-naming/)
