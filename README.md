# 🛒 Spaeti SnackEnd

This is the team project for the "Hero Tech Course 2025" by [Farhana](https://github.com/farhanaahmed), [Gabriela](https://github.com/knopgm), [Sara](https://github.com/saragrosser), and [Deniz](https://github.com/deniz-oezdemir).

<img src="images/snacks.jpg" width="250" alt="products">

## Introduction

We aim to implement an online Späti Shop where users can buy and gift snacks and soft drinks.

Instead of the physical delivery of said products we will send the details and images of the products to the user via a chosen channel.

---
## Project Sprints & Implementation
Our development followed a structured three-sprint agile methodology, each focusing on a key phase of the project lifecycle.

### Sprint 1: Project Setup & Development Environment
Goal: Establish a robust foundation for collaboration and deployment.

| Requirement                           | Implementation Status & Details                                                                                                          |
|:--------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------|
| **Team & Project Name**               | ✅ **[Spaeti SnackEnd](https://github.com/deniz-oezdemir/Spaeti-SnackEnd)** established as the official name.                             |
| **Development Documentation**         | ✅ [Comprehensive guidelines](README-Development-Documentation.md) defined.                                                               |
| **Code Review Process & PR Template** | ✅ Established a mandatory review process and implemented a [PR template](.github/pull_request_template.md) for all merges.               |
| **CI Pipeline (GitHub Actions)**      | ✅ Set up [automated build](https://github.com/deniz-oezdemir/Spaeti-SnackEnd/actions) and test workflows on every push and pull request. |
| **Deploy API**                        | ✅ Successfully deployed a basic API to AWS EC2.                                                                                          |

### Sprint 2: Feature Development & Production Readiness
Goal: Develop core functionality and ensure the service is production-ready.

| Requirement                             | Implementation Status & Details                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
|:----------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Core Features**                       | ✅ Implemented [Email](https://github.com/deniz-oezdemir/Spaeti-SnackEnd/pull/55/commits/e0f6fd17fbc4191f3d885320e8e304938c2a1473) and [Slack](https://github.com/deniz-oezdemir/Spaeti-SnackEnd/pull/62) notifications for purchased Products. Key endpoints include:<br> • `POST /auth/register` - New users fill up the key channels for the notifications service<br> • `POST /orders` - Implemented features will be triggered after payment service and send proper notifications<br> |
| **Gift Feature**                        | ✅ Implemented a [Gift flow Feature](https://github.com/deniz-oezdemir/Spaeti-SnackEnd/pull/59) that allows users to buy products and send a Gift notification for another user.                                                                                                                                                                                                                                                                                                            |
| **API Documentation (Swagger)**         | 🔄 Automated API documentation integrated and available. See our [API Documentation](README-APIs.md).                                                                                                                                                                                                                                                                                                                                                                                      |
| **Logging Strategy**                    | 🔄 Structured logging implemented across services with INFO, WARN, and ERROR levels.                                                                                                                                                                                                                                                                                                                                                                                                       |
| **Production Deployment (HTTPS)**       | 🔄 Service deployed to a production AWS EC2 instance, secured with HTTPS and a custom domain.                                                                                                                                                                                                                                                                                                                                                                                              |
| **Production Database & Safety Policy** | ✅ PostgresSQL RDS instance provisioned. Policies implemented to prevent accidental data loss.                                                                                                                                                                                                                                                                                                                                                                                              |
| **Monitoring & Observability**          | 🔄 Integrated CloudWatch for logging and basic monitoring alerts.                                                                                                                                                                                                                                                                                                                                                                                                                          |

### Sprint 3: Performance Optimization & Scalability
**Goal:** Enhance performance, plan for scalability, and tackle advanced challenges.

| Requirement                                                 | Implementation Status & Details                                                                                                                |
|:------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------|
| **Infrastructure Scaling & SPOF Analysis**                  | 🔄 Conducted analysis. Proposed moving to a multi-AZ database and adding read replicas to eliminate SPOFs.                                     |
| <!--**Performance Challenge: Query Optimization** --> ...   | 🔄 <!-- Inserted large test dataset. Analyzed slow queries and applied targeted database indexes, improving response times by over 90%.--> ... |
| <!--**Performance Challenge: Concurrency Handling** --> ... | 🔄 <!-- Implemented optimistic locking for the `Product` entity to prevent stock overselling in high-concurrency scenarios. --> ...            |
| <!-- **Zero-Downtime Deployment**  --> ...                  | 🔄 <!-- Researching and planning for a blue-green deployment strategy. -->    ...                                                              |

---
## Tech Stack

**Backend:**
*   **Framework:** Spring Boot (Java/Kotlin)
*   **Persistence:** Spring Data JPA, Hibernate
*   **Database:** PostgreSQL (AWS RDS)
*   **API Documentation:** Springdoc OpenAPI (Swagger)

**Infrastructure & DevOps:**
*   **Cloud Provider:** AWS (EC2, RDS, S3, Route53)
*   **CI/CD:** GitHub Actions
*   **Monitoring:** AWS CloudWatch
*   **Version Control:** Git / GitHub

---
##  Documentation Index
Below you’ll find quick links to all our App README files: 

- [Development Documentation README](README-Development-Documentation.md) — Code Conventions,Branching Strategy, Commit Message Format.
- [E-commerce-Order README](README-Order.md) — Base project Features from previous mission already applied.
- [API's README](README-APIs.md) — API Documentation. 
- [Architecture README](README-Architecture.md) — Documentation of the tech stack and system architecture.

## Product Categories

Currently we plan on not selling any of the age restricted products. So we will only sell snacks and soft drinks.

<img src="images/product-categories.jpg" width="250" alt="product-categories">

## User Flow

The user flow "Surprise" is currently out of scope:

<img src="images/userflows.jpg" width="250" alt="userflows">

Note: no age verification is needed.

## Feature List

Roughly in descending order of importance:
- User can choose between channels to receive product pictures
  - WhatsApp
  - Slack
  - Email
- Buying
- Gift sending – allow sending orders directly to someone else 
- Ranking – e.g., most bought products in each category 

Implement if there is enough time:
- Surprise Me – select random product(s) within user’s budget
- Age verification – verify user’s age before purchase
- Group buying – e.g., 6 beers for the price of 5, chips + soft drink combo discount
- Feedback – collect ratings or feedback for products/stores

## Kanban Board

The board for coordination of working items can be found [here](https://github.com/users/deniz-oezdemir/projects/2/views/1).

## 🙏 Acknowledgments
*   Coaches and reviewers of the Hero Tech Course.
