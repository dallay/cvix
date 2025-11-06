# Vue 3 Conventions

> This document outlines the conventions and best practices for Vue 3 development.

## Component Conventions

- Use the `<script setup lang="ts"> </script>` syntax for all components.
- Name components using `PascalCase` (e.g., `UserProfileCard.vue`).
- Define props using `defineProps()` and provide defaults with `withDefaults()`.
- Explicitly declare all emitted events with `defineEmits()`.
- Co-locate styles in a `<style scoped>` block.

## State Management (Pinia)

- Use **Pinia** for all state management.
- Organize stores by domain (e.g., `useUserStore`, `useProjectStore`).
- Always provide strong types for state, getters, and actions.

## Composition API

- Encapsulate reusable logic into composables (e.g., `useFeature.ts`) and place them in the `composables/` directory.
- Composables should always return reactive values (`ref`, `computed`).

## UI Components

- Use **Shadcn-Vue** as the primary UI component library (`src/components/ui`). Use the components as provided, customizing styles via props or CSS variables.
- Create custom components only when necessary (e.g., for unique functionality or complex interactions).
- Prefer composing functionality with slots over complex props.
- Ensure all interactive components are accessible (a11y).

## Form Validation (Vee-Validate)

- Use **Vee-Validate** with Zod schemas for all form validation.
- **Validation Strategy**: Use manual validation on blur for optimal UX:
  - Do NOT use `validate-on-blur`, `validate-on-change`, or `validate-on-input` props on FormField
  - Instead, call `validateField()` manually in the `@blur` event of the input
  - Set `validateOnMount: false` in the form configuration
  - This ensures validation only happens when the user leaves a field, not while typing

Example:

```vue
<script setup lang="ts">
const { handleSubmit, validateField } = useForm<FormData>({
  validationSchema: toTypedSchema(schema),
  validateOnMount: false,
});
</script>

<template>
  <FormField
    v-slot="{ componentField }"
    name="email"
  >
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

This ensures users aren't frustrated by validation errors appearing while they're still typing, and prevents vee-validate from switching to "aggressive" mode after the first validation.

## Internationalization (i18n)

- Use `vue-i18n` for all user-facing text.
- Wrap all display text with the `$t()` function.
- Organize translation keys by domain or component (e.g., `userProfile.title`).

## Code Quality

- Use **Biome** for linting and formatting.
- Use TypeScript with `strict` mode enabled.

## Communication Between Components

- **Parent-Child**: Use props (down) and events (up).
- **Distant/Sibling**: Use a Pinia store or a shared composable.
- Avoid global event buses.

## Performance

- Use `v-memo` and `v-once` for static content.
- Use lazy loading for components and routes that are not immediately visible.
- Clean up side effects (e.g., timers, event listeners) in the `onUnmounted` lifecycle hook.
