# E2E Test Fixtures

This directory contains test fixtures and utilities for setting up deterministic test data in E2E tests.

## Overview

The fixtures system provides:

- **Workspace Test Fixtures**: API-based setup/teardown for workspace test data
- **Centralized Configuration**: Environment config for all test URLs and credentials
- **User Management**: Automatic user creation and validation
- **Keycloak Integration**: Real authentication using Keycloak OAuth2
- **Deterministic State**: Clean state before each test to prevent flakiness

## Workspace Test Fixtures

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

- **`default`**: User with a default workspace (test@example.com)
- **`noDefault`**: User with only non-default workspaces (nodefault@example.com)
- **`noWorkspace`**: User with no workspaces (noworkspace@example.com)
- **`newUser`**: New user with no history (newuser@example.com)

### Environment Variables

The fixtures use the following environment variables:

- `API_BASE_URL`: Base URL for API calls (default: `http://localhost:8080/api/v1`)
- `KEYCLOAK_URL`: Keycloak realm URL (default: `http://localhost:9080/realms/loomify`)
- `KEYCLOAK_CLIENT_ID`: Keycloak client ID (default: `loomify-client`)

You can set these in your Playwright configuration:

```typescript
// playwright.config.ts
use: {
  baseURL: 'http://localhost:9876',
}

// Set environment variables
process.env.API_BASE_URL = 'http://localhost:8080/api/v1';
process.env.KEYCLOAK_URL = 'http://localhost:9080/realms/loomify';
```

### Best Practices

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
