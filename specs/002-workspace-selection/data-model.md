# Data Model: Workspace Selection Implementation

**Feature**: Workspace Selection Implementation
**Branch**: `002-workspace-selection`
**Date**: 2025-10-28

## Purpose

This document defines the domain entities, value objects, and state models for the workspace selection feature.

## 1. Domain Entities

### Workspace

Represents a work environment or project context that a user can access.

```typescript
interface Workspace {
  id: string;                    // UUID v4 format
  name: string;                  // Display name (1-100 characters)
  description: string | null;    // Optional description (max 500 characters)
  isDefault: boolean;            // Whether this is the user's default workspace
  ownerId: string;               // UUID of workspace owner
  createdAt: Date;               // ISO 8601 datetime
  updatedAt: Date;               // ISO 8601 datetime
}
```

**Validation Rules**:

- `id`: Must be valid UUID v4 format
- `name`: Required, 1-100 characters, no leading/trailing whitespace
- `description`: Optional, max 500 characters
- `isDefault`: Boolean, defaults to false
- `ownerId`: Must be valid UUID v4 format
- `createdAt`: Must be valid ISO 8601 datetime
- `updatedAt`: Must be valid ISO 8601 datetime, must be >= createdAt

**Business Rules**:

- Every user must have at least one workspace (enforced by backend)
- Every user must have exactly one default workspace (enforced by backend)
- Workspace names must be unique per user (enforced by backend)

### WorkspaceSelectionPreference

Represents the user's workspace selection history.

```typescript
interface WorkspaceSelectionPreference {
  userId: string;                // UUID of authenticated user
  lastSelectedWorkspaceId: string | null; // UUID of last selected workspace
  selectedAt: Date | null;       // Timestamp of last selection
}
```

**Storage**:

- Persisted in browser local storage
- Key: `loomify:workspace:lastSelected`
- Value: JSON serialized WorkspaceSelectionPreference

**Validation Rules**:

- `userId`: Must be valid UUID v4 format
- `lastSelectedWorkspaceId`: Must be valid UUID v4 format or null
- `selectedAt`: Must be valid ISO 8601 datetime or null

## 2. Value Objects

### WorkspaceId

```typescript
class WorkspaceId {
  private readonly value: string;

  constructor(id: string) {
    if (!WorkspaceId.isValid(id)) {
      throw new Error(`Invalid workspace ID: ${id}`);
    }
    this.value = id;
  }

  static isValid(id: string): boolean {
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    return uuidRegex.test(id);
  }

  toString(): string {
    return this.value;
  }

  equals(other: WorkspaceId): boolean {
    return this.value === other.value;
  }
}
```

**Purpose**: Type-safe workspace identifier with validation.

### WorkspaceName

```typescript
class WorkspaceName {
  private readonly value: string;

  constructor(name: string) {
    const trimmed = name.trim();
    if (trimmed.length < 1 || trimmed.length > 100) {
      throw new Error('Workspace name must be 1-100 characters');
    }
    this.value = trimmed;
  }

  toString(): string {
    return this.value;
  }
}
```

**Purpose**: Encapsulates workspace name validation logic.

## 3. State Models

### WorkspaceState (Pinia Store)

```typescript
interface WorkspaceState {
  // Data
  workspaces: Workspace[];
  currentWorkspace: Workspace | null;
  lastSelectedId: string | null;

  // UI State
  isLoading: boolean;
  isLoadingWorkspaces: boolean;
  isSwitching: boolean;
  error: WorkspaceError | null;

  // Cache
  cacheTimestamp: number | null; // Unix timestamp in milliseconds
  cacheTTL: number;              // 5 minutes = 300000ms
}
```

**State Transitions**:

```text
INITIAL → LOADING_WORKSPACES → WORKSPACES_LOADED
WORKSPACES_LOADED → SWITCHING → WORKSPACE_SELECTED
WORKSPACE_SELECTED → SWITCHING → WORKSPACE_SELECTED
ANY_STATE → ERROR → (can retry from ERROR)
```

### WorkspaceError

```typescript
interface WorkspaceError {
  code: WorkspaceErrorCode;
  message: string;
  details?: unknown;
  timestamp: Date;
}

enum WorkspaceErrorCode {
  NETWORK_ERROR = 'NETWORK_ERROR',
  API_ERROR = 'API_ERROR',
  VALIDATION_ERROR = 'VALIDATION_ERROR',
  NO_WORKSPACES = 'NO_WORKSPACES',
  WORKSPACE_NOT_FOUND = 'WORKSPACE_NOT_FOUND',
  NO_DEFAULT_WORKSPACE = 'NO_DEFAULT_WORKSPACE',
  UNAUTHORIZED = 'UNAUTHORIZED',
}
```

**Error Messages** (i18n keys):

- `NETWORK_ERROR`: "workspace.error.network"
- `API_ERROR`: "workspace.error.api"
- `VALIDATION_ERROR`: "workspace.error.validation"
- `NO_WORKSPACES`: "workspace.error.noWorkspaces"
- `WORKSPACE_NOT_FOUND`: "workspace.error.notFound"
- `NO_DEFAULT_WORKSPACE`: "workspace.error.noDefault"
- `UNAUTHORIZED`: "workspace.error.unauthorized"

## 4. API Response Models

### GetAllWorkspacesResponse

```typescript
interface GetAllWorkspacesResponse {
  data: Workspace[];
  meta: {
    total: number;
    hasMore: boolean; // Always false for MVP (no pagination)
  };
}
```

**Example Response**:

```json
{
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "My First Workspace",
      "description": "Default workspace",
      "isDefault": true,
      "ownerId": "123e4567-e89b-12d3-a456-426614174000",
      "createdAt": "2025-10-01T10:00:00Z",
      "updatedAt": "2025-10-01T10:00:00Z"
    }
  ],
  "meta": {
    "total": 1,
    "hasMore": false
  }
}
```

### GetWorkspaceResponse

```typescript
interface GetWorkspaceResponse {
  data: Workspace;
}
```

**Example Response**:

```json
{
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "My First Workspace",
    "description": "Default workspace",
    "isDefault": true,
    "ownerId": "123e4567-e89b-12d3-a456-426614174000",
    "createdAt": "2025-10-01T10:00:00Z",
    "updatedAt": "2025-10-01T10:00:00Z"
  }
}
```

### ErrorResponse

```typescript
interface ErrorResponse {
  error: {
    code: string;
    message: string;
    errors?: Array<{
      field: string;
      message: string;
    }>;
  };
}
```

**Example Error Response**:

```json
{
  "error": {
    "code": "workspace_not_found",
    "message": "Workspace not found or you don't have access"
  }
}
```

## 5. Local Storage Schema

### Last Selected Workspace

**Key**: `loomify:workspace:lastSelected`

**Value**:

```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "lastSelectedWorkspaceId": "550e8400-e29b-41d4-a716-446655440000",
  "selectedAt": "2025-10-28T14:30:00Z"
}
```

**Type Definition**:

```typescript
interface StoredWorkspacePreference {
  userId: string;
  lastSelectedWorkspaceId: string | null;
  selectedAt: string | null; // ISO 8601 string
}
```

## 6. Domain Services

### WorkspaceSelectionService

Encapsulates workspace selection business logic.

**Responsibilities**:

- Determine which workspace to load on login
- Validate workspace selection
- Handle fallback logic (last selected → default → first available)

**Methods**:

```typescript
interface IWorkspaceSelectionService {
  /**
   * Determines which workspace should be loaded on login.
   * Priority: Last selected (if valid) → Default → First available
   */
  determineWorkspaceToLoad(
    workspaces: Workspace[],
    lastSelectedId: string | null
  ): Workspace | null;

  /**
   * Validates if a workspace ID exists in the available workspaces.
   */
  isWorkspaceValid(
    workspaceId: string,
    workspaces: Workspace[]
  ): boolean;

  /**
   * Finds the default workspace from a list.
   */
  findDefaultWorkspace(workspaces: Workspace[]): Workspace | undefined;

  /**
   * Handles the case when no workspaces are available.
   */
  handleNoWorkspaces(): WorkspaceError;
}
```

**Business Logic**:

```typescript
function determineWorkspaceToLoad(
  workspaces: Workspace[],
  lastSelectedId: string | null
): Workspace | null {
  if (workspaces.length === 0) {
    return null;
  }

  // Priority 1: Last selected (if valid)
  if (lastSelectedId) {
    const lastSelected = workspaces.find(w => w.id === lastSelectedId);
    if (lastSelected) {
      return lastSelected;
    }
  }

  // Priority 2: Default workspace
  const defaultWorkspace = workspaces.find(w => w.isDefault);
  if (defaultWorkspace) {
    return defaultWorkspace;
  }

  // Priority 3: First available workspace
  return workspaces[0];
}
```

## 7. Data Transformation

### Backend to Frontend Mapping

Backend response uses snake_case (Kotlin convention), frontend uses camelCase (TypeScript convention).

**Transformation Function**:

```typescript
function transformWorkspaceFromApi(apiWorkspace: any): Workspace {
  return {
    id: apiWorkspace.id,
    name: apiWorkspace.name,
    description: apiWorkspace.description,
    isDefault: apiWorkspace.is_default ?? false,
    ownerId: apiWorkspace.owner_id,
    createdAt: new Date(apiWorkspace.created_at),
    updatedAt: new Date(apiWorkspace.updated_at),
  };
}
```

**Note**: Verify actual backend response format in Phase 1 contracts generation.

## Summary

- **3 entities**: Workspace, WorkspaceSelectionPreference, WorkspaceState
- **2 value objects**: WorkspaceId, WorkspaceName
- **1 domain service**: WorkspaceSelectionService
- **State transitions** defined for loading, switching, and error states
- **Validation rules** documented for all entities
- **API response models** defined for GET endpoints
- **Local storage schema** defined for persistence

All data models are type-safe, validated, and follow domain-driven design principles. Ready for contract generation in Phase 1.
