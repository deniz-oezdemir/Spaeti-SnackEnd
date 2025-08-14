# Development Documentation

## 1. Code Conventions

We follow industry best practices to ensure code readability, maintainability, and scalability.

### 1.1 Principles
- **SRP (Single Responsibility Principle)** – Each class/module has one clear responsibility.
- **OOP (Object-Oriented Programming)** – Use encapsulation, inheritance, and polymorphism where it improves clarity and reduces duplication.
- **SOLID Principles** – Follow all five SOLID principles for clean architecture.
- **Clean Code** – Self-explanatory variable/method names, no magic numbers, no deep nesting.
- **DRY (Don’t Repeat Yourself)** – Reuse logic through functions, classes, or shared utilities.

### 1.2 Formatting
- **Autoformatting:** Run the project’s auto-formatter before committing (e.g., `ktlintFormat` for Kotlin).
- **Indentation:** 4 spaces (no tabs).
- **Naming:**
    - `PascalCase` for classes
    - `camelCase` for variables/methods
    - `SCREAMING_SNAKE_CASE` for constants

### 1.3 Testing
- **Unit Tests** – For isolated logic.
- **Integration Tests** – For interactions between modules/layers.
- **E2E Tests** – For full system verification.
- All merges to `main` require passing tests.

---

## 2. Branching Strategy

We use a **trunk-based branching** approach with `main` always in a deployable state.

### Workflow:
1. **Main branch** is always stable and production-ready.
2. Create a branch from `main` using:
    - `feat/<short-description>` – new features
    - `fix/<short-description>` – bug fixes
    - `chore/<short-description>` – maintenance work  
      Example: `feat/add-user-login`, `fix/incorrect-pricing-bug`
3. Commit changes regularly (see commit format below).
4. Push branch and open a **Pull Request (PR)**  following [SoundCloud's PR template](https://developers.soundcloud.com/blog/pr-templates-for-effective-pull-requests) to `main`.
5. PR must pass:
    - Code review
    - Automated tests
    - Manual testing (if required)
6. After approval, merge into `main`.
7. `main` is immediately deployable.

---

## 3. Commit Message Format (Angular Convention)

We follow **[Angular commit message guidelines](https://gist.github.com/stephenparish/9941e89d80e2bc58a153)** for clean history and changelog generation.

**Structure:**

**Types:**
- `feat` – New feature
- `fix` – Bug fix
- `docs` – Documentation only
- `style` – Formatting, no code changes
- `refactor` – Code changes without feature/bug impact
- `perf` – Performance improvements
- `test` – Adding or fixing tests
- `chore` – Maintenance, build/config updates