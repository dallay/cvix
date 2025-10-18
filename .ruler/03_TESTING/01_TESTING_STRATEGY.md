# Testing Strategy

> This document defines the project's testing strategy, outlining the different types of tests and their purpose.

## The Testing Pyramid

![The Testing Pyramid](The%20Testing%20Pyramid.png)
We follow the principles of the testing pyramid to ensure a balanced and effective testing portfolio. The pyramid consists of three main layers:

1. **Unit Tests (Base)**: The largest number of tests. They are fast, isolated, and verify small pieces of code (e.g., a single function or component).
2. **Integration Tests (Middle)**: Fewer than unit tests. They verify that different parts of the system work together correctly (e.g., a service interacting with a database).
3. **End-to-End (E2E) Tests (Top)**: The smallest number of tests. They simulate a full user journey through the application and are the slowest and most brittle.

## Unit Tests

- **Backend (Kotlin)**: Use **JUnit 5** and **Kotest**. All business logic in services and domain models must be unit tested. Mock dependencies using **MockK**.
- **Frontend (TypeScript)**: Use **Vitest**. Test all components, composables, and utility functions. Use `@testing-library/vue` for component interaction.
- **Goal**: Ensure individual units of code work as expected in isolation.

## Integration Tests

- **Backend**: Use **Testcontainers** to spin up real dependencies like PostgreSQL in a Docker container. This ensures that repository queries and service integrations work against a real database.
- **Frontend**: Integration tests often take the form of testing a component that relies on a Pinia store or a composable that makes a mock API call.
- **Goal**: Verify the interaction between different components of the application.

## End-to-End (E2E) Tests

- **Framework**: Use **Playwright** for all E2E tests.
- **Scope**: E2E tests should cover critical user flows, such as user registration, login, and core feature usage.
- **Environment**: E2E tests are run against a fully deployed, production-like staging environment.
- **Goal**: Ensure the entire system works together correctly from the user's perspective.

## General Principles

- **CI/CD**: All tests (Unit and Integration) are run automatically in our GitHub Actions CI pipeline on every commit.
- **Code Coverage**: We aim for high code coverage, but focus on testing behavior rather than just lines of code. Use Kover (backend) and Vitest coverage (frontend) to track this.
- **Test Naming**: Test names should be descriptive and follow the `should do something when condition` pattern.
