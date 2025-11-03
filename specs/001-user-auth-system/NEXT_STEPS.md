# Remaining Implementation Tasks

## Priority Tasks

### 1. Token Refresh Interceptor (High Priority)

Add to `AuthHttpClient.ts`:

```typescript
private setupInterceptors(): void {
  // Request interceptor - Add access token to requests if available
  this.client.interceptors.request.use(
    (config) => {
      // Token is sent via HTTP-only cookies, no need to manually add headers
      return config;
    },
    (error) => Promise.reject(error)
  );

  // Response interceptor for token refresh
  this.client.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const originalRequest = error.config as any;

      // If error is 401 and we haven't retried yet
      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true;

        try {
          // Attempt to refresh token
          await this.refreshToken();

          // Retry original request
          return this.client(originalRequest);
        } catch (refreshError) {
          // Refresh failed, redirect to login
          return Promise.reject(refreshError);
        }
      }

      return Promise.reject(this.handleError(error));
    }
  );
}
```

### 2. Session Persistence (Medium Priority)

Create `client/apps/webapp/src/authentication/infrastructure/storage/SessionStorage.ts`:

```typescript
const SESSION_KEY = 'auth_session_expires';

export class SessionStorage {
  setSessionExpiration(expiresIn: number): void {
    const expirationTime = Date.now() + (expiresIn * 1000);
    sessionStorage.setItem(SESSION_KEY, expirationTime.toString());
  }

  getSessionExpiration(): number | null {
    const expiration = sessionStorage.getItem(SESSION_KEY);
    return expiration ? parseInt(expiration, 10) : null;
  }

  clearSession(): void {
    sessionStorage.removeItem(SESSION_KEY);
  }

  isSessionValid(): boolean {
    const expiration = this.getSessionExpiration();
    return expiration ? Date.now() < expiration : false;
  }
}
```

Update `authStore.ts`:

```typescript
import { SessionStorage } from '../../infrastructure/storage/SessionStorage';

const sessionStorage = new SessionStorage();

// In login action:
async function login(data: LoginFormData): Promise<void> {
  // ... existing code
  session.value = newSession;
  sessionStorage.setSessionExpiration(newSession.expiresIn);
  // ... rest of code
}

// Add initialization action:
async function initialize(): Promise<void> {
  if (sessionStorage.isSessionValid()) {
    await checkAuth();
  }
}

// In logout:
async function logout(): Promise<void> {
  // ... existing code
  sessionStorage.clearSession();
}
```

Call `initialize()` in `App.vue`:

```typescript
<script setup lang="ts">
import { onMounted } from 'vue';
import { useAuthStore } from '@/features/authentication/presentation/stores/authStore';

const authStore = useAuthStore();

onMounted(async () => {
  await authStore.initialize();
});
</script>
```

### 3. E2E Tests (Medium Priority)

Create `client/apps/webapp/e2e/authentication.spec.ts`:

```typescript
import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {
  test('should register a new user successfully', async ({ page }) => {
    await page.goto('/register');

    await page.getByLabel('First Name').fill('John');
    await page.getByLabel('Last Name').fill('Doe');
    await page.getByLabel('Email').fill(`test-${Date.now()}@example.com`);
    await page.getByLabel('Password', { exact: true }).fill('Test123!@#');
    await page.getByLabel('Confirm Password').fill('Test123!@#');
    await page.getByLabel(/accept the terms/).check();

    await page.getByRole('button', { name: /create account/i }).click();

    // Should redirect to dashboard after registration
    await expect(page).toHaveURL('/dashboard');
    await expect(page.getByText(/welcome back/i)).toBeVisible();
  });

  test('should login with valid credentials', async ({ page }) => {
    await page.goto('/login');

    await page.getByLabel('Email').fill('existing@example.com');
    await page.getByLabel('Password').fill('ValidPassword123!');

    await page.getByRole('button', { name: /sign in/i }).click();

    await expect(page).toHaveURL('/dashboard');
    await expect(page.getByText(/welcome back/i)).toBeVisible();
  });

  test('should show validation errors for invalid input', async ({ page }) => {
    await page.goto('/register');

    await page.getByLabel('Email').fill('invalid-email');
    await page.getByLabel('Password', { exact: true }).fill('weak');

    // Should show validation errors
    await expect(page.getByText(/invalid email format/i)).toBeVisible();
    await expect(page.getByText(/password must be at least/i)).toBeVisible();
  });

  test('should protect dashboard route when not authenticated', async ({ page }) => {
    await page.goto('/dashboard');

    // Should redirect to login
    await expect(page).toHaveURL(/\/login/);
  });

  test('should logout successfully', async ({ page, context }) => {
    // Login first
    await page.goto('/login');
    await page.getByLabel('Email').fill('existing@example.com');
    await page.getByLabel('Password').fill('ValidPassword123!');
    await page.getByRole('button', { name: /sign in/i }).click();

    await expect(page).toHaveURL('/dashboard');

    // Logout
    await page.getByRole('button', { name: /logout/i }).click();

    // Should redirect to login
    await expect(page).toHaveURL('/login');

    // Try to access dashboard again - should redirect
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/\/login/);
  });

  test('should remember redirect URL after login', async ({ page }) => {
    // Try to access protected page
    await page.goto('/dashboard');

    // Should redirect to login with redirect query
    await expect(page).toHaveURL(/\/login\?redirect=/);

    // Login
    await page.getByLabel('Email').fill('existing@example.com');
    await page.getByLabel('Password').fill('ValidPassword123!');
    await page.getByRole('button', { name: /sign in/i }).click();

    // Should redirect back to dashboard
    await expect(page).toHaveURL('/dashboard');
  });
});
```

### 4. Run and Test

```bash
# Terminal 1: Start backend
./gradlew :server:engine:bootRun

# Terminal 2: Start frontend
cd client/apps/webapp
pnpm dev

# Terminal 3: Run E2E tests
cd client/apps/webapp
pnpm test:e2e
```

## Testing Checklist

- [ ] Backend is running on <http://localhost:8080>
- [ ] Keycloak is accessible at <http://localhost:9080>
- [ ] Frontend dev server is running on <http://localhost:9876>
- [ ] Can register a new user
- [ ] Can login with registered user
- [ ] Dashboard shows user information
- [ ] Logout works and clears session
- [ ] Protected routes redirect to login
- [ ] Login remembers original destination
- [ ] Validation errors display correctly
- [ ] OAuth buttons are rendered (integration pending)

## Known Limitations

1. **OAuth Integration**: OAuth providers need Keycloak configuration
2. **Email Verification**: Not implemented yet
3. **Password Reset**: Not implemented yet
4. **Multi-factor Authentication**: Not implemented yet
5. **Session Management UI**: Not implemented yet (view/revoke sessions)

## Security Considerations

- ✅ HTTP-only cookies for tokens
- ✅ CSRF protection enabled
- ✅ Password strength requirements
- ✅ SQL injection prevention (parameterized queries)
- ✅ XSS prevention (Vue template escaping)
- ⚠️ Rate limiting (needs implementation)
- ⚠️ Account lockout (needs implementation)
- ⚠️ Audit logging (needs implementation)
