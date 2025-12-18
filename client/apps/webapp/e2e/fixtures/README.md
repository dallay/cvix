# E2E Test Fixtures

This directory contains test fixtures and utilities for E2E tests with **two testing modes**: offline-first with mocks (recommended) and integration with real backend.

## 📁 Directory Structure

```text
fixtures/
├── auth-fixtures.ts          # Authentication utilities with mocking (✅ no backend needed)
├── workspace-fixtures.ts      # Workspace API fixtures (⚠️ requires backend)
├── mocks/                     # Mock data generators and route handlers
│   └── workspace-mocks.ts     # Workspace mock data and API routes
├── har/                       # HTTP Archive files for recording/replaying traffic
└── README.md                  # This file
```

---

## 🎯 Testing Philosophy

### Offline-First Testing (Recommended)

**E2E tests should NOT require a running backend server** by default. This enables:

- ✅ **Faster test execution** - No network latency, no server startup time
- ✅ **Reliable tests in CI** - No complex infrastructure setup needed
- ✅ **Predictable test data** - Mocks are deterministic and controllable
- ✅ **Easy error testing** - Simulate any error scenario without backend changes

### When to Use What

| Scenario                        | Use                                     |
|---------------------------------|-----------------------------------------|
| **Normal test flow**            | Mock fixtures (`auth-fixtures.ts`, etc) |
| **Testing error scenarios**     | Mock fixtures with error handlers       |
| **Integration with real API**   | `workspace-fixtures.ts` (backend required) |
| **Recording real API traffic**  | HAR files in `har/` directory           |

---

## Overview

The fixtures system provides:

- **Auth Test Fixtures** (NEW): Mock authentication without backend - no server required
- **Workspace Mock Data** (NEW): Mock workspace API responses - no server required
- **Workspace Test Fixtures**: API-based setup/teardown - requires backend running
- **Centralized Configuration**: Environment config for all test URLs and credentials
- **User Management**: Automatic user creation and validation
- **Keycloak Integration**: Real authentication using Keycloak OAuth2
- **Deterministic State**: Clean state before each test to prevent flakiness

---

## 🚀 Quick Start (Offline-First with Mocks)

### Basic Authentication Test

```typescript
import { test, expect } from '@playwright/test';
import { AuthTestFixtures } from './fixtures/auth-fixtures';

test('should login successfully', async ({ page }) => {
  // Set up mock authentication routes
  await AuthTestFixtures.setupMockAuth(page);

  // Perform login
  await AuthTestFixtures.loginWithUI(page);

  // Verify authentication
  await expect(page).toHaveURL(/\/dashboard/);
  expect(await AuthTestFixtures.verifyAuthenticated(page)).toBe(true);
});
```

### Workspace Test with Mocks

```typescript
import { test, expect } from '@playwright/test';
import { AuthTestFixtures } from './fixtures/auth-fixtures';
import { WorkspaceMockData } from './fixtures/mocks/workspace-mocks';

test('should load default workspace', async ({ page }) => {
  // Set up mocks
  await AuthTestFixtures.setupMockAuth(page);
  WorkspaceMockData.createDefaultWorkspaces();
  WorkspaceMockData.setupWorkspaceRoutes(page.route.bind(page));

  // Login and navigate
  await AuthTestFixtures.setAuthState(page);
  await page.goto('/dashboard');

  // Verify default workspace loaded
  const workspaceIndicator = page.locator('[data-testid="workspace-indicator"]');
  await expect(workspaceIndicator).toContainText('Default Workspace');
});
```

---

## 📦 Auth Test Fixtures (Offline-First)

The `AuthTestFixtures` class provides mock authentication **without requiring a backend server**.

### Available Methods

| Method                        | Description                                     |
|-------------------------------|-------------------------------------------------|
| `setupMockAuth(page)`         | Set up mock authentication routes               |
| `setupMockAuthFailure(page)`  | Mock authentication failure                     |
| `setupMockNetworkError(page)` | Mock network errors                             |
| `setupMockRateLimitError(page)` | Mock rate limiting                            |
| `loginWithUI(page, credentials?)` | Perform UI login                            |
| `setAuthState(page, userId?)` | Set authentication state directly in storage    |
| `clearAuthState(page)`        | Clear authentication state                      |
| `verifyAuthenticated(page)`   | Check if user is authenticated                  |
| `createMockStorageState()`    | Create reusable storage state for test context  |

### Usage Examples

#### Testing Error Scenarios

```typescript
import { test, expect } from '@playwright/test';
import { AuthTestFixtures } from './fixtures/auth-fixtures';

test('should show error for invalid credentials', async ({ page }) => {
  // Set up mock authentication failure
  await AuthTestFixtures.setupMockAuthFailure(page);

  await AuthTestFixtures.loginWithUI(page);

  await expect(page.getByText(/invalid.*credentials/i)).toBeVisible();
});

test('should handle rate limiting', async ({ page }) => {
  await AuthTestFixtures.setupMockRateLimitError(page);

  // Attempt login 6 times
  for (let i = 0; i < 6; i++) {
    await AuthTestFixtures.loginWithUI(page);
  }

  await expect(page.getByText(/too many.*attempts/i)).toBeVisible();
});
```

#### Skip Login in Every Test

```typescript
import { test as base } from '@playwright/test';
import { AuthTestFixtures } from './fixtures/auth-fixtures';

// Extend test fixture with authenticated context
const test = base.extend({
  authenticatedPage: async ({ page }, use) => {
    await AuthTestFixtures.setupMockAuth(page);
    await AuthTestFixtures.setAuthState(page);
    await page.goto('/dashboard');
    await use(page);
  },
});

test('should see workspace selector when authenticated', async ({ authenticatedPage }) => {
  const workspaceSelector = authenticatedPage.locator('[data-testid="workspace-selector"]');
  await expect(workspaceSelector).toBeVisible();
});
```

---

## 📦 Workspace Mock Data (Offline-First)

The `WorkspaceMockData` class provides mock workspace API responses **without requiring a backend server**.

### Available Methods

| Method                               | Description                              |
|--------------------------------------|------------------------------------------|
| `createDefaultWorkspaces(userId?)`   | Create mock workspaces with one default  |
| `createNonDefaultWorkspaces(userId?)` | Create non-default workspaces only      |
| `getAllWorkspaces()`                 | Get all mock workspaces                  |
| `getWorkspaceById(id)`               | Get specific workspace                   |
| `addWorkspace(workspace)`            | Add workspace to mock store              |
| `removeWorkspace(id)`                | Remove workspace from mock store         |
| `clearWorkspaces()`                  | Clear all workspaces                     |
| `setupWorkspaceRoutes(routeHandler)` | Set up API route handlers                |

### Usage Example

```typescript
import { test, expect } from '@playwright/test';
import { AuthTestFixtures } from './fixtures/auth-fixtures';
import { WorkspaceMockData } from './fixtures/mocks/workspace-mocks';

test.beforeEach(async ({ page }) => {
  // Clear previous state
  WorkspaceMockData.clearWorkspaces();
  await AuthTestFixtures.clearAuthState(page);
});

test('should switch between workspaces', async ({ page }) => {
  await AuthTestFixtures.setupMockAuth(page);
  WorkspaceMockData.createDefaultWorkspaces();
  WorkspaceMockData.setupWorkspaceRoutes(page.route.bind(page));

  await AuthTestFixtures.setAuthState(page);
  await page.goto('/dashboard');

  // Open workspace selector
  await page.locator('[data-testid="workspace-selector"]').click();

  // Select different workspace
  await page.getByRole('option', { name: /project alpha/i }).click();

  // Verify switch
  const workspaceIndicator = page.locator('[data-testid="workspace-indicator"]');
  await expect(workspaceIndicator).toContainText('Project Alpha');
});
```

---

## Workspace Test Fixtures (Requires Backend)

The `WorkspaceTestFixtures` class provides utilities for managing workspace test data via API calls with Keycloak authentication.

### Usage Example

```typescript
import { test, expect } from "@playwright/test";
import { getTestUser } from "./config/environment";
import { WorkspaceTestFixtures } from "./fixtures/workspace-fixtures";

test("my workspace test", async ({ page, request }) => {
  const fixtures = new WorkspaceTestFixtures(request);
  const testUser = getTestUser("noDefault");

  // Setup: Create test data
  await test.step("Setup test data", async () => {
    const workspaces = await fixtures.setupUserWithNonDefaultWorkspaces(
      testUser.email,
      testUser.password,
      testUser.id,
      ["Workspace 1", "Workspace 2"]
    );

    expect(workspaces.length).toBe(2);
  });

  // Your test steps here...

  // Cleanup: Remove test data
  await test.step("Cleanup", async () => {
    await fixtures.cleanup(testUser.email, testUser.password);
  });
});
```

### Available Methods

#### `checkApiAvailability()`

Check if the backend API is accessible before running tests.

**Returns:** `Promise<boolean>` - True if API is available

**Example:**

```typescript
const isAvailable = await fixtures.checkApiAvailability();
if (!isAvailable) {
  console.warn("API not available, tests may fail");
}
```

#### `ensureUserExists(email, password, userId)`

Ensures a test user exists in the system. Creates the user if they don't exist, or verifies authentication if they do.

**Parameters:**

- `email` (string): User email
- `password` (string): User password
- `userId` (string, optional): User UUID

**Returns:** `Promise<UserFixture>` - User data

#### `setupUserWithNonDefaultWorkspaces(email, password, userId, workspaceNames)`

Creates multiple non-default workspaces for a test user. Automatically ensures user exists and cleans up existing workspaces first.

**Parameters:**

- `email` (string): User email for authentication
- `password` (string): User password
- `userId` (string): User UUID
- `workspaceNames` (string[]): Array of workspace names to create (default: ["Workspace Alpha", "Workspace Beta"])

**Returns:** `Promise<WorkspaceFixture[]>` - Array of created workspaces

#### `setupUserWithDefaultWorkspace(email, password, userId, workspaceName)`

Creates a single default workspace for a test user. Automatically ensures user exists and cleans up existing workspaces first.

**Parameters:**

- `email` (string): User email for authentication
- `password` (string): User password
- `userId` (string): User UUID
- `workspaceName` (string): Name for the default workspace (default: "Default Workspace")

**Returns:** `Promise<WorkspaceFixture>` - The created workspace

#### `setupUserWithNoWorkspaces(email, password, userId)`

Sets up a user with no workspaces. Useful for testing the "no workspaces available" error scenario.

**Parameters:**

- `email` (string): User email
- `password` (string): User password
- `userId` (string): User UUID

**Returns:** `Promise<void>`

#### `cleanup(email, password)`

Deletes all workspaces for a user. Use this in `afterEach` or at the end of test steps to clean up test data.

**Parameters:**

- `email` (string): User email
- `password` (string): User password

**Returns:** `Promise<void>`

## Environment Configuration

Test environment settings are centralized in `config/environment.ts`. Use the `getTestUser()` helper to get predefined test user credentials:

```typescript
import { getTestUser } from "./config/environment";

const testUser = getTestUser("noDefault"); // or "default", "noWorkspace", "newUser"
// testUser contains: { id, email, password }
```

### Available Test Users

- **`default`**: User with a default workspace (<test@example.com>)
- **`noDefault`**: User with only non-default workspaces (<nodefault@example.com>)
- **`noWorkspace`**: User with no workspaces (<noworkspace@example.com>)
- **`newUser`**: New user with no history (<newuser@example.com>)

### Environment Variables

The fixtures use the following environment variables:

- `API_BASE_URL`: Base URL for API calls (default: `http://localhost:8080/api`)
- `KEYCLOAK_URL`: Keycloak realm URL (default: `http://localhost:9080/realms/cvix`)
- `KEYCLOAK_CLIENT_ID`: Keycloak client ID (default: `cvix-client`)

You can set these in your Playwright configuration:

```typescript
// playwright.config.ts
use: {
  baseURL: 'http://localhost:9876',
}

// Set environment variables
process.env.API_BASE_URL = 'http://localhost:8080/api';
process.env.KEYCLOAK_URL = 'http://localhost:9080/realms/cvix';
```

### Best Practices (Backend-based fixtures)

1. **Always clean up**: Use the `cleanup()` method in a test step or afterEach hook to prevent test pollution
2. **Use unique emails**: Each test should use a distinct test user email to avoid conflicts
3. **Handle errors gracefully**: Wrap setup calls in try-catch blocks, as shown in the examples
4. **Verify setup**: Assert that the expected data was created before running test assertions
5. **Deterministic state**: Always start with a clean slate by calling cleanup or setup methods that do so

### Troubleshooting

**Authentication fails:**

- Ensure the test user exists in your test database
- Verify the API_BASE_URL is correct
- Check that the auth endpoint matches your implementation

**Workspaces not created:**

- Verify the backend API is running and accessible
- Check API endpoint paths match your backend routes
- Review backend logs for error details

**Cleanup fails:**

- This is usually non-critical and logged as a warning
- May occur if the user doesn't exist or was already cleaned up
- Check if your backend requires special permissions for deletion

---

## 🛠️ Migration Guide

### Old Pattern (Backend Required)

```typescript
// ❌ Old - requires backend running
test('should login', async ({ page, request }) => {
  const fixtures = new WorkspaceTestFixtures(request);
  const token = await fixtures.getAuthToken(email, password);

  await page.goto('/login');
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: /log in/i }).click();

  await expect(page).toHaveURL(/\/dashboard/);
});
```

### New Pattern (Mocked)

```typescript
// ✅ New - no backend needed
import { AuthTestFixtures } from './fixtures/auth-fixtures';

test('should login', async ({ page }) => {
  await AuthTestFixtures.setupMockAuth(page);
  await AuthTestFixtures.loginWithUI(page);

  await expect(page).toHaveURL(/\/dashboard/);
});
```

---

## 🎯 Best Practices (General)

### 1. **Default to Mocks**

Always use mocks unless you're explicitly testing API integration.

```typescript
// ✅ Good - uses mocks, no backend needed
await AuthTestFixtures.setupMockAuth(page);

// ❌ Avoid - requires backend running
const token = await fixtures.getAuthToken(email, password);
```

### 2. **Clean Up State**

Always clean up mock state between tests.

```typescript
test.beforeEach(async ({ page }) => {
  // Clear previous state
  WorkspaceMockData.clearWorkspaces();
  await AuthTestFixtures.clearAuthState(page);
});
```

### 3. **Use Fixtures for Reusability**

Extend Playwright's test fixture for common setups.

```typescript
import { test as base } from '@playwright/test';
import { AuthTestFixtures } from './fixtures/auth-fixtures';

export const test = base.extend({
  authenticatedPage: async ({ page }, use) => {
    await AuthTestFixtures.setupMockAuth(page);
    await AuthTestFixtures.setAuthState(page);
    await use(page);
  },
});
```

### 4. **Document Mock Behavior**

When creating custom mocks, document what they do and what they return.

```typescript
/**
 * Mock workspace API to return empty workspace list
 * Used for testing "no workspaces available" scenario
 */
static setupEmptyWorkspaceResponse(page: Page): void {
  page.route('**/api/workspace', route => {
    route.fulfill({
      status: 200,
      body: JSON.stringify({ data: [] }),
    });
  });
}
```

---

## 📚 Further Reading

- [Playwright Mocking Guide](https://playwright.dev/docs/mock)
- [HAR Files Documentation](https://playwright.dev/docs/api/class-browser#browser-new-context-option-record-har)
- [Playwright Test Fixtures](https://playwright.dev/docs/test-fixtures)
