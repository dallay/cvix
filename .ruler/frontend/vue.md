# Vue 3 Conventions

> Conventions and best practices for Vue 3 development with Composition API and TypeScript.

## Component Conventions

- Prefer the **Composition API** with TypeScript for all Vue components
- Use the `<script setup lang="ts">` block for concise, type-safe code
- Name components using `PascalCase` (e.g., `UserProfileCard.vue`)
- Define props using `defineProps()` and provide defaults with `withDefaults()`
- Explicitly declare all emitted events with `defineEmits()`
- Co-locate styles in a `<style scoped>` block

```vue
<script setup lang="ts">
import { computed } from 'vue';

type Props = {
  title: string;
  count?: number;
};

const props = withDefaults(defineProps<Props>(), {
  count: 0,
});

const { count, title } = props;

const emit = defineEmits<{
  (e: 'update', value: number): void;
  (e: 'close'): void;
}>();

const doubled = computed(() => count * 2);
</script>

<template>
  <div class="card">
    <h2>{{ title }}</h2>
    <p>Count: {{ count }} (Doubled: {{ doubled }})</p>
    <button @click="emit('update', count + 1)">Increment</button>
  </div>
</template>

<style scoped>
.card {
  /* styles */
}
</style>
```

## State Management (Pinia)

- Use **Pinia** for all state management
- Organize stores by domain (e.g., `useUserStore`, `useProjectStore`)
- Always provide strong types for state, getters, and actions

```typescript
// stores/user.ts
import { defineStore } from 'pinia';

type User = {
  id: string;
  name: string;
  email: string;
};

type UserState = {
  currentUser: User | null;
  isLoading: boolean;
};

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    currentUser: null,
    isLoading: false,
  }),
  getters: {
    isAuthenticated: (state) => state.currentUser !== null,
  },
  actions: {
    async fetchUser(id: string) {
      this.isLoading = true;
      // fetch logic
      this.isLoading = false;
    },
  },
});
```

## Composition API

- Encapsulate reusable logic into **composables** (e.g., `useFeature.ts`)
- Place composables in the `composables/` directory
- Composables should always return reactive values (`ref`, `computed`)

```typescript
// composables/useCounter.ts
import { ref, computed } from 'vue';

export const useCounter = (initial = 0) => {
  const count = ref(initial);
  const doubled = computed(() => count.value * 2);
  
  const increment = () => count.value++;
  const decrement = () => count.value--;
  
  return { count, doubled, increment, decrement };
};
```

## UI Components

- Use **Shadcn-Vue** as the primary UI component library (located in `client/packages/ui`)
- Use components as provided, customizing styles via props or CSS variables
- Create custom components only when necessary (unique functionality or complex interactions)
- Prefer composing functionality with **slots** over complex props
- Ensure all interactive components are accessible (a11y)

## Form Validation (Vee-Validate)

- Use **Vee-Validate** with **Zod** schemas for all form validation
- **Validation Strategy**: Use manual validation on blur for optimal UX:
  - Do **NOT** use `validate-on-blur`, `validate-on-change`, or `validate-on-input` props on FormField
  - Instead, call `validateField()` manually in the `@blur` event of the input
  - Set `validateOnMount: false` in the form configuration

```vue
<script setup lang="ts">
import { useForm } from 'vee-validate';
import { toTypedSchema } from '@vee-validate/zod';
import { z } from 'zod';

const schema = z.object({
  email: z.string().email('Invalid email format'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
});

type FormData = z.infer<typeof schema>;

const { handleSubmit, validateField } = useForm<FormData>({
  validationSchema: toTypedSchema(schema),
  validateOnMount: false,
});
</script>

<template>
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
</template>
```

## Accessibility (a11y)

- Always use **semantic HTML elements** and ARIA attributes where needed
- Prefer user-facing locators (roles, labels, text) for E2E and component tests
- Ensure **keyboard navigation** and focus management in interactive components
- Use tools like axe-core or Playwright's accessibility snapshot for audits

## Props and Events Documentation

- Document complex props and events using **JSDoc comments** above the `<script setup>` block
- For reusable components, provide clear prop types and event signatures

## UI Extension

- To extend or customize UI components from `client/packages/ui`, prefer **composition** (slots, wrappers) over direct modification
- If you need new variants or behaviors, create a new component in your app and wrap the base UI component
- Document any custom UI extensions in the relevant domain README

## Internationalization (i18n)

- Use `vue-i18n` for all user-facing text
- Wrap all display text with the `$t()` function
- Organize translation keys by domain or component (e.g., `userProfile.title`)

## Code Quality

- Use **Biome** for linting and formatting
- Use TypeScript with `strict` mode enabled

## Communication Between Components

| Scenario | Approach |
|----------|----------|
| Parent-Child | Props (down) and events (up) |
| Distant/Sibling | Pinia store or shared composable |
| **Avoid** | Global event buses |

## Performance

- Use `v-memo` and `v-once` for static content
- Use **lazy loading** for components and routes that are not immediately visible
- Clean up side effects (timers, event listeners) in the `onUnmounted` lifecycle hook

```typescript
import { onUnmounted } from 'vue';

const intervalId = setInterval(() => {
  // polling logic
}, 5000);

onUnmounted(() => {
  clearInterval(intervalId);
});
```
