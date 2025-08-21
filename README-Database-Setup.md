## Database Setup and Safety Policy

This project uses a **PostgreSQL database** with a clear separation between **production usage** and **schema migrations**, ensuring that accidental table drops or destructive operations do not occur in production.

---

### Phase A: Production Database Setup
- **RDS PostgreSQL instance** created on AWS  
- **Security group** configured to allow controlled access  
- **Connectivity** verified through EC2 bastion host  
- Database **`spaeti_prod`** created  
- Dedicated users created with specific roles and privileges:  
  - **`app_user`** → used by the application, restricted to **CRUD operations only** (no schema changes)  
  - **`migrator_user`** → reserved for Flyway/Liquibase migrations, has `CREATE` privileges but is separate from the runtime user  

---

### Phase B: Application Integration
- **Spring Boot** configured with environment-based profiles  
- `application-prod.properties` created with secure DB connection settings  
- **Hibernate safety policy** applied:  
  - `spring.jpa.hibernate.ddl-auto=validate` ensures schema is validated at runtime but never altered or dropped  
- Application packaged and deployed to **EC2** for verification  
- Current status: application attempts to connect to RDS but integration is intentionally postponed until final deployment to avoid unnecessary costs  

---

### Phase C: Local Development Environment
- Pending setup of a local **PostgreSQL database**   
- This will allow development and testing without relying on AWS infrastructure  
- Final step will include integration with the production RDS before project showcase  

---

