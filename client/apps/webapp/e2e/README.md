# E2E Testing Strategy

Este proyecto utiliza dos estrategias de mocking para tests E2E sin depender del backend:

## Estrategia 1: Manual Mocking (Actual)

**Archivos:** `authentication-flows.spec.ts`, `helpers/auth.helper.ts`

**Pros:**

- Control total sobre respuestas
- Fácil de debugear
- No depende de requests exactas
- Tests específicos por caso

**Contras:**

- Más código manual
- Hay que actualizar mocks cuando cambia la API

**Uso:**

```bash
# Correr tests normalmente
pnpm test:e2e
```

---

## Estrategia 2: HAR Files (Nuevo - Experimental)

**Archivos:** `fixtures/harFixture.ts`, `fixtures/har/api-responses.har`

**Pros:**

- Graba interacciones reales del backend
- Actualización automática con `RECORD_HAR=true`
- Menos código manual

**Contras:**

- Depende de headers/cookies exactos
- Más difícil de debugear
- HAR files pueden ser grandes

### Uso de HAR Files

#### 1. Grabar HAR (una vez, con backend corriendo)

```bash
# Levantar backend primero
./gradlew :server:engine:bootRun

# En otra terminal, grabar HAR
RECORD_HAR=true pnpm test:e2e
```

Esto crea/actualiza `e2e/fixtures/har/api-responses.har` con las respuestas reales del backend.

#### 2. Reproducir HAR (sin backend)

```bash
# CI/CD (automático)
USE_HAR=true pnpm test:e2e

# Local
USE_HAR=true pnpm test:e2e
```

#### 3. Modo normal (sin HAR, con backend)

```bash
# Requiere backend corriendo
pnpm test:e2e
```

---

## Cuándo usar cada estrategia

| Escenario                        | Usar                    |
|----------------------------------|-------------------------|
| Tests de UI pura (clics, forms)  | Manual Mocking (Actual) |
| Tests con flows complejos de API | HAR Files               |
| CI/CD pipelines                  | Manual Mocking o HAR    |
| Desarrollo local sin backend     | Manual Mocking          |

---

## Convertir un test a HAR

1. Crear el test usando el HAR fixture:

```typescript
import {test, expect} from '../fixtures/harFixture';

test('mi test con HAR', async ({page}) => {
    await page.goto('/');
    // Interactuar con la app, HAR se encarga del resto
});
```

2. Grabar HAR con backend corriendo:

```bash
RECORD_HAR=true pnpm test:e2e
```

3. Correr sin backend:

```bash
USE_HAR=true pnpm test:e2e
```

---

## Variables de Entorno

| Variable     | Valor  | Efecto                                      |
|--------------|--------|---------------------------------------------|
| `USE_HAR`    | `true` | Reproduce HAR file (automático en CI)       |
| `RECORD_HAR` | `true` | Graba/actualiza HAR file (requiere backend) |
| `CI`         | `true` | Activa USE_HAR automáticamente              |

---

## Estructura de Archivos

```text
e2e/
├── authentication-flows.spec.ts    # Tests con manual mocking
├── fixtures/
│   ├── harFixture.ts              # HAR fixture helper
│   └── har/
│       └── api-responses.har      # Respuestas grabadas
└── helpers/
    └── auth.helper.ts              # Manual mocking helpers
```

---

## Recomendación

Para este proyecto, **manual mocking es más mantenible** porque:

1. La API es simple y controlable
2. Los tests ya están escritos con manual mocking
3. No hay flows complejos de múltiples requests
4. Es más fácil debugear cuando algo falla

**Usar HAR solo si:**

- Tienes muchos flows complejos que grabar
- El backend ya está estable y no cambia mucho
- Quieres reducir código de mocking manual
