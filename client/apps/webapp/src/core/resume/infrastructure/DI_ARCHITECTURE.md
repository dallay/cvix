# Dependency Injection Architecture - Resume Module

## Executive Summary

A **Dependency Injection (DI)** pattern has been implemented using Vue's `provide/inject` system to decouple the resume validator from the Pinia store. This solution:

✅ **Enables easy testing** with mock validators
✅ **Maintains Clean Architecture** by separating domain from infrastructure
✅ **Provides fallback** with `JsonResumeValidator` by default
✅ **Is extensible** to add more dependencies in the future

## Architecture

### Layer Structure

```
src/core/resume/
├── domain/                          # Domain Layer
│   ├── Resume.ts                    # Entities
│   └── ResumeValidator.ts           # Interfaces (ports)
│
├── infrastructure/                  # Infrastructure Layer
│   ├── di/                          # Dependency Injection
│   │   ├── keys.ts                  # Injection keys (InjectionKey)
│   │   └── index.ts
│   │
│   ├── config/                      # Configuration
│   │   ├── di.ts                    # DI setup
│   │   └── index.ts
│   │
│   ├── store/                       # State Management
│   │   ├── resume.store.ts           # Pinia store (consumes validator)
│   │   └── resume.store.test.ts
│   │
│   └── validation/                  # Validators (adapters)
│       ├── JsonResumeValidator.ts   # Concrete implementation
│       └── JsonResumeValidator.test.ts
```

### Injection Flow

```
┌─────────────────────────────────────────────────────────────┐
│                        main.ts                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ import { setupResumeDI } from '@/core/resume/.../di'  │  │
│  │ setupResumeDI(app)                                    │  │
│  └───────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ├─ app.provide(RESUME_VALIDATOR_KEY,
                         │              new JsonResumeValidator())
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    resume.store.ts                           │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ function getValidator(): ResumeValidator {            │  │
│  │   const instance = getCurrentInstance()              │  │
│  │   if (instance?.appContext.provides[KEY]) {          │  │
│  │     return instance.appContext.provides[KEY]         │  │
│  │   }                                                   │  │
│  │   return new JsonResumeValidator() // Fallback       │  │
│  │ }                                                     │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  const validator = getValidator()                          │
│  const isValid = computed(() => validator.validate(...))   │
└─────────────────────────────────────────────────────────────┘
```

## Key Components

### 1. Injection Key (`di/keys.ts`)

```typescript
export const RESUME_VALIDATOR_KEY: InjectionKey<ResumeValidator> = Symbol(
  "ResumeValidator"
);
```

**Purpose**: Defines a typed key for injecting/consuming the validator

### 2. DI Configuration (`config/di.ts`)

```typescript
export function setupResumeDI(app: App): void {
  app.provide(RESUME_VALIDATOR_KEY, new JsonResumeValidator());
}
```

**Purpose**: Registers dependencies in the Vue container

### 3. Store with DI (`store/resume.store.ts`)

```typescript
function getValidator(): ResumeValidator {
  const instance = getCurrentInstance();
  if (instance?.appContext.provides[RESUME_VALIDATOR_KEY as symbol]) {
    return instance.appContext.provides[RESUME_VALIDATOR_KEY as symbol];
  }
  return new JsonResumeValidator(); // Fallback
}

export const useResumeStore = defineStore("resume", () => {
  const validator = getValidator();
  // ... uses validator to validate resume
});
```

**Purpose**: Consumes the injected validator with automatic fallback

## Usage

### Setup in `main.ts`

```typescript
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import { setupResumeDI } from '@/core/resume/infrastructure/config';

const app = createApp(App);
const pinia = createPinia();

// Configure DI
setupResumeDI(app);

app.use(pinia);
app.mount('#app');
```

### Usage in Components

```typescript
<script setup lang="ts">
import { useResumeStore } from '@/core/resume/infrastructure/store/resume.store';

const resumeStore = useResumeStore();

const resume = {
  basics: {
    name: "John Doe",
    email: "john@example.com",
    // ...
  },
  // ...
};

resumeStore.setResume(resume);
console.log(resumeStore.isValid); // true/false (automatically validated)
</script>
```

## Testing

### Store Testing (with fallback)

```typescript
describe('useResumeStore', () => {
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

The store automatically uses `JsonResumeValidator` when nothing is injected.

### Testing with Custom Validator (future)

If in the future you need to test with a mock validator, you could extend the approach with a testable factory.

## Advantages of this Implementation

### 1. **Separation of Concerns**
- The store does NOT know which concrete validator it uses
- The validator implementation can change without touching the store

### 2. **Dependency Inversion Principle (SOLID)**
```
┌──────────────────────────────┐
│     resume.store.ts           │  ← Depends on abstraction
│   (depends on interface)     │
└──────────┬───────────────────┘
           │ uses
           ▼
┌──────────────────────────────┐
│    ResumeValidator           │  ← Interface (domain)
│      (interface)             │
└──────────▲───────────────────┘
           │ implements
           │
┌──────────┴───────────────────┐
│  JsonResumeValidator         │  ← Concrete implementation
│    (infrastructure)          │
└──────────────────────────────┘
```

### 3. **Testability**
- The store can be tested with the default validator
- Easy to add mocks in the future if necessary

### 4. **Centralized Configuration**
- All DI is configured in `setupResumeDI()`
- Single place to change implementations

### 5. **Automatic Fallback**
- If you forget to configure DI, the store still works
- Uses `JsonResumeValidator` by default

## Future Extension

### Adding More Dependencies

**1. Create new key:**
```typescript
// di/keys.ts
export const RESUME_GENERATOR_KEY: InjectionKey<ResumeGenerator> =
  Symbol('ResumeGenerator');
```

**2. Register in DI:**
```typescript
// config/di.ts
export function setupResumeDI(app: App): void {
  app.provide(RESUME_VALIDATOR_KEY, new JsonResumeValidator());
  app.provide(RESUME_GENERATOR_KEY, new AiResumeGenerator());
}
```

**3. Consume in the store:**
```typescript
// store/resume.store.ts
function getGenerator(): ResumeGenerator {
  const instance = getCurrentInstance();
  if (instance?.appContext.provides[RESUME_GENERATOR_KEY as symbol]) {
    return instance.appContext.provides[RESUME_GENERATOR_KEY as symbol];
  }
  return new DefaultResumeGenerator();
}

export const useResumeStore = defineStore("resume", () => {
  const validator = getValidator();
  const generator = getGenerator();
  // ...
});
```

## Comparison with Alternatives

| Approach                           | Pros                                                          | Cons                                       |
| ---------------------------------- | ------------------------------------------------------------- | ------------------------------------------ |
| **DI with provide/inject** (current) | ✅ Clean Architecture<br>✅ Extensible<br>✅ Automatic fallback | ⚠️ Requires setup in main.ts                |
| Direct import                      | ✅ Simple                                                      | ❌ Tight coupling<br>❌ Difficult testing   |
| Factory pattern                    | ✅ Flexible                                                    | ❌ More boilerplate code                   |
| Service Locator                    | ✅ Centralized                                                | ❌ Anti-pattern (hidden dependencies)      |

## Best Practices

1. ✅ **Always provide fallback** so code works without configuration
2. ✅ **Use typed `InjectionKey`** for complete type safety
3. ✅ **Centralize configuration** in a single file (`config/di.ts`)
4. ✅ **Document required dependencies** for each module
5. ✅ **Keep interfaces in domain** and implementations in infrastructure
6. ✅ **Store tests** should work without special setup (thanks to fallback)

## Conclusion

This DI architecture provides:

- 🎯 **Clean Architecture**: Domain separated from infrastructure
- 🧪 **Testability**: Easy to test with real or mock validators
- 🔧 **Maintainability**: Easy to change implementations
- 📈 **Scalability**: Easy to add more dependencies
- 🛡️ **Robustness**: Automatic fallbacks prevent errors

The pattern is ready to scale when more features are added such as:
- Resume generators (AI-powered)
- Export services (PDF, DOCX)
- Template engines
- Analytics services
