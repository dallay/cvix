---
title: "Quick Start"
description: "A quick guide to getting the ProFileTailors platform running locally."
---

## Local Development Setup

This guide provides the essential steps to get the ProFileTailors platform running on your local machine for development. The project is managed via a centralized `Makefile` to simplify setup and ensure consistency.

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java (JDK)**: Version 21 or higher
- **Node.js**: Version 24.12.0 or higher
- **pnpm**: Version 10 or higher
- **Docker and Docker Compose**: For running backend services and dependencies
- **Make**: To use the simplified commands in the `Makefile`

### Setup and Installation

Follow these steps to prepare your environment and run the applications:

1.  **Prepare Environment Files**:
    This command copies `.env.example` to `.env` and sets up necessary configurations.

    ```bash
    make prepare-env
    ```

2.  **Install Dependencies**:
    This installs all frontend (pnpm) and backend (Gradle) dependencies.

    ```bash
    make install
    ```

3.  **Generate SSL Certificates**:
    For local HTTPS, generate the required SSL certificates.

    ```bash
    make ssl-cert
    ```

### Running the Applications

-   **Run the Backend**:
    This command starts the Spring Boot application along with its dependencies (PostgreSQL, Keycloak) via Docker Compose.

    ```bash
    make backend-run
    ```

-   **Run the Frontend Web App**:
    This starts the Vue.js frontend application in development mode with hot-reloading.

    ```bash
    make dev-web
    ```

-   **Run the Documentation Site**:
    To view and work on the documentation locally, run:

    ```bash
    make dev-docs
    ```

### Verifying the Setup

To ensure everything is working correctly, run the full verification suite. This command executes all checks, linters, and tests for both the frontend and backend.

```bash
make verify-all
```

This is the definitive command to confirm that the entire platform is stable and ready.
