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

### Dependency Flow

The architecture follows a strict dependency rule:

```text
domain ← application ← infrastructure
```

- **Infrastructure** depends on **Application** and **Domain**
- **Application** depends only on **Domain**
- **Domain** depends on nothing (pure business logic)

### Layer Details

#### 1. Domain Layer (`domain/`)

The innermost layer containing pure Kotlin code with no framework dependencies. This layer defines:

- **Entities**: Core business objects (e.g., `Workspace.kt`, `WorkspaceMember.kt`)
- **Value Objects**: Immutable objects representing domain concepts (e.g., `WorkspaceId.kt`, `WorkspaceRole.kt`)
- **Repository Interfaces**: Contracts for data access (e.g., `WorkspaceRepository.kt`, `WorkspaceFinderRepository.kt`)
- **Domain Events**: Events that represent business facts (e.g., `WorkspaceCreatedEvent.kt`)
- **Domain Exceptions**: Business-specific exceptions (e.g., `WorkspaceException.kt`, `WorkspaceNotFoundException.kt`)

**Example structure:**

```text
📁domain
├── Workspace.kt                    // Entity
├── WorkspaceId.kt                  // Value Object
├── WorkspaceRole.kt                // Value Object
├── WorkspaceRepository.kt          // Repository interface
├── WorkspaceFinderRepository.kt    // Query repository interface
├── WorkspaceException.kt           // Domain exception
└── 📁event
    └── WorkspaceCreatedEvent.kt    // Domain event
```

#### 2. Application Layer (`application/`)

The use case layer implementing business logic orchestration. This layer is organized by **operations** following CQRS principles:

**Organization:**

- Operations are grouped by folders (e.g., `create/`, `find/`, `update/`, `delete/`)
- Each operation contains:
  - **Command/Query**: The input data structure
  - **CommandHandler/QueryHandler**: Receives the command/query and delegates to a service
  - **Service**: Contains the business logic implementation (e.g., `WorkspaceCreator`, `WorkspaceFinder`)

**Command Pattern (Writes):**

```text
📁application/create
├── CreateWorkspaceCommand.kt        // Input DTO
├── CreateWorkspaceCommandHandler.kt // Receives command, delegates to service
└── WorkspaceCreator.kt              // Service with creation logic
```

**Flow:** `Controller → CommandHandler → Service → Domain Repository`

**Query Pattern (Reads):**

```text
📁application/find
├── FindWorkspaceQuery.kt            // Input DTO
├── FindWorkspaceQueryHandler.kt     // Receives query, delegates to service
└── WorkspaceFinder.kt               // Service with query logic
```

**Flow:** `Controller → QueryHandler → Service → Domain Repository`

**Key Principles:**

- Handlers are thin: they receive commands/queries and delegate to specialized services
- Services contain the actual business logic and coordinate with repositories
- All application code is framework-agnostic (no Spring annotations in business logic)

#### 3. Infrastructure Layer (`infrastructure/`)

The outermost layer implementing technical concerns and framework integration. Organized by **technical responsibility**:

**Organization:**

```text
📁infrastructure
├── 📁http                                    // REST API endpoints
│   ├── CreateWorkspaceController.kt
│   ├── FindWorkspaceController.kt
│   └── 📁request                             // Request DTOs
│       └── CreateWorkspaceRequest.kt
├── 📁persistence                             // Database implementation
│   ├── WorkspaceStoreR2DbcRepository.kt      // Repository implementation
│   ├── 📁entity                              // Database entities (JPA/R2DBC)
│   │   └── WorkspaceEntity.kt
│   ├── 📁mapper                              // Entity ↔ Domain mappers
│   │   └── WorkspaceMapper.kt
│   └── 📁repository                          // Spring Data repositories
│       └── WorkspaceR2DbcRepository.kt
└── 📁event                                   // Event infrastructure
    └── WorkspaceEventPublisher.kt
```

**Responsibilities:**

- **`http/`**: REST controllers that receive HTTP requests and invoke command/query handlers
- **`persistence/`**: Database adapters implementing domain repository interfaces using Spring Data R2DBC
  - `entity/`: Database-specific entities
  - `mapper/`: Conversion between domain models and database entities
  - `repository/`: Spring Data repository interfaces
- **`event/`**: Event publishing infrastructure (message queues, event buses)

**Key Principles:**

- Controllers are thin: they validate input, call handlers, and return responses
- Persistence layer implements domain repository interfaces defined in the domain layer
- Never expose database entities through the API; always use DTOs
- This is the only layer that can use framework-specific features (Spring annotations, R2DBC, etc.)
