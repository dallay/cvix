# Resume Store - Dependency Injection Pattern

Este documento explica cómo se implementa y utiliza la inyección de dependencias en el Resume Store.

## Arquitectura

El store utiliza el patrón de **Dependency Injection** (DI) mediante el sistema `provide/inject` de Vue para desacoplar la implementación del validador de la lógica del store.

### Ventajas de este enfoque:

1. **Testabilidad**: Fácil de mockear el validador en tests
2. **Flexibilidad**: Puedes cambiar la implementación del validador sin modificar el store
3. **Clean Architecture**: Respeta los principios SOLID y mantiene las capas separadas
4. **Configuración centralizada**: La DI se configura en un solo lugar

## Estructura de archivos

```
infrastructure/
├── di/
│   ├── keys.ts          # Claves de inyección
│   └── index.ts         # Barrel export
├── config/
│   ├── di.ts            # Configuración de DI
│   └── index.ts         # Barrel export
├── store/
│   ├── resumeStore.ts   # Store de Pinia
│   └── resumeStore.test.ts
└── validation/
    └── JsonResumeValidator.ts
```

## Configuración

### 1. Configurar DI en `main.ts`

```typescript
import { createApp } from 'vue';
import App from './App.vue';
import { setupResumeDI } from '@/core/resume/infrastructure/config';

const app = createApp(App);

// Configurar inyección de dependencias para el módulo resume
setupResumeDI(app);

app.mount('#app');
```

### 2. Usar el store en componentes

```typescript
import { useResumeStore } from '@/core/resume/infrastructure/store/resumeStore';

export default {
  setup() {
    const resumeStore = useResumeStore();

    // El store automáticamente usa el validador inyectado
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

## API del Store

### State

- **`resume`**: `Resume | null` - El currículum actual
- **`isGenerating`**: `boolean` - Indica si se está generando un currículum
- **`generationError`**: `ProblemDetail | null` - Error de generación si existe

### Computed

- **`isValid`**: `boolean` - Indica si el currículum actual es válido según JSON Resume Schema
- **`hasResume`**: `boolean` - Indica si hay un currículum cargado

### Actions

#### `setResume(newResume: Resume): void`
Establece un nuevo currículum y lo valida automáticamente.

```typescript
const resume = createResume();
resumeStore.setResume(resume);
console.log(resumeStore.isValid); // Validación automática
```

#### `clearResume(): void`
Limpia el currículum actual y los errores.

```typescript
resumeStore.clearResume();
console.log(resumeStore.hasResume); // false
```

#### `validateResume(): boolean`
Valida explícitamente el currículum actual.

```typescript
const isValid = resumeStore.validateResume();
if (!isValid) {
  console.error('Resume validation failed');
}
```

#### `setGenerating(generating: boolean): void`
Establece el estado de generación.

```typescript
resumeStore.setGenerating(true);
// ... realizar generación ...
resumeStore.setGenerating(false);
```

#### `setGenerationError(error: ProblemDetail | null): void`
Establece un error de generación.

```typescript
try {
  // ... generar currículum ...
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

### Test con el validador por defecto

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

### Test con un validador mock (DI)

```typescript
import { createApp } from 'vue';
import { createPinia, setActivePinia } from 'pinia';
import { RESUME_VALIDATOR_KEY } from '@/core/resume/infrastructure/di';
import { useResumeStore } from './resumeStore';

describe('Resume Store with Mock Validator', () => {
  it('should use injected validator', () => {
    // Crear validador mock
    const mockValidator = {
      validate: vi.fn().mockReturnValue(true)
    };

    // Configurar app con DI
    const app = createApp({});
    app.provide(RESUME_VALIDATOR_KEY, mockValidator);

    const pinia = createPinia();
    app.use(pinia);
    setActivePinia(pinia);

    // Usar el store
    const store = useResumeStore();
    const resume = createResume();

    store.setResume(resume);

    // Verificar que se usó el validador inyectado
    expect(mockValidator.validate).toHaveBeenCalledWith(resume);
    expect(store.isValid).toBe(true);
  });
});
```

## Inyección personalizada de validador

Si necesitas usar un validador personalizado en desarrollo o testing:

```typescript
import { createApp } from 'vue';
import { RESUME_VALIDATOR_KEY } from '@/core/resume/infrastructure/di';

class CustomResumeValidator implements ResumeValidator {
  validate(resume: Resume): boolean {
    // Lógica personalizada
    return true;
  }
}

const app = createApp(App);
app.provide(RESUME_VALIDATOR_KEY, new CustomResumeValidator());
```

## Patrón de Fallback

El store implementa un patrón de fallback: si no se inyecta un validador, usa automáticamente `JsonResumeValidator`:

```typescript
const validator: ResumeValidator =
  inject(RESUME_VALIDATOR_KEY) ?? new JsonResumeValidator();
```

Esto garantiza que el store siempre funcione, incluso si se olvida configurar la DI.

## Extensión futura

Para agregar más dependencias al módulo de resume:

1. Crear nuevas keys en `di/keys.ts`:
```typescript
export const RESUME_GENERATOR_KEY: InjectionKey<ResumeGenerator> =
  Symbol('ResumeGenerator');
```

2. Registrarlas en `config/di.ts`:
```typescript
export function setupResumeDI(app: App): void {
  app.provide(RESUME_VALIDATOR_KEY, new JsonResumeValidator());
  app.provide(RESUME_GENERATOR_KEY, new AiResumeGenerator());
}
```

3. Inyectarlas en el store:
```typescript
const validator = inject(RESUME_VALIDATOR_KEY) ?? new JsonResumeValidator();
const generator = inject(RESUME_GENERATOR_KEY) ?? new DefaultGenerator();
```

## Mejores prácticas

1. **Siempre proporciona un fallback** para que el store funcione sin configuración
2. **Usa InjectionKey tipado** para type safety
3. **Centraliza la configuración de DI** en `config/di.ts`
4. **Documenta las dependencias** requeridas por cada módulo
5. **Usa mocks en tests** para aislar la lógica del store
