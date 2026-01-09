# Workspace Selection Feature - Quick Start Guide

> Get up and running with the workspace selection feature in under 5 minutes.

## Prerequisites

Before you begin, ensure you have:

- **Node.js** 18.x or higher
- **pnpm** 10.27.0 or higher
- **Backend service** running on `http://localhost:8080` (or configured endpoint)
- **Authentication** configured (Keycloak token available)

## Installation

### 1. Install Dependencies

```bash
# From project root
pnpm install
```

### 2. Verify Backend Connection

Ensure the backend workspace API is accessible:

```bash
# Test endpoint (requires authentication)
curl -H "Authorization: Bearer <your-jwt-token>" \
  http://localhost:8080/api/workspace
```

Expected response: `200 OK` with array of workspaces.

### 3. Architecture Note: BaseHttpClient Reuse

The workspace feature leverages the existing `BaseHttpClient` class from `client/apps/webapp/src/shared/BaseHttpClient.ts` for all HTTP communication. This provides:

- Automatic Bearer token injection via Axios interceptors
- CSRF token handling (automatic retry on 403 CSRF errors)
- Standardized error response parsing
- Request/response logging in development
- Configurable timeouts and retry logic

The `workspaceHttpClient` extends `BaseHttpClient` and adds workspace-specific API methods while inheriting all base functionality.

## Basic Usage

### Using the Composable

The `useWorkspaceSelection` composable provides reactive workspace state and actions:

```typescript
import { useWorkspaceSelection } from '@/workspace/application/composables/useWorkspaceSelection'

// In your component
const {
  workspaces,           // Ref<Workspace[]>
  currentWorkspace,     // Ref<Workspace | null>
  isLoading,            // Ref<boolean>
  error,                // Ref<WorkspaceError | null>

  loadWorkspaces,       // () => Promise<void>
  selectWorkspace,      // (id: string) => Promise<void>
  clearError,           // () => void
} = useWorkspaceSelection()

// Load workspaces on mount
onMounted(async () => {
  await loadWorkspaces()
})

// Switch workspace
const handleWorkspaceChange = async (workspaceId: string) => {
  await selectWorkspace(workspaceId)
}
```

### Using the Pinia Store (Direct)

If you need direct access to the store:

```typescript
import { useWorkspaceStore } from '@/workspace/infrastructure/store/workspaceStore'

const workspaceStore = useWorkspaceStore()

// Access state
console.log(workspaceStore.currentWorkspace)
console.log(workspaceStore.workspaces)

// Call actions
await workspaceStore.loadWorkspaces()
await workspaceStore.selectWorkspace('workspace-id')
```

### Using the Workspace Selector Component

The pre-built component handles all UI logic:

```vue
<script setup lang="ts">
import WorkspaceSelector from '@/workspace/presentation/components/WorkspaceSelector.vue'

const handleWorkspaceSelected = (workspace: Workspace) => {
  console.log('Switched to:', workspace.name)
  // Navigate or reload data as needed
}
</script>

<template>
  <WorkspaceSelector @workspace-selected="handleWorkspaceSelected" />
</template>
```

## Integration Steps

### 1. Register Pinia Store

The workspace store is automatically registered if using Pinia plugin auto-registration. Otherwise:

```typescript
// src/main.ts
import { createPinia } from 'pinia'
import { useWorkspaceStore } from '@/workspace/infrastructure/store/workspaceStore'

const pinia = createPinia()
app.use(pinia)

// Pre-register workspace store (optional)
useWorkspaceStore(pinia)
```

### 2. Add Auto-Load on Login

Add a navigation guard to automatically load the workspace after login:

```typescript
// The workspace feature provides a ready-made navigation guard implementation
// at `client/apps/webapp/src/workspace/infrastructure/router/workspaceGuard.ts`.
// Import and register it directly rather than re-implementing the logic.

// src/router/index.ts
import { workspaceGuard } from './guards/workspaceGuard'

// Register the guard (it will load workspace on first authenticated navigation per-session)
router.beforeEach(workspaceGuard)

// Optional: reset the per-session flag when logging out or in test teardown so the
// guard will attempt to load again on the next authenticated navigation.
// resetWorkspaceGuardSession()
```

### 3. Add Workspace Selector to Layout

Add the selector to your app's navigation or header:

```vue
<!-- src/layouts/AppLayout.vue -->
<template>
  <header>
    <nav>
      <!-- Other nav items -->
      <WorkspaceSelector />
    </nav>
  </header>
</template>
```

## Running Tests

### Unit Tests

Test domain logic and composables:

```bash
# Run all workspace unit tests
pnpm test -- workspace

# Run with coverage
pnpm test:coverage -- workspace

# Watch mode for development
pnpm test:watch -- workspace
```

### Integration Tests

Test Pinia store with mocked API:

```bash
# Run integration tests
pnpm test:integration -- workspace
```

### E2E Tests

Test full workspace selection flow:

```bash
# Run E2E tests (requires backend running)
pnpm test:e2e

# Run specific workspace E2E tests
pnpm test:e2e -- workspace-selection
```

## Configuration

### Environment Variables

Configure the backend API endpoint:

```bash
# .env.local
CVIX_API_URL=https://localhost:8443
```

### Local Storage

The feature uses local storage with the key:

```typescript
const STORAGE_KEY = 'cvix:workspace:lastSelected'
```

To clear workspace selection:

```typescript
localStorage.removeItem('cvix:workspace:lastSelected')
```

## Troubleshooting

### Workspaces Not Loading

**Issue**: `loadWorkspaces()` returns empty array.

**Solutions**:

1. Verify authentication token is valid:

   ```typescript
   console.log(localStorage.getItem('auth_token'))
   ```

2. Check backend API is running: `curl http://localhost:8080/api/workspace`
3. Verify CORS headers allow requests from your frontend origin
4. Check browser console for network errors

### Workspace Not Persisting

**Issue**: Selected workspace not remembered after page refresh.

**Solutions**:

1. Verify local storage is enabled (not in private browsing mode)
2. Check storage key is correct:

   ```typescript
   console.log(localStorage.getItem('cvix:workspace:lastSelected'))
   ```

3. Ensure `selectWorkspace()` completes successfully (no errors thrown)

### Auto-Load Not Triggering

**Issue**: Workspace doesn't auto-load after login.

**Solutions**:

1. Verify navigation guard is registered in router
2. Check guard execution order (workspace guard should run after auth guard)
3. Ensure `to.meta.requiresAuth` is set correctly for protected routes
4. Debug guard with console logs:

   ```typescript
   console.log('Workspace guard triggered', { to: to.name, from: from.name })
   ```

### Performance Issues

**Issue**: Workspace switching feels slow.

**Solutions**:

1. Check network tab for API response times (should be <500ms)
2. Verify cache is working (subsequent loads should use cached data)
3. Disable dev tools extensions that may slow down Vue reactivity
4. Profile component rendering with Vue DevTools

## Next Steps

- **Read the full spec**: `specs/002-workspace-selection/spec.md`
- **Review data model**: `specs/002-workspace-selection/data-model.md`
- **Check API contracts**: `specs/002-workspace-selection/contracts/workspace-api.yaml`
- **Explore component patterns**: See research document for compound component architecture

## API Reference

See `contracts/workspace-api.yaml` for complete OpenAPI specification.

### Quick API Overview

#### Get All Workspaces

```http
GET /api/workspace
Authorization: Bearer <jwt-token>
Accept: application/vnd.api.v1+json
```

#### Get Workspace by ID

```http
GET /api/workspace/{workspaceId}
Authorization: Bearer <jwt-token>
Accept: application/vnd.api.v1+json
```

## Support

For questions or issues:

1. Check the troubleshooting section above
2. Review the feature specification in `spec.md`
3. Contact the team via Slack #workspace-feature channel
