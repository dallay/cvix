# Research: Workspace Selection Implementation

**Feature**: Workspace Selection Implementation
**Branch**: `002-workspace-selection`
**Date**: 2025-10-28

## Purpose

This document consolidates research findings for implementing workspace selection functionality in the frontend, including technology decisions, integration patterns, and best practices.

## 1. Backend API Integration

### Decision: Use Existing Workspace Endpoints

**Endpoints Identified**:

- `GET /api/workspace` - Retrieve all workspaces for authenticated user
- `GET /api/workspace/{id}` - Retrieve specific workspace details
- `POST /api/workspace` - Create workspace (not needed for this feature)
- `PUT /api/workspace/{id}` - Update workspace (not needed for this feature)
- `DELETE /api/workspace/{id}` - Delete workspace (not needed for this feature)

**Rationale**:

- Backend workspace API is fully implemented with Hexagonal Architecture
- Authentication handled via Keycloak OAuth2/OIDC (JWT tokens)
- Backend returns only workspaces user has access to (authorization enforced)
- API follows REST principles with versioned media types (`application/vnd.api.v1+json`)

**Integration Pattern**:

```typescript
// HTTP client wrapper using fetch API
interface WorkspaceApiClient {
  getAllWorkspaces(): Promise<Workspace[]>;
  getWorkspace(id: string): Promise<Workspace | null>;
}
```

### Alternatives Considered

- **GraphQL**: Rejected - REST API already implemented and sufficient
- **WebSocket**: Rejected - Real-time updates not required for workspace selection
- **Server-Sent Events**: Rejected - Polling/refetching on demand is adequate

## 2. State Management Strategy

### Decision: Pinia for Global Workspace State

**Rationale**:

- Pinia is the official Vue state management library (replacing Vuex)
- Type-safe with TypeScript
- Composition API style aligns with Vue 3 best practices
- DevTools support for debugging
- Already used in the project

**Store Structure**:

```typescript
interface WorkspaceState {
  workspaces: Workspace[];          // All available workspaces
  currentWorkspace: Workspace | null; // Selected workspace
  lastSelectedId: string | null;    // Persisted in local storage
  isLoading: boolean;               // Loading indicator state
  error: string | null;             // Error message
}
```

**Getters**:

- `hasWorkspaces`: boolean - Check if user has any workspaces
- `defaultWorkspace`: Workspace | undefined - Find workspace marked as default
- `workspaceCount`: number - Total number of workspaces

**Actions**:

- `loadWorkspaces()`: Fetch workspaces from API
- `selectWorkspace(id: string)`: Set current workspace and persist selection
- `loadLastSelected()`: Load last selected workspace from local storage
- `clearWorkspaceState()`: Reset state on logout

### Alternatives Considered

- **Vue provide/inject**: Rejected - Not sufficient for complex state management
- **Composable-only state**: Rejected - Loses state on component unmount
- **Vuex**: Rejected - Deprecated in favor of Pinia

## 3. Persistence Strategy

### Decision: Browser Local Storage for Last Selected Workspace

**Rationale**:

- Simple, built-in browser API
- Persists across browser sessions
- No server-side storage needed for this feature
- Synchronous access (no async overhead)
- 5-10MB storage limit is more than sufficient for workspace ID

**Storage Key Structure**:

```typescript
const STORAGE_KEYS = {
  LAST_SELECTED_WORKSPACE: 'loomify:workspace:lastSelected',
  WORKSPACE_CACHE: 'loomify:workspace:cache', // Optional: cache workspace list
} as const;
```

**Data Stored**:

- Workspace ID (UUID) only, not full workspace data
- Timestamp of last selection (for cache invalidation if needed)

**Security Considerations**:

- No sensitive data stored (workspace ID is not secret)
- No auth tokens stored in local storage (handled by auth system)
- Validate stored workspace ID before using (check UUID format)

### Alternatives Considered

- **Session Storage**: Rejected - Lost on browser close, bad UX
- **IndexedDB**: Rejected - Overkill for single key-value pair
- **Cookies**: Rejected - Sent with every request (unnecessary overhead)
- **Backend User Preferences API**: Future enhancement - local storage sufficient for MVP

## 4. Loading and Error Handling Patterns

### Decision: Optimistic UI with Graceful Degradation

**Loading States**:

- **Initial Load**: Skeleton loader for workspace list (Shadcn-Vue Skeleton component)
- **Switching**: Inline spinner + disable interactions during switch
- **Background Refresh**: Silent loading (no UI block) with stale-while-revalidate pattern

**Error Handling**:

- **Network Errors**: Retry logic (3 attempts with exponential backoff)
- **API Errors**: Parse error response and show user-friendly message
- **Missing Default Workspace**: Fallback to first available workspace
- **Empty Workspace List**: Special empty state UI with support contact

**Toast Notifications**:

```typescript
interface ToastMessage {
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  action?: { label: string; onClick: () => void };
}
```

Examples:

- Success: "Workspace '{name}' loaded successfully"
- Error: "Failed to load workspaces. [Retry]"
- Warning: "Your last workspace is unavailable. Loaded default workspace."

### Alternatives Considered

- **Modal Dialogs for Errors**: Rejected - Too intrusive, blocks user flow
- **Inline Error Messages**: Rejected - Easy to miss, no action buttons
- **Console-only Errors**: Rejected - Poor UX, users won't see issues

## 5. Component Architecture

### Decision: Compound Component Pattern with Shadcn-Vue

**Component Hierarchy**:

```text
WorkspaceSelector (Container)
├── WorkspaceLoadingState (Skeleton)
├── WorkspaceErrorState (Error message + retry)
└── WorkspaceSelectorList
    └── WorkspaceSelectorItem (x N)
```

**Rationale**:

- Separation of concerns (container vs presentational)
- Reusable sub-components
- Easy to test in isolation
- Follows Shadcn-Vue patterns (Select, Dialog, Button components)

**Props vs Composables**:

- **Props**: For presentational components (data, callbacks)
- **Composables**: For container components (business logic, state management)

### Alternatives Considered

- **Single Monolithic Component**: Rejected - Hard to test, poor reusability
- **Renderless Components**: Rejected - Adds complexity without clear benefit
- **Custom Headless UI**: Rejected - Shadcn-Vue already provides accessible components

## 6. Auto-Load on Login Implementation

### Decision: Router Navigation Guard + Pinia Action

**Implementation Flow**:

1. User logs in successfully (handled by auth system)
2. Vue Router navigation guard intercepts route change
3. Guard checks if workspace is already loaded
4. If not loaded: Call Pinia action `loadLastSelectedWorkspace()`
5. Action checks local storage for last selected workspace ID
6. If found and valid: Load that workspace
7. If not found: Fetch default workspace from API
8. If default not found: Load first available workspace
9. Navigate to dashboard with workspace context

**Navigation Guard**:

```typescript
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  const workspaceStore = useWorkspaceStore();

  if (authStore.isAuthenticated && !workspaceStore.currentWorkspace) {
    await workspaceStore.loadLastSelectedWorkspace();
  }

  next();
});
```

### Alternatives Considered

- **App.vue onMounted**: Rejected - Runs too late, flicker on load
- **Middleware Pattern**: Rejected - Vue Router guards are the standard
- **Separate "Workspace Selection" Page**: Rejected - Bad UX, extra step

## 7. Performance Optimization

### Decision: Memory Cache + Lazy Loading

**Caching Strategy**:

- Cache workspace list in Pinia store after first load
- Time-to-live (TTL): 5 minutes
- Invalidate cache on workspace switch or manual refresh
- No disk caching (local storage only for last selected ID)

**Lazy Loading**:

- Workspace-dependent components use `defineAsyncComponent()`
- Code-split workspace feature into separate chunk
- Target: <10KB gzipped for workspace selection code

**Debouncing**:

- Debounce workspace switch action (300ms) to prevent rapid switches
- Cancel in-flight API requests on new workspace selection

### Alternatives Considered

- **Aggressive Caching (1 hour)**: Rejected - Stale data risk
- **No Caching**: Rejected - Unnecessary API calls on every component mount
- **Service Worker Caching**: Rejected - Overkill for this feature

## 8. Accessibility Implementation

### Decision: Shadcn-Vue Components + Custom ARIA Labels

**Keyboard Navigation**:

- `Tab`: Focus workspace selector trigger
- `Enter`/`Space`: Open workspace list
- `Arrow Up/Down`: Navigate workspace list
- `Escape`: Close workspace list
- `Enter` on item: Select workspace

**Screen Reader Support**:

- `role="combobox"` on selector trigger
- `aria-expanded` to indicate open/closed state
- `aria-label` with current workspace name
- `aria-live="polite"` for loading/error announcements
- `aria-describedby` for error messages

**Focus Management**:

- Trap focus inside workspace selector when open
- Return focus to trigger on close
- Announce workspace changes to screen readers

### Alternatives Considered

- **Custom Accessible Component**: Rejected - Shadcn-Vue already WCAG AA compliant
- **ARIA Landmarks Only**: Rejected - Insufficient for complex interactions

## 9. Internationalization

### Decision: vue-i18n with Domain-Specific Keys

**Translation Key Structure**:

```text
workspace.selector.title = "Select Workspace"
workspace.selector.loading = "Loading workspaces..."
workspace.selector.error = "Failed to load workspaces"
workspace.selector.empty = "No workspaces available"
workspace.selector.retry = "Retry"
workspace.selected = "Workspace '{name}' selected"
workspace.fallback = "Your last workspace is unavailable. Using default."
```

**RTL Support**:

- Use Tailwind logical properties (`ms-` instead of `ml-`, etc.)
- Test with `dir="rtl"` attribute
- Mirror icons where appropriate (dropdown arrows, etc.)

### Alternatives Considered

- **Hardcoded English Strings**: Rejected - Not compliant with UX principle V
- **Backend-Provided Translations**: Rejected - Frontend i18n is standard

## 10. Testing Strategy

### Decision: Three-Layer Test Pyramid

**Unit Tests (70% coverage target)**:

- Domain services (workspace selection logic)
- Composables (useWorkspaceSelection, useWorkspaceLoader)
- API client (mock fetch responses)
- Local storage adapter (mock localStorage)
- Utility functions

**Integration Tests (20% coverage target)**:

- Pinia store + API client integration
- Components + store integration
- Router guards + store integration

**E2E Tests (10% coverage target)**:

- Login → Auto-load last selected workspace
- Login → Auto-load default workspace (first-time user)
- Manual workspace selection
- Error handling (API failure, empty workspace list)

**Tools**:

- Vitest for unit/integration tests
- @testing-library/vue for component tests
- Playwright for E2E tests
- Faker.js for test data generation

### Alternatives Considered

- **Cypress Instead of Playwright**: Rejected - Playwright is faster and already in use
- **Jest Instead of Vitest**: Rejected - Vitest is faster and Vite-native
- **Snapshot Tests**: Rejected - Fragile, test behavior not snapshots

## Research Summary

All technical decisions have been made with clear rationale. No blockers identified. Ready to proceed to Phase 1 (Design & Contracts).

**Key Dependencies Confirmed**:

- ✅ Backend workspace API endpoints exist and are functional
- ✅ Authentication system (Keycloak) integrated
- ✅ Pinia store pattern established in project
- ✅ Shadcn-Vue component library available
- ✅ Testing infrastructure (Vitest, Playwright) configured

**Next Steps**: Generate data model and API contracts in Phase 1.
