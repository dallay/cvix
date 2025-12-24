# Web Application (Vue 3 SPA)

> **Production-grade Vue 3 SPA** for ProFileTailors with TypeScript, Pinia state management, and enterprise-level architecture.

---

## Overview

This is the **main web application** where users create, edit, and manage their professional resumes. Built with Vue 3 Composition API and TypeScript, it follows Clean Architecture principles with clear separation between domain, application, and infrastructure layers.

### Key Features

- ⚡ **Vue 3 Composition API**: Modern reactive architecture
- 📘 **TypeScript**: Full type safety with strict mode
- 🗄️ **Pinia**: Intuitive state management
- 🎨 **Tailwind CSS 4**: Token-based design system
- ✅ **Vee-Validate + Zod**: Declarative form validation
- 🧩 **Shadcn-Vue**: High-quality UI component library
- 🔐 **Keycloak**: OAuth2 authentication
- 🧪 **Vitest**: Lightning-fast unit testing
- 🎭 **Playwright**: Robust E2E testing

---

## Tech Stack

| Technology        | Purpose                              |
|-------------------|--------------------------------------|
| **Vue 3**         | Progressive JavaScript framework     |
| **TypeScript**    | Type-safe development                |
| **Vite**          | Build tool and dev server            |
| **Pinia**         | State management                     |
| **Vue Router**    | Client-side routing                  |
| **Vee-Validate**  | Form validation                      |
| **Zod**           | Schema validation                    |
| **TailwindCSS**   | Utility-first styling                |
| **Shadcn-Vue**    | UI component library                 |
| **Vitest**        | Unit and component testing           |
| **Playwright**    | End-to-end testing                   |

---

## Project Structure

```text
src/
├── core/                    # Domain-driven features
│   ├── authentication/      # Auth logic and Keycloak integration
│   ├── resume/              # Resume management (domain, application, infrastructure)
│   │   ├── domain/          # Business logic and entities
│   │   ├── application/     # Use cases and services
│   │   └── infrastructure/  # API clients, stores, validation
│   └── settings/            # User preferences and settings
├── components/              # Shared UI components
├── composables/             # Reusable composition functions
├── layouts/                 # Page layouts (authenticated, public, etc.)
├── pages/                   # Route-based pages
├── router/                  # Vue Router configuration
├── stores/                  # Pinia stores
├── styles/                  # Global styles and design tokens
└── types/                   # Shared TypeScript types
```

### Architecture Philosophy

Each feature follows **Hexagonal Architecture**:

- **`domain/`**: Pure business logic (framework-agnostic)
- **`application/`**: Use cases and orchestration
- **`infrastructure/`**: Framework integration (API, stores, validation)

See `.ruler/reference/architecture.md` for detailed architecture documentation.

---

## Development

### Prerequisites

- Node.js 20+
- pnpm 10+
- Backend services running (see root README)

### Commands

```bash
# Install dependencies (from repo root)
pnpm install

# Start dev server with hot reload
pnpm dev

# Type checking
pnpm type-check

# Linting and formatting
pnpm check

# Run unit tests
pnpm test:unit

# Run unit tests in watch mode
pnpm test:unit --watch

# Run E2E tests
pnpm test:e2e

# Run E2E tests in headed mode (with browser UI)
pnpm test:e2e --headed

# Build for production
pnpm build

# Preview production build
pnpm preview
```

---

## Component Development

### Vue Component Structure

```vue
<script setup lang="ts">
import { computed, ref } from 'vue';

// Props with TypeScript
type Props = {
  title: string;
  count?: number;
};

const props = withDefaults(defineProps<Props>(), {
  count: 0,
});

// Events with TypeScript
const emit = defineEmits<{
  (e: 'update', value: number): void;
  (e: 'close'): void;
}>();

// Reactive state
const localCount = ref(props.count);

// Computed properties
const doubled = computed(() => localCount.value * 2);

// Methods
const increment = () => {
  localCount.value++;
  emit('update', localCount.value);
};
</script>

<template>
  <div class="card">
    <h2>{{ title }}</h2>
    <p>Count: {{ localCount }} (Doubled: {{ doubled }})</p>
    <button @click="increment">Increment</button>
  </div>
</template>

<style scoped>
.card {
  @apply rounded-lg border border-border bg-card p-4;
}
</style>
```

### UI Components

Use **Shadcn-Vue** components from `@/components/ui/`:

```vue
<script setup lang="ts">
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
</script>

<template>
  <div>
    <Label for="email">Email</Label>
    <Input id="email" type="email" placeholder="you@example.com" />
    <Button variant="default">Submit</Button>
  </div>
</template>
```

---

## State Management (Pinia)

### Creating a Store

```typescript
// stores/user.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

type User = {
  id: string;
  name: string;
  email: string;
};

export const useUserStore = defineStore('user', () => {
  // State
  const currentUser = ref<User | null>(null);
  const isLoading = ref(false);

  // Getters
  const isAuthenticated = computed(() => currentUser.value !== null);

  // Actions
  const fetchUser = async (id: string) => {
    isLoading.value = true;
    try {
      // API call here
      currentUser.value = await api.getUser(id);
    } finally {
      isLoading.value = false;
    }
  };

  return {
    currentUser,
    isLoading,
    isAuthenticated,
    fetchUser,
  };
});
```

### Using a Store

```vue
<script setup lang="ts">
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();

onMounted(() => {
  userStore.fetchUser('123');
});
</script>

<template>
  <div v-if="userStore.isAuthenticated">
    <p>Welcome, {{ userStore.currentUser?.name }}!</p>
  </div>
</template>
```

---

## Form Validation

### Vee-Validate with Zod

```vue
<script setup lang="ts">
import { useForm } from 'vee-validate';
import { toTypedSchema } from '@vee-validate/zod';
import { z } from 'zod';
import { FormField, FormItem, FormLabel, FormControl, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';

// Define schema
const schema = z.object({
  email: z.string().email('Invalid email format'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
});

type FormData = z.infer<typeof schema>;

// Setup form
const { handleSubmit, validateField } = useForm<FormData>({
  validationSchema: toTypedSchema(schema),
  validateOnMount: false,
});

// Submit handler
const onSubmit = handleSubmit((values) => {
  console.log('Form submitted:', values);
});
</script>

<template>
  <form @submit="onSubmit">
    <FormField v-slot="{ componentField }" name="email">
      <FormItem>
        <FormLabel>Email</FormLabel>
        <FormControl>
          <Input
            type="email"
            v-bind="componentField"
            @blur="validateField('email')"
          />
        </FormControl>
        <FormMessage />
      </FormItem>
    </FormField>

    <FormField v-slot="{ componentField }" name="password">
      <FormItem>
        <FormLabel>Password</FormLabel>
        <FormControl>
          <Input
            type="password"
            v-bind="componentField"
            @blur="validateField('password')"
          />
        </FormControl>
        <FormMessage />
      </FormItem>
    </FormField>

    <Button type="submit">Sign In</Button>
  </form>
</template>
```

**Important**: Validate on blur, not on input/change, for optimal UX.

---

## Composables

Extract reusable logic into composables:

```typescript
// composables/useCounter.ts
import { ref, computed } from 'vue';

export const useCounter = (initialValue = 0) => {
  const count = ref(initialValue);
  const doubled = computed(() => count.value * 2);

  const increment = () => count.value++;
  const decrement = () => count.value--;
  const reset = () => count.value = initialValue;

  return {
    count,
    doubled,
    increment,
    decrement,
    reset,
  };
};
```

Usage:

```vue
<script setup lang="ts">
import { useCounter } from '@/composables/useCounter';

const { count, doubled, increment, decrement, reset } = useCounter(10);
</script>
```

---

## Testing

### Unit Tests (Vitest)

```typescript
// components/Counter.spec.ts
import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import Counter from './Counter.vue';

describe('Counter', () => {
  it('renders initial count', () => {
    const wrapper = mount(Counter, {
      props: { initialCount: 5 },
    });
    expect(wrapper.text()).toContain('5');
  });

  it('increments count on button click', async () => {
    const wrapper = mount(Counter);
    await wrapper.find('button').trigger('click');
    expect(wrapper.text()).toContain('1');
  });
});
```

### E2E Tests (Playwright)

Located in `e2e/`:

```typescript
// e2e/login.spec.ts
import { test, expect } from '@playwright/test';

test.describe('User Login', () => {
  test('should allow user to log in', async ({ page }) => {
    await page.goto('/login');

    await page.getByLabel('Email').fill('test@example.com');
    await page.getByLabel('Password').fill('password123');
    await page.getByRole('button', { name: 'Sign In' }).click();

    await expect(page).toHaveURL('/dashboard');
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
  });
});
```

---

## Styling

### Design System

Use semantic tokens from the global design system:

```html
<div class="bg-background text-foreground">
  <div class="bg-card border-border rounded-lg p-4">
    <button class="bg-primary text-primary-foreground">Action</button>
  </div>
</div>
```

See `.ruler/frontend/design-system.md` for complete token reference.

### Dark Mode

Dark mode is automatic via the `.dark` class on the root element. All semantic tokens adapt automatically.

---

## Authentication

Authentication is handled via **Keycloak** with OAuth2:

```typescript
// In a component
import { useAuthStore } from '@/core/authentication/store';

const authStore = useAuthStore();

// Check authentication status
if (authStore.isAuthenticated) {
  // User is logged in
}

// Get current user
const user = authStore.currentUser;

// Login
await authStore.login();

// Logout
await authStore.logout();
```

---

## Conventions

- Follow `.ruler/frontend/vue.md` for Vue-specific guidelines
- Follow `.ruler/frontend/typescript.md` for TypeScript standards
- Follow `.ruler/frontend/design-system.md` for styling
- Use Composition API (not Options API)
- Keep components small and focused
- Use TypeScript strict mode
- Test critical user flows with E2E tests

---

## Environment Variables

Create `.env.local` for local development:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_KEYCLOAK_URL=http://localhost:8080/auth
VITE_KEYCLOAK_REALM=cvix
VITE_KEYCLOAK_CLIENT_ID=webapp
```

---

## References

- [Vue 3 Documentation](https://vuejs.org/)
- [Pinia Documentation](https://pinia.vuejs.org/)
- [Vee-Validate Documentation](https://vee-validate.logaretm.com/v4/)
- [Zod Documentation](https://zod.dev/)
- [Shadcn-Vue](https://www.shadcn-vue.com/)
- [Vitest Documentation](https://vitest.dev/)
- [Playwright Documentation](https://playwright.dev/)
