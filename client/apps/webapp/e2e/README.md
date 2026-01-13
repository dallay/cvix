# E2E Testing Guide

Este proyecto sigue el patrón **Page Object Model (POM)** para tests E2E con Playwright.

## Quick Start

```bash
# Run all E2E tests
pnpm test:e2e

# Run specific feature
pnpm test:e2e e2e/login/
pnpm test:e2e e2e/register/

# Run by tag
npx playwright test --grep "@critical"
npx playwright test --grep "@login"

# Interactive UI mode
npx playwright test --ui

# Debug mode
npx playwright test --debug
```

## Project Structure

```markdown
e2e/
├── base-page.ts              # Parent class for ALL pages
├── helpers.ts                # Shared utilities, mocking, data generation
├── login/
│   ├── login-page.ts         # LoginPage class
│   ├── login.spec.ts         # ALL login tests
│   └── login.md              # Test documentation
├── register/
│   ├── register-page.ts      # RegisterPage class
│   ├── register.spec.ts      # ALL register tests
│   └── register.md           # Test documentation
├── logout/
│   ├── home-page.ts          # HomePage class (for logout)
│   ├── logout.spec.ts        # ALL logout tests
│   └── logout.md             # Test documentation
├── config/
│   └── environment.ts        # Environment configuration
└── fixtures/
    ├── harFixture.ts         # HAR replay (experimental)
    └── README.md             # Fixtures documentation
```

## File Naming Convention

- ✅ `login.spec.ts` (all login tests)
- ✅ `login-page.ts` (page object)
- ✅ `login.md` (documentation)
- ❌ `login-critical-path.spec.ts` (WRONG - no separate files)
- ❌ `login-validation.spec.ts` (WRONG)

## Page Object Pattern

All pages extend `BasePage`:

```typescript
import { BasePage } from "../base-page";

export class LoginPage extends BasePage {
    readonly emailInput: Locator;
    readonly passwordInput: Locator;

    constructor(page: Page) {
        super(page);
        this.emailInput = page.getByLabel(/email/i);
        this.passwordInput = page.getByLabel(/password/i);
    }

    async login(credentials: LoginCredentials): Promise<void> {
        await this.emailInput.fill(credentials.email);
        await this.passwordInput.fill(credentials.password);
        await this.submitButton.click();
    }
}
```

## Test Pattern with Tags

```typescript
test(
    "User can login with valid credentials",
    { tag: ["@critical", "@e2e", "@login", "@LOGIN-E2E-001"] },
    async ({ page }) => {
        await setupLoginMocks(page);
        const loginPage = new LoginPage(page);

        await loginPage.goto();
        await loginPage.login(TEST_USERS.existingUser);
        await loginPage.verifyLoginSuccess();
    },
);
```

## Tag Categories

- **Priority:** `@critical`, `@high`, `@medium`, `@low`
- **Type:** `@e2e`
- **Feature:** `@login`, `@register`, `@logout`
- **Special:** `@security`
- **Test ID:** `@LOGIN-E2E-001`, `@REGISTER-E2E-002`

## API Mocking

Tests use **manual API mocking** (no real backend required):

```typescript
import { setupLoginMocks, mockApiErrors, TEST_USERS } from "../helpers";

test("User can login", async ({ page }) => {
    await setupLoginMocks(page);  // Sets up all login-related mocks
    // ... test code
});

test("User sees error for invalid credentials", async ({ page }) => {
    await setupBasicMocks(page);
    await mockApiErrors.invalidCredentials(page);  // Override with error
    // ... test code
});
```

### Available Mocking Functions

| Function | Description |
|----------|-------------|
| `setupBasicMocks(page)` | Health check + unauthenticated account |
| `setupLoginMocks(page)` | Full login flow |
| `setupRegisterMocks(page)` | Full registration flow |
| `mockApiErrors.invalidCredentials(page)` | 401 error |
| `mockApiErrors.rateLimitExceeded(page)` | 429 error |
| `mockApiErrors.networkError(page)` | Network failure |
| `mockApiErrors.emailAlreadyExists(page)` | 409 conflict |

## Selector Priority (REQUIRED)

```typescript
// 1. BEST - getByRole for interactive elements
this.submitButton = page.getByRole("button", { name: "Submit" });

// 2. BEST - getByLabel for form controls
this.emailInput = page.getByLabel("Email");

// 3. SPARINGLY - getByText for static content
this.errorMessage = page.getByText("Invalid credentials");

// 4. LAST RESORT - getByTestId when above fail
this.customWidget = page.getByTestId("date-picker");

// ❌ AVOID fragile selectors
this.button = page.locator(".btn-primary");  // NO
```

## Environment Variables

| Variable | Description |
|----------|-------------|
| `USE_HAR` | Enable HAR replay mode (auto in CI) |
| `RECORD_HAR` | Record new HAR files |
| `CI` | CI environment flag |
| `FORCE_HTTP` | Force HTTP (skip HTTPS) |
| `PLAYWRIGHT_BACKEND_URL` | Backend URL override |

## Adding New Tests

1. **Check if page object exists** - reuse existing ones
2. **Create page object if needed** - extend `BasePage`
3. **Add tests to existing spec** - ONE spec file per feature
4. **Document in .md file** - follow template
5. **Add appropriate tags** - priority, feature, test ID

See `.ruler/skills/playwright/SKILL.md` for complete guidelines.
