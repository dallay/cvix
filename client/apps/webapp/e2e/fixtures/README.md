# E2E Test Fixtures

This directory contains test fixtures and utilities for E2E tests using **HAR-based API mocking**.

## 📁 Directory Structure

```text
e2e/
├── authentication-flows.spec.ts  # Authentication E2E tests
├── config/
│   └── environment.ts            # Environment configuration
├── fixtures/
│   ├── har/                      # HTTP Archive files for mocking
│   │   ├── auth-flow-login.har   # Login flow recording
│   │   └── auth-flow-register.har # Registration flow recording
│   └── README.md                 # This file
└── helpers/
    └── auth.helper.ts            # HAR-based authentication helpers
```

---

## 🎯 Testing Philosophy

### Offline-First Testing

**E2E tests run WITHOUT a real backend server** by using pre-recorded HAR files. This enables:

- ✅ **Fast execution** - No network latency, no server startup time
- ✅ **Reliable CI** - No backend infrastructure required
- ✅ **Predictable data** - HAR responses are deterministic
- ✅ **Easy error testing** - Mock any error scenario with custom routes

### HAR Files

HAR (HTTP Archive) files contain recorded API request/response pairs from real interactions:

| File | Description | Test User |
|------|-------------|-----------|
| `auth-flow-login.har` | Login flow for existing user | `john.doe@cvix.com` |
| `auth-flow-register.har` | Registration flow | `jane.doe@cvix.com` |

---

## 🚀 Quick Start

### Basic Login Test

```typescript
import { test, expect } from '@playwright/test';
import { setupLoginMocks, TEST_USERS } from './helpers/auth.helper';

test.describe('Login Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Set up HAR-based API mocking
    await setupLoginMocks(page);
  });

  test('should login successfully', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel(/email/i).fill(TEST_USERS.existingUser.email);
    await page.getByLabel(/password/i).fill(TEST_USERS.existingUser.password);
    await page.getByRole('button', { name: /log in/i }).click();

    await expect(page).toHaveURL(/\/dashboard/);
  });
});
```

### Testing Error Scenarios

```typescript
import { mockApiErrors, setupLoginMocks } from './helpers/auth.helper';

test('should show error for invalid credentials', async ({ page }) => {
  // First set up base mocks, then override with error
  await setupLoginMocks(page);
  await mockApiErrors.invalidCredentials(page);

  await page.goto('/login');
  await page.getByLabel(/email/i).fill('wrong@example.com');
  await page.getByLabel(/password/i).fill('WrongPassword!');
  await page.getByRole('button', { name: /log in/i }).click();

  await expect(page.getByText(/invalid.*credentials/i)).toBeVisible();
});
```

---

## 📦 Auth Helper API

### Setup Functions

| Function | Description |
|----------|-------------|
| `setupLoginMocks(page)` | Set up HAR-based mocking for login flow |
| `setupRegisterMocks(page)` | Set up HAR-based mocking for registration flow |

### Test Users

```typescript
import { TEST_USERS } from './helpers/auth.helper';

// Existing user (for login tests)
TEST_USERS.existingUser.email     // "john.doe@cvix.com"
TEST_USERS.existingUser.password  // "S3cr3tP@ssw0rd*123"

// New user (for registration tests)
TEST_USERS.newUser.email          // "jane.doe@cvix.com"
TEST_USERS.newUser.password       // "S3cr3tP@ssw0rd*123"
```

### Error Mocks

```typescript
import { mockApiErrors } from './helpers/auth.helper';

// Invalid login credentials
await mockApiErrors.invalidCredentials(page);

// Rate limit exceeded
await mockApiErrors.rateLimitExceeded(page);

// Network error
await mockApiErrors.networkError(page);

// Email already registered
await mockApiErrors.emailAlreadyExists(page);
```

### UI Helpers

```typescript
import { loginViaUI, registerViaUI } from './helpers/auth.helper';

// Perform login through UI
await loginViaUI(page);  // Uses TEST_USERS.existingUser

// Perform registration through UI
await registerViaUI(page);  // Uses TEST_USERS.newUser
```

---

## 🔧 How HAR Mocking Works

1. **Recording**: HAR files were created by running tests against a real backend with `update: true`
2. **Replaying**: `page.routeFromHAR()` intercepts matching requests and returns recorded responses
3. **Filtering**: Only `/api/**` requests are mocked; Vite dev server requests pass through normally

```typescript
// Under the hood, setupLoginMocks does this:
await page.routeFromHAR('fixtures/har/auth-flow-login.har', {
  url: '**/api/**',      // Only mock API calls
  notFound: 'fallback',  // Let other requests through
});
```

---

## 📝 Updating HAR Files

When the API changes, you need to re-record the HAR files:

1. **Start the backend** and all dependencies (Keycloak, PostgreSQL)
2. **Modify the helper** to use `update: true`:
   ```typescript
   await page.routeFromHAR('fixtures/har/auth-flow-login.har', {
     url: '**/api/**',
     update: true,  // Record new responses
   });
   ```
3. **Run the tests** against the real backend
4. **Revert** the helper to `update: false` (or remove the option)
5. **Commit** the updated HAR files

---

## 🛠️ Adding New HAR Files

1. Create a new test that exercises the flow you want to record
2. Use `page.routeFromHAR()` with `update: true` pointing to a new file
3. Run the test against the real backend
4. Create a helper function in `auth.helper.ts` (or a new helper file)
5. Set `update: false` and commit the HAR file

---

## 🆕 Creating a New HAR Archive for E2E Tests

When you need to add a new E2E test that requires a new HAR file (for a new API flow or endpoint), follow these steps:

1. **Start the backend and all dependencies** (Keycloak, PostgreSQL, etc.) so the API is available at the expected URL (e.g., `https://localhost:9876`).
2. **Open Playwright in record mode to capture API requests**. Use the following command, replacing the HAR file path and URL as needed:

   ```sh
   npx playwright open \
     --save-har=client/apps/webapp/e2e/fixtures/har/example.har \
     --save-har-glob="**/api/**" \
     https://localhost:9876
   ```

   - This will launch a browser window. Interact with the app to exercise the flow you want to record.
   - All API requests matching `**/api/**` will be saved to the specified HAR file.

3. **Complete the flow in the browser** (e.g., registration, login, etc.).
4. **Close the browser**. The HAR file will be written to the path you specified.
5. **Rename the HAR file** if needed and move it to `client/apps/webapp/e2e/fixtures/har/`.
6. **Update or create a helper** in `helpers/` to use your new HAR file for mocking in tests.
7. **Commit the new HAR file** to version control. **Never commit HAR files with real/production credentials.**

**Example:**

To record a registration flow and save it as `auth-flow-register.har`:

```sh
npx playwright open \
  --save-har=client/apps/webapp/e2e/fixtures/har/auth-flow-register.har \
  --save-har-glob="**/api/**" \
  https://localhost:9876
```

---

## ⚠️ Important Notes

### HAR File Matching

- HAR matching is **strict** on URL and HTTP method
- For POST requests, it also matches the request body
- If no match is found and `notFound: 'fallback'`, the request passes through
- If no match is found and `notFound: 'abort'` (default), the request fails

### Test Isolation

- Each test should set up its own mocks in `beforeEach`
- Mocks are automatically cleared when the page closes
- Override specific routes after setting up HAR mocks for error scenarios

### Credentials in HAR Files

- HAR files contain test credentials - this is intentional
- Never commit HAR files with real/production credentials
- The test passwords in HAR files should match `TEST_USERS` constants

---

## 📚 Further Reading

- [Playwright Mock APIs - HAR](https://playwright.dev/docs/mock#mocking-with-har-files)
- [HAR 1.2 Specification](http://www.softwareishard.com/blog/har-12-spec)
- [Playwright Test Fixtures](https://playwright.dev/docs/test-fixtures)
