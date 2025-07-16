# spring-ecommerce-product

## Features
### Step 1.1
- [x] Create a Product class
  - [x] contains id: Long, name: String, price: Double, imageUrl: String
  - [x] use AtomicLong to create the Id
- [x] Create ProductController
  - [x] use @RestController to return always JSON
  - [x] Create the "Database" in the form of HashMap()
  - [x] Create CRUD operations
- [x] Create a GlobalControllerAdvise to handle Exceptions
### Step 1.2
- [ ] Implement a controller that return html
- [ ] Detach the "Database" to be accessible by the two controllers
- [ ] Create a ProductService to simulate the connection with a real DataBase
- [ ] Inject the ProductService dependency to the controllers
- [ ] Create a template html with the list of all products
