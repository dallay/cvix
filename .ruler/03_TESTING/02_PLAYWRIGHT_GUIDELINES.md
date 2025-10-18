# Playwright & E2E Testing Guidelines

> This guide provides instructions for writing End-to-End (E2E) tests using Playwright and TypeScript.

## Guiding Principles

- **User-Facing Locators**: Prioritize user-facing, role-based locators (`getByRole`, `getByLabel`, `getByText`) for resilience and accessibility. Avoid brittle selectors like XPath or CSS classes.
- **Web-First Assertions**: Always use Playwright's auto-retrying web-first assertions (e.g., `await expect(locator).toBeVisible()`). Avoid manual waits or sleeps.
- **Test Structure**: Group related tests for a feature under a `test.describe()` block. Use `beforeEach` for common setup actions like navigation.

## Test Structure Example

```typescript
import { test, expect } from '@playwright/test';

test.describe('User Login Feature', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
  });

  test('should allow a user to log in with valid credentials', async ({ page }) => {
    await test.step('Fill and submit login form', async () => {
      await page.getByLabel('Email').fill('test@example.com');
      await page.getByLabel('Password').fill('password123');
      await page.getByRole('button', { name: 'Log In' }).click();
    });

    await test.step('Verify successful navigation', async () => {
      await expect(page).toHaveURL('/dashboard');
      await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
    });
  });
});
```

## File Organization

- **Location**: Store all E2E test files in the `e2e/` directory at the root of the client monorepo.
- **Naming**: Use the convention `<feature>.spec.ts` (e.g., `login.spec.ts`).

## Assertion Best Practices

- **Visibility**: Use `toBeVisible()` to check if an element is in the DOM and visible.
- **Text**: Use `toHaveText()` for exact text matches and `toContainText()` for partial matches.
- **URL**: Use `toHaveURL()` to verify the page URL after an action.
- **Count**: Use `toHaveCount()` to assert the number of elements found by a locator.

## Test Execution

- Run tests headlessly in CI using `pnpm test:e2e`.
- For debugging, run tests in headed mode with `pnpm test:e2e --headed`.
