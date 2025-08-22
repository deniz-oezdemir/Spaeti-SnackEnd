# Database Setup and Safety Policy

This project uses a **PostgreSQL database** with a strong separation of environments (**local development** vs. **production RDS**) and a **role-based access policy** to ensure safety.  
The primary goal is to prevent **accidental destructive operations** (e.g., `DROP TABLE`) in production, while keeping local development flexible and efficient.

---

## 1. Environment Overview

### Local Development (`dev` profile)
- Database: **PostgreSQL (spaeti_dev)** running locally
- Schema/data initialization:
    - `schema.sql` and `data.sql` are executed on startup
    - Used to quickly bootstrap a consistent dataset for testing
- Spring Boot configuration:
    - `spring.jpa.hibernate.ddl-auto=validate` → schema validated against entities, never auto-created
    - `spring.sql.init.mode=always` → ensures schema/data scripts are applied
- Purpose: allow developers to iterate quickly without risk to production data.

### Production (`prod` profile)
- Database: **AWS RDS PostgreSQL (spaeti_prod)**
- Connectivity secured via **VPC, subnet groups, and security groups**
- **EC2 bastion host** used for safe database access
- Spring Boot configuration:
    - `spring.jpa.hibernate.ddl-auto=validate` → schema is only validated, never modified
    - No automatic schema/data initialization
- Purpose: ensure data integrity and prevent destructive changes.

---

## 2. Policy to prevent accidental table drops

To enforce the safety policy, dedicated users were created with **least privilege access**:

- **`app_user`**
    - Used by the running Spring Boot application
    - Privileges: `SELECT`, `INSERT`, `UPDATE`, `DELETE`
    - ❌ No `DROP TABLE`, `CREATE TABLE`, or schema modification rights
    - Ensures that application bugs cannot damage the schema

- **`migrator_user`**
    - Reserved for schema migrations (Flyway, Liquibase, or manual DDL)
    - Privileges: `CREATE`, `ALTER`, `DROP` in the target schema
    - ❌ Not used by the application at runtime

- **`master user` (RDS admin)**
    - Created during RDS instance setup
    - Full superuser privileges
    - Used only for database provisioning, role management, and granting privileges

---

## 3. Initialization Flow

- **Local development**
    - On startup, Spring runs `schema.sql` and `data.sql` to create tables and seed products/options
    - Example: products table is initialized with Coca-Cola, Fanta, Apfelschorle, Fritz-Kola

- **Production**
    - Schema must be created beforehand (via `migrator_user`)
    - Application will only validate schema consistency, never attempt to modify it
    - Ensures that accidental `DROP TABLE` or `CREATE TABLE` statements cannot run from the application

---

## 4. Safety Mechanisms

- **DDL disabled** for the runtime user in production
- **Environment-specific profiles** guarantee that schema initialization only happens locally
- **Explicit grants** applied to users (PostgreSQL `GRANT` statements)
- **Fail-fast validation** (`ddl-auto=validate`) prevents startup if schema and entities drift

---

## 5. Example: User Creation & Grants (PostgreSQL)

```sql
-- Master user creates roles
CREATE USER appuser WITH PASSWORD 'securepassword';
CREATE USER migrator_user WITH PASSWORD 'securepassword';

-- Application user (no schema modifications allowed)
GRANT CONNECT ON DATABASE spaeti_prod TO appuser;
GRANT USAGE ON SCHEMA public TO appuser;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO appuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO appuser;

-- Migrator user (for schema migrations only)
GRANT CONNECT ON DATABASE spaeti_prod TO migrator_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO migrator_user;