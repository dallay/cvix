# Swagger Documentation Standard

Este documento define el estándar obligatorio para documentar todos los controllers REST en el proyecto CVIX.

## Estándar basado en

- `ContactController` - Mejor ejemplo de documentación completa
- `WaitlistController` - Segundo mejor ejemplo

## Estructura Obligatoria

### 1. Controller Class

```kotlin
/**
 * Controller for [feature description].
 *
 * [Detailed description of what this controller does, its purpose, and any special considerations]
 *
 * Uses header-based API versioning (API-Version: v1).
 *
 * ## Security Features: (if applicable)
 * - [Security feature 1]
 * - [Security feature 2]
 *
 * ## Special Notes: (if applicable)
 * - [Note 1]
 * - [Note 2]
 *
 * @property mediator The mediator for dispatching commands.
 * @property [otherProperty] Description of other injected properties.
 * @created [date] (optional, for historical tracking)
 */
@Validated  // If using Jakarta validation
@RestController
@RequestMapping(value = ["/api/[resource]"])
@Tag(
    name = "[Resource Name]",
    description = "[Resource] management endpoints"
)
class [Resource]Controller(
    private val mediator: Mediator,
    // other dependencies
) : ApiController(mediator) {
    // ...
}
```

### 2. Endpoint Method

```kotlin
/**
 * Endpoint for [operation description].
 *
 * [Detailed explanation of what this endpoint does, including:]
 * - Input validation rules
 * - Business logic flow
 * - Side effects
 * - Special considerations
 *
 * @param [param1] Description of parameter 1.
 * @param [param2] Description of parameter 2.
 * @return ResponseEntity with [success response type] or error response.
 */
@Operation(
    summary = "[Short action description]",
    description = "[Detailed description of what this operation does, including business logic and validation]",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",  // or 201 for creation
            description = "[Success description]",
            content = [Content(schema = Schema(implementation = [SuccessResponse]::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "[What causes a 400 error]",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication required",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "409",
            description = "[What causes a conflict]",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "429",
            description = "Rate limit exceeded",
            headers = [Header(name = "Retry-After", description = "Seconds until rate limit resets")],
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
    ],
)
@SecurityRequirement(name = "bearerAuth")  // If endpoint requires authentication
@[HttpMethod]Mapping(
    produces = ["application/vnd.api.v1+json"],
    consumes = ["application/json"],  // If endpoint accepts body
)
suspend fun [operationName](
    @Valid @RequestBody request: [Request],
    // other params
): ResponseEntity<[Response]> {
    // implementation
}
```

### 3. Request DTOs

**TODOS** los request DTOs DEBEN tener:

```kotlin
/**
 * Request body for [operation description].
 *
 * [Detailed description of the purpose and usage]
 *
 * @property [field1] Description including constraints and examples.
 * @property [field2] Description including constraints and examples.
 */
data class [Operation]Request(
    @field:NotBlank(message = "[Field] is required")
    @field:Size(min = X, max = Y, message = "[Field] must be between X and Y characters")
    @field:Schema(
        description = "[Detailed field description]",
        example = "[realistic example value]",
        required = true,  // or false
        minLength = X,     // if applicable
        maxLength = Y,     // if applicable
    )
    val [field]: String,

    @field:Email(message = "Email must be valid")
    @field:NotBlank(message = "Email is required")
    @field:Schema(
        description = "User's email address",
        example = "user@example.com",
        required = true,
        format = "email",
    )
    val email: String,

    @field:Pattern(regexp = "^[regex]$", message = "[Constraint description]")
    @field:Schema(
        description = "[Field description]",
        example = "[example]",
        allowableValues = ["value1", "value2"],  // if enum-like
        required = true,
    )
    val [enumField]: String,
)
```

### 4. Response DTOs

```kotlin
/**
 * Response for [operation description].
 *
 * [Detailed description of when this response is returned]
 *
 * @property success Whether the operation succeeded.
 * @property message Localized message describing the result.
 * @property data Optional data payload.
 */
@Schema(description = "[Operation] success response")
data class [Operation]Response(
    @field:Schema(
        description = "Operation success indicator",
        example = "true",
    )
    val success: Boolean,

    @field:Schema(
        description = "Localized message describing the result",
        example = "Operation completed successfully",
    )
    val message: String,

    @field:Schema(
        description = "[Optional data description]",
        nullable = true,
    )
    val data: [DataType]? = null,
)
```

## Códigos HTTP Estándar

| Code | Usage                                    | Cuando usarlo                                                      |
|------|------------------------------------------|--------------------------------------------------------------------|
| 200  | OK                                       | GET/PATCH/DELETE exitosos                                          |
| 201  | Created                                  | POST/PUT exitosos que crean recursos                               |
| 400  | Bad Request                              | Validación fallida, datos inválidos                                |
| 401  | Unauthorized                             | Token faltante o inválido                                          |
| 403  | Forbidden                                | Token válido pero sin permisos                                     |
| 404  | Not Found                                | Recurso no existe                                                  |
| 409  | Conflict                                 | Email duplicado, estado inválido, constraint violation             |
| 429  | Too Many Requests                        | Rate limit excedido                                                |
| 500  | Internal Server Error                    | Error inesperado del servidor                                      |

## Content Types Estándar

- **Produces**: `application/vnd.api.v1+json` (versioned API)
- **Consumes**: `application/json`

## Security Annotations

Para endpoints que requieren autenticación:

```kotlin
@Operation(
    summary = "...",
    security = [SecurityRequirement(name = "bearerAuth")],
)
```

## Validation Annotations

### String Fields
- `@NotBlank` - No null, empty, o solo whitespace
- `@Size(min, max)` - Longitud del string
- `@Email` - Formato email válido
- `@Pattern(regexp)` - Regex personalizada

### Numeric Fields
- `@NotNull` - No null
- `@Min(value)` - Valor mínimo
- `@Max(value)` - Valor máximo
- `@Positive` / `@PositiveOrZero`

### Collections
- `@NotEmpty` - No null y no vacío
- `@Size(min, max)` - Tamaño de la colección

## Best Practices

1. **KDoc primero, Swagger después**: Documentar en KDoc, luego en anotaciones Swagger
2. **Ejemplos realistas**: Usar ejemplos que representen datos reales
3. **Descripciones completas**: Explicar el "por qué", no solo el "qué"
4. **Todos los códigos HTTP**: Documentar TODAS las respuestas posibles
5. **Localización**: Usar `MessageSource` para mensajes localizados
6. **Logging**: Log en INFO para operaciones exitosas, WARN para errores de negocio, ERROR para fallos técnicos
7. **Content Schema**: SIEMPRE especificar `Content(schema = Schema(implementation = ...))`
8. **ProblemDetail**: Usar Spring's `ProblemDetail` para respuestas de error
9. **Headers**: Documentar headers especiales (ej: `Retry-After` para 429)

## Ejemplo Completo: ContactController

Ver `/server/engine/src/main/kotlin/com/cvix/contact/infrastructure/http/ContactController.kt`

Este controller es el estándar de oro para documentación Swagger en el proyecto.

## Verificación

Antes de commit:

```bash
make verify-all  # Debe pasar sin errores
```

Para ver la documentación generada:

1. Correr `./gradlew bootRun`
2. Abrir `http://localhost:8080/swagger-ui.html`

## Anti-Patterns a Evitar

❌ **MAL**:
```kotlin
@Operation(summary = "Login endpoint")
@ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "400", description = "Bad request"),
)
```

✅ **BIEN**:
```kotlin
@Operation(
    summary = "Authenticate user",
    description = "Authenticates a user with email and password, returning access and refresh tokens. " +
        "Optionally extends session duration if rememberMe is true.",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "Authentication successful, tokens returned",
            content = [Content(schema = Schema(implementation = AccessToken::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "Invalid email format or password does not meet security requirements",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Invalid credentials - email or password is incorrect",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "429",
            description = "Too many login attempts - rate limit exceeded",
            headers = [Header(name = "Retry-After", description = "Seconds until rate limit resets")],
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "500",
            description = "Internal server error during authentication",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
    ],
)
```

---

**Este estándar es OBLIGATORIO** para todos los controllers nuevos y debe aplicarse progresivamente a los existentes.
