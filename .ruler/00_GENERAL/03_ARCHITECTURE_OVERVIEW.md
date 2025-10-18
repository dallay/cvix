# Architecture Overview

> This document outlines the Hexagonal Architecture (Ports and Adapters) pattern implemented in the project to ensure separation of concerns, testability, and maintainability.

## Key Concepts

- **Core Domain**: The central part of the application containing the business logic, independent of external systems and frameworks.
- **Ports**: Interfaces defining how the core domain interacts with external systems (inbound for commands, outbound for data).
- **Adapters**: Implementations of ports that connect the core domain to external systems (e.g., REST controllers, database repositories).
- **Dependency Inversion**: The core domain defines interfaces (ports) that adapters implement, inverting the dependency flow.

## Clean Architecture Implementation

Each feature is self-contained and follows this standard structure:

```text
📁{feature}
├── 📁domain         // Core domain logic (pure Kotlin, no framework dependencies)
├── 📁application    // Use cases (CQRS commands/queries, framework-agnostic)
└── 📁infrastructure // Framework integration (Spring Boot, R2DBC, HTTP, etc.)
```

### Layers

1. **`domain`**: The core, containing pure Kotlin entities, value objects, domain events, exceptions, and repository interfaces.
2. **`application`**: Implements CQRS with command and query handlers that orchestrate domain logic. This layer is framework-independent.
3. **`infrastructure`**: The outermost layer, implementing adapters for HTTP (controllers), persistence (R2DBC repositories), and other external services. This is the only layer allowed to use framework-specific features.
