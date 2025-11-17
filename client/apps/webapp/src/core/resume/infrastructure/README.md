# Resume Infrastructure Layer

## Description

This layer contains all concrete infrastructure implementations for the Resume module, following **Clean Architecture** and **Hexagonal Architecture** principles.

## Structure

```text
infrastructure/
├── di/                    # Dependency Injection
│   ├── keys.ts            # Typed injection keys
│   └── index.ts
│
├── config/                # Configuration
│   ├── di.ts              # Dependency Injection setup
│   └── index.ts
│
├── store/                 # State Management (Pinia)
│   ├── resume.store.ts     # Main Resume store
│   ├── resume.store.test.ts
│   └── README.md
│
└── validation/            # Validators (Adapters)
    ├── JsonResumeValidator.ts      # JSON Resume Schema validator
    ├── JsonResumeValidator.test.ts
    ├── index.ts
    └── README.md
```

## Main Features

### 1. Dependency Injection (DI)

Implements the **Dependency Injection** pattern using Vue 3 `provide/inject`:

- **Decoupling**: The store does not depend on concrete implementations
- **Testability**: Easy to inject mocks or alternative implementations
- **Automatic fallback**: If DI is not configured, uses default implementations
- **Type-safe**: Uses `InjectionKey` to maintain complete typing

See [DI_ARCHITECTURE.md](./DI_ARCHITECTURE.md) for complete details.

### 2. JSON Resume Validator

Complete validator that complies with the [JSON Resume Schema](https://jsonresume.org/schema/) standard:

✅ Validates all resume sections
✅ Validates formats: emails (RFC 5322), URLs (RFC 3986), dates (ISO 8601), country codes (ISO 3166-1)
✅ 15 unit tests with 100% coverage
✅ Type-safe with strict TypeScript

See [validation/README.md](./validation/README.md) for validation details.

### 3. Pinia Store

Reactive store to manage resume state:

- **Auto-validation**: The resume is automatically validated when set
- **PDF generation**: Generates PDFs using the injected `ResumeGenerator`
- **State management**: Manages resume, generation states and errors
- **Computed properties**: `isValid`, `hasResume` for easy use
- **15 unit tests** verifying all functionality (validation + generation)

See [store/README.md](./store/README.md) for store details.

## Quickstart

### Setup in `main.ts`

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { setupResumeDI } from '@/core/resume/infrastructure/config'
import App from './App.vue'

const app = createApp(App)
const pinia = createPinia()

// Configure Dependency Injection
setupResumeDI(app)

app.use(pinia)
app.mount('#app')
```

### Usage in Components

```typescript
import { useResumeStore } from '@/core/resume/infrastructure/store/resume.store'

const resume.store = useResumeStore()

// Set a resume
resumeStore.setResume({
  basics: {
    name: "John Doe",
    email: "john@example.com",
  },
})

// Automatic validation
console.log(resumeStore.isValid) // true/false

// Manual validation
const errors = resumeStore.validateResume()
console.log(errors) // true/false

// Generate PDF
async function downloadPDF() {
  try {
    const pdf = await resumeStore.generatePdf('en')

    // Create download link
    const url = URL.createObjectURL(pdf)
    const link = document.createElement('a')
    link.href = url
    link.download = 'resume.pdf'
    link.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    console.error('Error generating PDF:', error)
  }
}
```

## Architectural Principles

### Clean Architecture

```text
┌─────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Resume.ts (Entities)                            │   │
│  │ ResumeValidator.ts (Interface/Port)             │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────┬───────────────────────────────────────┘
                  │ Implements
                  ▼
┌─────────────────────────────────────────────────────────┐
│              INFRASTRUCTURE LAYER                       │
│  ┌──────────────────────────────────────────────────┐  │
│  │ JsonResumeValidator (Adapter)                    │  │
│  │ resume.store (State Management)                   │  │
│  │ DI Configuration (Wiring)                        │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

**Dependency rules:**

1. **Domain** does NOT depend on anything (pure TypeScript only)
2. **Infrastructure** depends on Domain (implements interfaces)
3. Dependency flow goes from outside to inside

### Dependency Inversion (SOLID)

```typescript
// ❌ BAD: Store depends on concrete implementation
import { JsonResumeValidator } from './validation/JsonResumeValidator'
const validator = new JsonResumeValidator()

// ✅ GOOD: Store depends on abstraction (interface)
import type { ResumeValidator } from '../../domain/ResumeValidator'
const validator = getValidator() // Injected via DI
```

## Testing

### Run Tests

```bash
# Validator tests
pnpm --filter @loomify/webapp test:unit JsonResumeValidator

# Store tests
pnpm --filter @loomify/webapp test:unit resume.store

# All resume tests
pnpm --filter @loomify/webapp test:unit resume
```

### Coverage

| Module              | Tests | Status |
| ------------------- | ----- | ------ |
| JsonResumeValidator | 15    | ✅ 100% |
| resume.store         | 15    | ✅ 100% |

## Extension

### Adding a New Validator

If you need an alternative validator implementation:

**1. Create new implementation:**

```typescript
// infrastructure/validation/CustomResumeValidator.ts
import type { Resume, ResumeValidator } from '../../domain'

export class CustomResumeValidator implements ResumeValidator {
  validate(resume: Resume | null): boolean {
    // Your custom logic
    return true
  }

  validateWithErrors(resume: Resume | null): string[] {
    // Your custom logic
    return []
  }
}
```

**2. Register in DI:**

```typescript
// infrastructure/config/di.ts
import { CustomResumeValidator } from '../validation/CustomResumeValidator'

export function setupResumeDI(app: App): void {
  app.provide(RESUME_VALIDATOR_KEY, new CustomResumeValidator())
}
```

The store will automatically use the new validator without modifications.

### Adding New Dependencies

To add more injectable services:

**1. Create injection key:**

```typescript
// infrastructure/di/keys.ts
export const RESUME_GENERATOR_KEY: InjectionKey<ResumeGenerator> =
  Symbol('ResumeGenerator')
```

**2. Register in configuration:**

```typescript
// infrastructure/config/di.ts
export function setupResumeDI(app: App): void {
  app.provide(RESUME_VALIDATOR_KEY, new JsonResumeValidator())
  app.provide(RESUME_GENERATOR_KEY, new AiResumeGenerator())
}
```

**3. Consume in the store:**

```typescript
function getGenerator(): ResumeGenerator {
  const instance = getCurrentInstance()
  if (instance?.appContext.provides[RESUME_GENERATOR_KEY as symbol]) {
    return instance.appContext.provides[RESUME_GENERATOR_KEY as symbol]
  }
  return new DefaultResumeGenerator() // Fallback
}
```

## Best Practices

### ✅ Do

- Use domain interfaces instead of concrete classes
- Provide fallbacks for all injected dependencies
- Write unit tests for each adapter
- Keep business logic in the domain
- Use typed `InjectionKey` for type safety

### ❌ Avoid

- Import concrete infrastructure classes in the domain
- Use `any` or `unknown` for injected dependencies
- Couple the store to specific implementations
- Put business logic in adapters
- Forget to configure `setupResumeDI()` in main.ts

## Additional Documentation

- [DI_ARCHITECTURE.md](./DI_ARCHITECTURE.md) - Complete Dependency Injection architecture
- [validation/README.md](./validation/README.md) - JSON Resume Validator details
- [store/README.md](./store/README.md) - Pinia Store guide with DI

## Contributing

When adding new features to this infrastructure layer:

1. Follow Clean Architecture principles
2. Define interfaces in the domain first
3. Implement adapters in infrastructure
4. Write unit tests with high coverage
5. Document in corresponding READMEs
6. Update this README if you added new modules

## License

See [LICENSE](../../../../../LICENSE) in the project root.
