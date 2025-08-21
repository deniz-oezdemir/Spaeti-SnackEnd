# API Documentation with springdoc-openapi

This document outlines the implementation of our automated API documentation, explains its underlying mechanics, and records key decisions regarding technical debt for future improvements.

---

## Overview

To provide a clear, interactive, and always up-to-date reference for our API, we have integrated **`springdoc-openapi`**. This library automatically generates an **OpenAPI 3.0 specification** for our Spring Boot application and serves it through an interactive **Swagger UI**.

This approach ensures that our documentation is never out of sync with our code, as it's generated directly from the source.

---

## How It Works

The `springdoc-openapi` library works by hooking into the Spring application's startup process. Here's a step-by-step breakdown of what it does:

1.  **Code Scanning:** At startup, the library scans the application's compiled code.
2.  **Endpoint Discovery:** It looks for standard Spring web annotations (`@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, etc.) to identify all available API endpoints, their paths, and their HTTP methods.
3.  **Schema Generation:** It inspects the method signatures of your controller functions to determine the structure of requests and responses.
    * It uses annotations like `@PathVariable` and `@RequestParam` for parameters.
    * Crucially, for a `@RequestBody`, it analyzes the **data class (DTO)** specified in the method signature (e.g., `fun login(@RequestBody request: TokenRequest)`) to build the request body schema.
4.  **Annotation Processing:** It reads the OpenAPI annotations we've added (`@Operation`, `@Tag`, `@ApiResponse`, `@SecurityScheme`) to enrich the generated documentation with human-readable descriptions, examples, and security information.
5.  **Serving the Documentation:** Finally, it exposes two key endpoints:
    * `/v3/api-docs`: A raw JSON file containing the complete OpenAPI 3.0 specification.
    * `/swagger-ui.html`: An interactive HTML page (Swagger UI) that consumes the JSON from `/v3/api-docs` and presents it in a user-friendly format.

---

## Implementation Steps

The integration was completed in three main steps:

1.  **Added Dependency:** The `org.springdoc:springdoc-openapi-starter-webmvc-ui` library was added to our `build.gradle.kts` file.

2.  **Configured Application:** The main `Application.kt` file was annotated to provide global API metadata and define our security scheme for JWT authentication.
    * `@OpenAPIDefinition`: Sets the title and version for the API.
    * `@SecurityScheme`: Configures the "Authorize" button in Swagger UI to allow developers to use a JWT Bearer token for testing protected endpoints.

3.  **Annotated Controllers:** All controller classes were enhanced with OpenAPI annotations to provide detailed descriptions for every endpoint, including summaries, potential responses, and parameter information.

---

## Accessing the Documentation

With the application running, the documentation is available at the following URLs:

-   **Interactive Swagger UI:** `http://localhost:8080/swagger-ui.html`
-   **Raw OpenAPI Spec (JSON):** `http://localhost:8080/v3/api-docs`

---

## Known Issues & Technical Debt

### Shared DTOs Leading to Inaccurate Schemas

During implementation, we identified an issue where the generated documentation for some endpoints is not perfectly accurate. This is a deliberate technical debt trade-off we have made to avoid immediate, large-scale refactoring.

-   **The Cause:** We are reusing the same Data Transfer Object (DTO) for multiple, distinct API operations. E.g., the `TokenRequest` DTO, which is used for both user registration and user login.

-   **The Impact:** The `TokenRequest` DTO contains fields for `name`, `email`, `password`, `role`, and `slackUserId`. While all of these are relevant for registration, a login operation only requires `email` and `password`. Because the `/auth/login` endpoint uses the `TokenRequest` DTO, the API documentation incorrectly suggests that all fields are part of the login request.

-   **Decision:** We have accepted this documentation inaccuracy for now, with a plan to address it in the future.

---

## Future Work

### Refactor DTOs for Specificity

To resolve the technical debt and improve our API design, we will refactor all our DTOs to be specific to their use case.

**Exemplary Action Plan for `TokenRequest` DTO:**
1.  **Create Specific DTOs:** Create new data classes like `LoginRequest` (with only `email`, `password`) and `RegisterRequest` (with `name`, `email`, `password`, `slackUserId`).
2.  **Update Controllers:** Update the method signatures in the `AuthController` to use these new, specific DTOs.
3.  **Update Services:** Adjust the `AuthService` layer to accept the new DTOs.

This refactoring will not only fix the documentation but also lead to a more robust and self-documenting API.

