# spring-ecommerce-productDTO

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
- [x] Implement a controller that return html
- [x] Detach the "Database" to be accessible by the two controllers
- [x] Create a ProductService to simulate the connection with a real DataBase
- [x] Inject the ProductService dependency to the controllers
- [x] Create a template html with the list of all products
- [x] Add JS for CRUD request in the frontend.
- [x] Display image instead of a string on image URL
### Step 1.3
- [x] Configure H2 database
- [x] Create a product repository for database operation
- [x] Create data schema and initialize and insert data to it
- [x] Modify the controller to use the new productDTO repository
## Step 1.4
- [ ] Create a ProductDTO class
  - [ ] contains id: Long, name: String, price: Double, imageUrl: String
  - [ ] add copyFrom method to copy data from Product to ProductDTO
- [ ] Create a ProductPatchDTO class for nullable fields
  - [ ] contains id: Long, name: String, price: Double, imageUrl: String
- [ ] Add Mappers to convert Product to ProductDTO and vice versa
- [ ] Create ProductService interface
  - [ ] Add ProductServiceCollection for CRUD operations with fake DB
  - [ ] Add ProductServiceJDBC for the logic between the controller and the repository
  - [ ] Add @Primary to the JDBC service to be used by default
- [ ] Add ProductRepository for CRUD operations with the H2 database
- [ ] The Repository should only handle the product entity
- [ ] The service should handle the ProductDTO conversion to Product Entity and vice versa
- [ ] Add extensions functions to convert Product to ProductDTO and vice versa
- [ ] Add more custom exceptions
- [ ] Handle custom and jdbc exceptions in the GlobalControllerAdvise
- [ ] Use the Product Entity exclusively in the repository
- [ ] Update the controller to use the new structure
- [ ] Add logging to the app with SLF4J
- [ ] Use logging instead of print statements