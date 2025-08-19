# spring-ecommerce-order

## Step 2 Implementation (Latest)

### Place Order Feature -> `Step 2-1`
- [x] Integrate Stripe Payment Create API (sandbox key)
- [x] Place order with the selected product option and quantity
- [x] Decrease stock of the selected option after a successful order
- [x] Remove ordered product from user's cart if present
- [x] Handle payment approval API failure safely
- [x] Display clear error messages for payment failures:
  - [x] Expired session
  - [x] Invalid payment method
  - [x] Insufficient balance
  - [x] Other Stripe failure reasons

### Orders Feature -> `Step 2-2`
- [x] Implement "Orders" API endpoint to retrieve order details
- [x] Display the following order information:
  - [x] Order date and time
  - [x] Order status
  - [x] Purchased items
  - [x] Checkout session ID (issued by Stripe)
  - [x] Payment amount
- [x] Store optional payment-related fields in the database

### Deployment -> `Step 2-3`
- [x] Deploy order service to AWS server
- [x] Ensure the deployed service interacts with the client successfully
- [x] Write deployment script to automate deployment process
- [x] Handle security issues when interacting with client API (e.g., CORS issues)

#### Run as a systemd service

1. Create a new service file:

```

sudo nano /etc/systemd/system/ecommerce.service

```

2. Add the following content (adjust paths as needed):

```

[Unit]
Description=Ecommerce Spring Boot Application
After=network.target

[Service]
User=ubuntu
ExecStart=/usr/bin/java -jar /home/ubuntu/app/app.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target

```

3. Reload and enable the service:

```

sudo systemctl daemon-reload
sudo systemctl enable ecommerce
sudo systemctl start ecommerce

```

4. Check status:

```

sudo systemctl status ecommerce

```

5. Once registered, the deploy.sh script will automatically restart the service via:

```

sudo systemctl restart ecommerce

```

If the service is not registered, the script falls back to running the app with nohup.

---

## Step 1 Implementation (spring-ecommerce-order)

### Project Setup -> `Step 1-0`
- [x] Set up local development environment
- [x] Update README with structured plan

### Refactor JDBC to Spring Data JPA -> `Step 1-1`
- [x] Convert all models to JPA entities
- [x] Create Option entity
- [x] Implement entity relationships (`@OneToMany`, `@ManyToOne`, etc.)
- [x] Replace `JdbcTemplate` with `JpaRepository`
- [x] Add `@DataJpaTest` learning tests

### Pagination Support -> `Step 1-2`
- [x] Product list pagination (page, size, sort)
- [x] Cart items pagination (page, size, sort)

### Product Options Validation and Logic -> `Step 1-3`
- [x] Option must exist for each product
- [x] Name validation:
  - [x] Max 50 chars (incl. spaces)
  - [x] Allowed special characters: `( ) [ ] + - & / _`
  - [x] Must be unique per product
- [x] Quantity validation:
  - [x] Must be ≥ 1
  - [x] Must be < 100,000,000
- [x] Quantity decrease logic in Service/Entity (not API)

---

## spring-ecommerce-product (Previously Merged PR)

### CRUD Operations for `Product` -> `Step 1-1`
#### Model
- [x] Product
  - [x] id: long?
  - [x] name: String
  - [x] price: Double
  - [x] imageUrl: String

#### Controller
- [x] Create: returns the new Product
- [x] Read All: returns the products
- [x] Update: returns updated Product or throws `NotFoundException`
- [x] Delete: deletes the Product or throws `NotFoundException`
- [x] `findProduct` helper method for lookup with error handling

#### Exceptions
- [x] `NotFoundException` handled via `ControllerAdvice`

### Admin Interface -> `Step 1-2`
- [x] View all products (`products.html`)
- [x] Add product (form + JS request)
- [x] Update product (form + JS request)
- [x] Delete product (JS request)
- [x] Template for header and footer

### Integrate DB -> `Step 1-3`
- [x] Install DB dependency
- [x] Create `schema.sql` and `data.sql`
- [x] Configure `application.properties`
- [x] Create Product Repository
- [x] Update Controller for DB usage

### Product Validation -> `Step 2-1`
- [x] Product Name:
  - [x] Max 15 chars (incl. spaces)
  - [x] Allowed: `( ) [ ] + - & / _`
  - [x] Must be unique
- [x] Product Price > 0
- [x] Image URL starts with `http://` or `https://`
- [x] Invalid data returns errors via validation annotations + `ControllerAdvice`

### User Authentication -> `Step 2-2`
- [x] Register with email/password
- [x] Login + token issuance
- [x] Token required for member-only features

### Cart Functionality -> `Step 2-3`
- [x] Add to cart (authenticated)
- [x] View cart (authenticated)
- [x] Remove from cart (authenticated)

### Admin Analytics -> `Step 2-4`
- [x] Top 5 most added products in the last 30 days
- [x] Members who added items in the last 7 days
- [x] Role-based access for analytics endpoints
