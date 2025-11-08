# Resume Store - Dependency Injection Pattern

This document explains how dependency injection is implemented and used in the Resume Store.

## Architecture

The store uses the **Dependency Injection** (DI) pattern through Vue's `provide/inject` system to decouple the validator implementation from the store logic.

### Advantages of this approach:

1. **Testability**: Easy to mock the validator in tests
2. **Flexibility**: You can change the validator implementation without modifying the store
3. **Clean Architecture**: Respects SOLID principles and keeps layers separated
4. **Centralized Configuration**: DI is configured in a single place

## File Structure

```
infrastructure/
├── di/
│   ├── keys.ts          # Injection keys
│   └── index.ts         # Barrel export
├── config/
│   ├── di.ts            # DI configuration
│   └── index.ts         # Barrel export
├── store/
│   ├── resumeStore.ts   # Pinia store
│   └── resumeStore.test.ts
└── validation/
    └── JsonResumeValidator.ts
```

## Configuration

### 1. Configure DI in `main.ts`

```typescript
import { createApp } from 'vue';
import App from './App.vue';
import { setupResumeDI } from '@/core/resume/infrastructure/config';

const app = createApp(App);

// Configure dependency injection for the resume module
setupResumeDI(app);

app.mount('#app');
```

### 2. Use the store in components

```typescript
import { useResumeStore } from '@/core/resume/infrastructure/store/resumeStore';

export default {
  setup() {
    const resumeStore = useResumeStore();

    // The store automatically uses the injected validator
    const resume = {
      basics: { /* ... */ },
      // ...
    };

    resumeStore.setResume(resume);
    console.log(resumeStore.isValid); // true/false

    return { resumeStore };
  }
};
```

## Store API

### State

- **`resume`**: `Resume | null` - The current resume
- **`isGenerating`**: `boolean` - Indicates if a resume is being generated
- **`generationError`**: `ProblemDetail | null` - Generation error if it exists

### Computed

- **`isValid`**: `boolean` - Indicates if the current resume is valid according to JSON Resume Schema
- **`hasResume`**: `boolean` - Indicates if there is a loaded resume

### Actions

#### `setResume(newResume: Resume): void`
Sets a new resume and validates it automatically.

```typescript
const resume = createResume();
resumeStore.setResume(resume);
console.log(resumeStore.isValid); // Automatic validation
```

#### `clearResume(): void`
Clears the current resume and errors.

```typescript
resumeStore.clearResume();
console.log(resumeStore.hasResume); // false
```

#### `validateResume(): boolean`
Explicitly validates the current resume.

```typescript
const isValid = resumeStore.validateResume();
if (!isValid) {
  console.error('Resume validation failed');
}
```

#### `setGenerating(generating: boolean): void`
Sets the generation state.

```typescript
resumeStore.setGenerating(true);
// ... perform generation ...
resumeStore.setGenerating(false);
```

#### `setGenerationError(error: ProblemDetail | null): void`
Sets a generation error.

```typescript
try {
  // ... generate resume ...
} catch (error) {
  resumeStore.setGenerationError({
    type: 'generation_error',
    title: 'Generation Failed',
    status: 500,
    detail: error.message
  });
}
```

## Testing

### Test with default validator

```typescript
import { setActivePinia, createPinia } from 'pinia';
import { useResumeStore } from './resumeStore';

describe('Resume Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should validate resume with default validator', () => {
    const store = useResumeStore();
    const resume = createValidResume();

    store.setResume(resume);

    expect(store.isValid).toBe(true);
  });
});
```

### Test with mock validator (DI)

```typescript
import { createApp } from 'vue';
import { createPinia, setActivePinia } from 'pinia';
import { RESUME_VALIDATOR_KEY } from '@/core/resume/infrastructure/di';
import { useResumeStore } from './resumeStore';

describe('Resume Store with Mock Validator', () => {
  it('should use injected validator', () => {
    // Create mock validator
    const mockValidator = {
      validate: vi.fn().mockReturnValue(true)
    };

    // Configure app with DI
    const app = createApp({});
    app.provide(RESUME_VALIDATOR_KEY, mockValidator);

    const pinia = createPinia();
    app.use(pinia);
    setActivePinia(pinia);

    // Use the store
    const store = useResumeStore();
    const resume = createResume();

    store.setResume(resume);

    // Verify that the injected validator was used
    expect(mockValidator.validate).toHaveBeenCalledWith(resume);
    expect(store.isValid).toBe(true);
  });
});
```

## Custom Validator Injection

If you need to use a custom validator in development or testing:

```typescript
import { createApp } from 'vue';
import { RESUME_VALIDATOR_KEY } from '@/core/resume/infrastructure/di';

class CustomResumeValidator implements ResumeValidator {
  validate(resume: Resume): boolean {
    // Custom logic
    return true;
  }
}

const app = createApp(App);
app.provide(RESUME_VALIDATOR_KEY, new CustomResumeValidator());
```

## Fallback Pattern

The store implements a fallback pattern: if no validator is injected, it automatically uses `JsonResumeValidator`:

```typescript
const validator: ResumeValidator =
  inject(RESUME_VALIDATOR_KEY) ?? new JsonResumeValidator();
```

This ensures the store always works, even if DI configuration is forgotten.

## Future Extension

To add more dependencies to the resume module:

1. Create new keys in `di/keys.ts`:
```typescript
export const RESUME_GENERATOR_KEY: InjectionKey<ResumeGenerator> =
  Symbol('ResumeGenerator');
```

2. Register them in `config/di.ts`:
```typescript
export function setupResumeDI(app: App): void {
  app.provide(RESUME_VALIDATOR_KEY, new JsonResumeValidator());
  app.provide(RESUME_GENERATOR_KEY, new AiResumeGenerator());
}
```

3. Inject them in the store:
```typescript
const validator = inject(RESUME_VALIDATOR_KEY) ?? new JsonResumeValidator();
const generator = inject(RESUME_GENERATOR_KEY) ?? new DefaultGenerator();
```

## Best Practices

1. **Always provide a fallback** so the store works without configuration
2. **Use typed InjectionKey** for type safety
3. **Centralize DI configuration** in `config/di.ts`
4. **Document required dependencies** for each module
5. **Use mocks in tests** to isolate store logic
