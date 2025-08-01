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
- [ ] Create an Option entity
- [ ] The entity should contain a name up to 50 characters
- [ ] The Option quantity must be at least 1 and less than 100_000_000
- [ ] Duplicated option names are not allowed within the same product to prevent confusion during purchase.

- [ ] Add Option choice to cart_item and dto's
- [ ] Modify the endpoint in cart_item to include the choice option
- [ ] Subtract the user option choice from stock for a product option.
- [ ] Create an endpoint in the admin controller for adding new options to products