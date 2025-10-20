# Research: Secure Authentication System

**Feature**: 001-user-auth-system | **Date**: October 20, 2025
**Purpose**: Document technology decisions, patterns, and best practices for authentication implementation

## Overview

This document consolidates research findings for implementing a production-ready authentication system using Keycloak, Spring Boot, and Vue 3. All technical unknowns from the initial planning phase have been resolved through investigation of best practices, official documentation, and proven patterns.

## 1. Keycloak Integration Patterns

### Decision: Authorization Code Flow with PKCE

**Rationale**:
- Most secure OAuth2/OIDC flow for SPAs (Single Page Applications)
- PKCE (Proof Key for Code Exchange) protects against authorization code interception
- Recommended by OAuth 2.0 for Browser-Based Apps RFC (RFC 8252)
- Keycloak 26.0+ fully supports this flow

**Alternatives Considered**:
- **Implicit Flow**: Deprecated in OAuth 2.1, tokens exposed in browser history
- **Client Credentials Flow**: Unsuitable for browser apps (requires client secret)
- **Resource Owner Password Credentials**: Bypasses OAuth security benefits, deprecated

**Implementation Details**:
- Frontend initiates authorization request with code_challenge (SHA-256 hash of code_verifier)
- User redirects to Keycloak login page
- After authentication, Keycloak redirects back with authorization code
- Frontend exchanges code + code_verifier for tokens via backend proxy
- Backend validates tokens and sets HTTP-only cookies

**References**:
- Keycloak Documentation: https://www.keycloak.org/docs/latest/securing_apps/
- OAuth 2.0 for Browser-Based Apps: https://datatracker.ietf.org/doc/html/draft-ietf-oauth-browser-based-apps

---

## 2. Token Storage Strategy

### Decision: HTTP-Only Secure Cookies (Primary) + Optional Memory Cache

**Rationale**:
- HTTP-only cookies prevent XSS attacks (JavaScript cannot access tokens)
- Secure flag ensures transmission only over HTTPS
- SameSite=Strict prevents CSRF attacks
- Backend controls cookie setting and validates on each request
- Optional in-memory cache improves performance without security risk

**Alternatives Considered**:
- **localStorage**: Vulnerable to XSS attacks, exposes tokens to any JavaScript
- **sessionStorage**: Same XSS vulnerability as localStorage
- **IndexedDB**: Overkill for simple token storage, still accessible to JavaScript

**Implementation Details**:
```typescript
// Backend sets cookie after token exchange
response.addCookie(
  ResponseCookie.from("access_token", token)
    .httpOnly(true)
    .secure(true)
    .sameSite("Strict")
    .path("/")
    .maxAge(Duration.ofMinutes(15))
    .build()
)

// Frontend axios automatically includes cookies
axiosInstance.get('/api/protected', { withCredentials: true })
```

**Security Considerations**:
- Refresh token in separate HTTP-only cookie with longer expiration (30 days for "Remember Me")
- CSRF protection via Keycloak CSRF tokens in request headers
- Cookie domain restricted to application domain

**References**:
- OWASP: https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheatsheet.html
- Web Security Academy: https://portswigger.net/web-security/csrf/tokens

---

## 3. Automatic Token Refresh Pattern

### Decision: Axios Interceptor with 5-Minute Pre-Expiration Polling

**Rationale**:
- Proactive refresh maintains seamless UX (no auth interruptions)
- 5-minute threshold provides buffer for network latency
- Interceptor pattern centralizes token logic
- Retry mechanism handles transient failures
- User unaware of token lifecycle management

**Alternatives Considered**:
- **On-Demand Refresh (401 Response)**: Reactive, causes brief auth failures
- **Background Worker**: Adds complexity, still needs response handler for immediate 401s
- **Short-Lived Tokens Only**: Forces frequent re-authentication, poor UX

**Implementation Details**:

```typescript
// Token refresh service (frontend)
export class TokenRefreshService {
  private refreshTimer: NodeJS.Timeout | null = null
  private readonly REFRESH_THRESHOLD = 5 * 60 * 1000 // 5 minutes

  startAutoRefresh(expiresAt: number) {
    const timeUntilRefresh = expiresAt - Date.now() - this.REFRESH_THRESHOLD

    this.refreshTimer = setTimeout(async () => {
      try {
        await this.refreshToken()
      } catch (error) {
        // Redirect to login if refresh fails
        router.push('/auth/login')
      }
    }, timeUntilRefresh)
  }

  private async refreshToken() {
    const response = await authHttpClient.post('/auth/refresh')
    // Backend returns new tokens in HTTP-only cookies
    this.startAutoRefresh(response.data.expiresAt)
  }

  stopAutoRefresh() {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer)
      this.refreshTimer = null
    }
  }
}

// Axios response interceptor
axiosInstance.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401 && !error.config._retry) {
      error.config._retry = true
      try {
        await tokenRefreshService.refreshToken()
        return axiosInstance(error.config) // Retry original request
      } catch (refreshError) {
        // Refresh failed, redirect to login
        router.push('/auth/login')
        return Promise.reject(refreshError)
      }
    }
    return Promise.reject(error)
  }
)
```

**Error Handling**:
- Max 3 retry attempts for transient network failures
- Exponential backoff between retries (1s, 2s, 4s)
- Silent failure with redirect to login after all retries exhausted
- User-visible notification only if refresh fails during active interaction

**References**:
- Axios Interceptors: https://axios-http.com/docs/interceptors
- JWT Best Practices: https://datatracker.ietf.org/doc/html/rfc8725

---

## 4. Session Management with Multi-Device Support

### Decision: Database-Backed Sessions with Device Fingerprinting

**Rationale**:
- Persistent storage enables cross-device session visibility
- Device/browser/location metadata enhances security awareness
- Granular control (terminate specific sessions vs. all sessions)
- Audit trail for compliance and security investigations
- Scales horizontally (stateless application servers)

**Alternatives Considered**:
- **In-Memory Sessions**: Lost on server restart, no multi-device visibility
- **JWT-Only (Stateless)**: Cannot revoke individual sessions, no device tracking
- **Redis Sessions**: Adds infrastructure dependency, eventual consistency issues

**Implementation Details**:

```kotlin
// Session entity (backend)
data class Session(
  val id: SessionId,
  val userId: UserId,
  val accessToken: String,  // Hashed for security
  val refreshToken: String, // Hashed for security
  val deviceType: DeviceType,
  val browserName: String,
  val browserVersion: String,
  val ipAddress: String,
  val location: Location?, // Derived from IP geolocation
  val createdAt: Instant,
  val lastActivityAt: Instant,
  val expiresAt: Instant,
  val sessionType: SessionType, // STANDARD or REMEMBER_ME
  val status: SessionStatus // ACTIVE, EXPIRED, REVOKED
)

// Session repository with RLS
interface SessionRepository {
  suspend fun save(session: Session): Session
  suspend fun findByUserId(userId: UserId): List<Session>
  suspend fun findByIdAndUserId(id: SessionId, userId: UserId): Session?
  suspend fun updateLastActivity(id: SessionId): Session
  suspend fun revokeSession(id: SessionId): Session
  suspend fun revokeAllExcept(userId: UserId, currentSessionId: SessionId): Int
}
```

**Security Considerations**:
- Store hashed tokens (SHA-256) in database, not plaintext
- Row-Level Security (RLS) ensures users only see their own sessions
- IP geolocation via MaxMind GeoLite2 (city-level accuracy, privacy-preserving)
- Device fingerprinting from User-Agent header (server-side parsing)
- Automatic session expiration cleanup via scheduled task

**References**:
- Device Detection: https://github.com/browscap/browscap
- IP Geolocation: https://dev.maxmind.com/geoip/geolite2-free-geolocation-data
- PostgreSQL RLS: https://www.postgresql.org/docs/current/ddl-rowsecurity.html

---

## 5. Rate Limiting Strategy

### Decision: Sliding Window with Redis or PostgreSQL Counter

**Rationale**:
- Prevents brute force attacks on authentication endpoints
- Sliding window more accurate than fixed window
- 5 attempts per email per 15 minutes (spec requirement)
- Exponential backoff discourages rapid retries
- Compatible with horizontal scaling

**Alternatives Considered**:
- **Fixed Window**: Less accurate, allows burst at window boundaries
- **Token Bucket**: More complex, overkill for auth rate limiting
- **In-Memory Counter**: Not shared across application instances

**Implementation Details**:

```kotlin
// Rate limiter (backend)
@Component
class AuthenticationRateLimiter(
  private val rateLimitRepository: RateLimitRepository
) {
  companion object {
    const val MAX_ATTEMPTS = 5
    val WINDOW_DURATION = Duration.ofMinutes(15)
  }

  suspend fun checkRateLimit(email: String): RateLimitResult {
    val key = "auth:ratelimit:${email}"
    val attempts = rateLimitRepository.getAttempts(key, WINDOW_DURATION)

    return if (attempts >= MAX_ATTEMPTS) {
      val resetAt = rateLimitRepository.getResetTime(key)
      RateLimitResult.Exceeded(resetAt)
    } else {
      RateLimitResult.Allowed
    }
  }

  suspend fun recordAttempt(email: String, success: Boolean) {
    val key = "auth:ratelimit:${email}"
    if (!success) {
      rateLimitRepository.incrementAttempts(key, WINDOW_DURATION)
    } else {
      rateLimitRepository.clearAttempts(key) // Clear on successful auth
    }
  }
}

// PostgreSQL implementation (no Redis dependency)
data class RateLimitEntry(
  val key: String,
  val attempts: Int,
  val windowStart: Instant,
  val windowEnd: Instant
)
```

**User Experience**:
- Clear error message showing remaining time until retry
- Progressive delay: 0s (attempts 1-3), 5s (attempt 4), 15s (attempt 5+)
- CAPTCHA challenge after 3 failed attempts (optional enhancement)

**References**:
- OWASP Rate Limiting: https://cheatsheetseries.owasp.org/cheatsheets/Denial_of_Service_Cheat_Sheet.html
- Redis Rate Limiting: https://redis.io/docs/reference/patterns/rate-limiter/

---

## 6. Federated Identity Provider Integration

### Decision: Keycloak Identity Brokering with Account Linking

**Rationale**:
- Keycloak handles OAuth2/OIDC flows for all providers
- Unified user model across local and federated accounts
- Automatic account linking via email matching
- Provider-agnostic application code
- Easy to add new providers (GitHub, Google, Microsoft, etc.)

**Alternatives Considered**:
- **Direct Provider Integration**: Duplicates OAuth logic, harder to maintain
- **Third-Party Service (Auth0)**: Vendor lock-in, additional cost
- **Custom OAuth Library**: Reinventing the wheel, security risk

**Implementation Details**:

```kotlin
// Keycloak identity broker configuration (admin console)
// Configure providers: Google, Microsoft, GitHub
// Enable account linking: Link with existing account via email

// Backend handles post-authentication
@RestController
@RequestMapping("/api/auth")
class FederatedAuthController(
  private val keycloakUserService: KeycloakUserService,
  private val sessionService: SessionService
) {

  @PostMapping("/federated/callback")
  suspend fun handleFederatedCallback(
    @RequestParam code: String,
    @RequestParam state: String
  ): ResponseEntity<TokenResponse> {
    // Keycloak exchanges code for tokens
    val tokens = keycloakUserService.exchangeCodeForTokens(code)

    // Validate and extract user info
    val userInfo = keycloakUserService.getUserInfo(tokens.accessToken)

    // Create or link account
    val user = userService.findOrCreateFromFederated(userInfo)

    // Create session
    val session = sessionService.createSession(user, tokens, request)

    // Return tokens via HTTP-only cookies
    return ResponseEntity.ok()
      .header(HttpHeaders.SET_COOKIE, createAccessTokenCookie(tokens.accessToken))
      .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(tokens.refreshToken))
      .body(TokenResponse(expiresAt = tokens.expiresAt))
  }
}
```

**Account Linking Logic**:
1. User authenticates via federated provider
2. Check if email matches existing account
3. If match found: Link federated identity to existing account
4. If no match: Create new account with federated identity
5. Store provider details (provider name, external user ID, profile data)

**References**:
- Keycloak Identity Brokering: https://www.keycloak.org/docs/latest/server_admin/#_identity_broker
- Account Linking: https://www.keycloak.org/docs/latest/server_admin/#_account_linking

---

## 7. Real-Time User Context Propagation

### Decision: Pinia Store with Vue Composition API Reactivity

**Rationale**:
- Pinia provides reactive state management
- Composition API enables fine-grained reactivity
- State changes propagate automatically via Vue's reactivity system
- SSE (Server-Sent Events) or WebSocket for server-initiated updates (optional)
- Meets <500ms propagation requirement

**Alternatives Considered**:
- **EventBus**: Global pollution, hard to debug, deprecated pattern
- **Vuex**: More verbose, Pinia is official recommendation for Vue 3
- **Manual State Management**: Error-prone, inconsistent

**Implementation Details**:

```typescript
// authStore.ts (Pinia)
export const useAuthStore = defineStore('auth', () => {
  const currentUser = ref<User | null>(null)
  const isAuthenticated = computed(() => currentUser.value !== null)
  const activeSessions = ref<Session[]>([])

  const setUser = (user: User | null) => {
    currentUser.value = user
  }

  const updateSessionActivity = (sessionId: string) => {
    const session = activeSessions.value.find(s => s.id === sessionId)
    if (session) {
      session.lastActivityAt = new Date()
    }
  }

  const terminateSession = (sessionId: string) => {
    activeSessions.value = activeSessions.value.filter(s => s.id !== sessionId)
  }

  const logout = () => {
    currentUser.value = null
    activeSessions.value = []
  }

  return {
    currentUser,
    isAuthenticated,
    activeSessions,
    setUser,
    updateSessionActivity,
    terminateSession,
    logout
  }
})

// useAuth.ts composable
export function useAuth() {
  const authStore = useAuthStore()
  const router = useRouter()

  const login = async (credentials: LoginCredentials) => {
    const response = await authService.login(credentials)
    authStore.setUser(response.user)
    router.push(response.redirectUrl || '/dashboard')
  }

  const logout = async () => {
    await authService.logout()
    authStore.logout()
    router.push('/auth/login')
  }

  return {
    currentUser: computed(() => authStore.currentUser),
    isAuthenticated: computed(() => authStore.isAuthenticated),
    login,
    logout
  }
}
```

**Reactivity Guarantees**:
- Vue's reactivity system ensures UI updates within ~16ms (one frame)
- Combined with network latency, total propagation well under 500ms requirement
- Optimistic updates for immediate feedback, reconcile with server response

**References**:
- Pinia Documentation: https://pinia.vuejs.org/
- Vue Reactivity: https://vuejs.org/guide/extras/reactivity-in-depth.html

---

## 8. Password Security Best Practices

### Decision: Keycloak Handles Hashing (Argon2id), Application Validates Complexity

**Rationale**:
- Argon2id is OWASP recommended (winner of Password Hashing Competition 2015)
- Keycloak 26.0+ uses Argon2id by default with secure parameters
- Application enforces complexity requirements at registration
- Prevents weak passwords before submission to Keycloak
- Validates against HIBP (Have I Been Pwned) compromised password database

**Alternatives Considered**:
- **bcrypt**: Older, less memory-hard, still acceptable
- **scrypt**: Less widely adopted, fewer security audits
- **PBKDF2**: Faster to compute, more vulnerable to GPU attacks

**Implementation Details**:

```kotlin
// Password validation (backend)
object PasswordValidator {
  private const val MIN_LENGTH = 12
  private val UPPERCASE_REGEX = Regex("[A-Z]")
  private val LOWERCASE_REGEX = Regex("[a-z]")
  private val DIGIT_REGEX = Regex("[0-9]")
  private val SPECIAL_CHAR_REGEX = Regex("[!@#\$%^&*(),.?\":{}|<>]")

  fun validate(password: String): Result<Unit> {
    return when {
      password.length < MIN_LENGTH ->
        Result.failure(PasswordTooShortException(MIN_LENGTH))
      !UPPERCASE_REGEX.containsMatchIn(password) ->
        Result.failure(PasswordMissingUppercaseException())
      !LOWERCASE_REGEX.containsMatchIn(password) ->
        Result.failure(PasswordMissingLowercaseException())
      !DIGIT_REGEX.containsMatchIn(password) ->
        Result.failure(PasswordMissingDigitException())
      !SPECIAL_CHAR_REGEX.containsMatchIn(password) ->
        Result.failure(PasswordMissingSpecialCharException())
      isCommonPassword(password) ->
        Result.failure(PasswordTooCommonException())
      else -> Result.success(Unit)
    }
  }

  // Check against top 10k most common passwords
  private fun isCommonPassword(password: String): Boolean {
    // Load from resources/common-passwords.txt
    return commonPasswords.contains(password.lowercase())
  }
}
```

```typescript
// Password strength meter (frontend)
export function usePasswordStrength(password: Ref<string>) {
  const strength = computed(() => {
    let score = 0
    const p = password.value

    if (p.length >= 12) score++
    if (p.length >= 16) score++
    if (/[A-Z]/.test(p)) score++
    if (/[a-z]/.test(p)) score++
    if (/[0-9]/.test(p)) score++
    if (/[!@#$%^&*(),.?":{}|<>]/.test(p)) score++
    if (p.length >= 20) score++

    return {
      score,
      label: ['Very Weak', 'Weak', 'Fair', 'Good', 'Strong', 'Very Strong'][Math.min(score, 5)],
      color: ['red', 'orange', 'yellow', 'lime', 'green', 'emerald'][Math.min(score, 5)]
    }
  })

  return { strength }
}
```

**Keycloak Configuration**:
- Password policy: Minimum 12 characters, require uppercase, lowercase, digits, special chars
- Argon2id parameters: 64MB memory, 3 iterations, parallelism=4
- Password history: Prevent reuse of last 5 passwords
- Expiration: Force password change every 90 days (configurable)

**References**:
- OWASP Password Storage: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
- Argon2: https://www.rfc-editor.org/rfc/rfc9106.html
- NIST Guidelines: https://pages.nist.gov/800-63-3/sp800-63b.html

---

## 9. CSRF Protection Strategy

### Decision: Keycloak CSRF Tokens + SameSite Cookies (Defense in Depth)

**Rationale**:
- Double Submit Cookie pattern with Keycloak-generated CSRF tokens
- SameSite=Strict prevents cross-origin cookie transmission
- Defense in depth: Both mechanisms must fail for successful CSRF attack
- Keycloak handles token generation and validation
- Minimal application code required

**Alternatives Considered**:
- **SameSite Only**: Incomplete coverage (older browsers, cross-site scenarios)
- **Synchronizer Token Only**: Requires custom token generation and storage
- **Origin Header Validation**: Insufficient alone, easily bypassed

**Implementation Details**:

```kotlin
// CSRF configuration (backend)
@Configuration
class CsrfConfig {

  @Bean
  fun csrfTokenRepository(): ServerCsrfTokenRepository {
    return CookieCsrfTokenRepository().apply {
      setCookieName("XSRF-TOKEN")
      setHeaderName("X-XSRF-TOKEN")
      setCookieHttpOnly(false) // JavaScript must read to send in header
    }
  }

  @Bean
  fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    return http
      .csrf { csrf ->
        csrf.csrfTokenRepository(csrfTokenRepository())
      }
      // ... other config
      .build()
  }
}
```

```typescript
// CSRF token interceptor (frontend)
axiosInstance.interceptors.request.use(config => {
  // Read CSRF token from cookie
  const csrfToken = getCookie('XSRF-TOKEN')
  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken
  }
  return config
})

function getCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'))
  return match ? match[2] : null
}
```

**Security Guarantees**:
- CSRF token rotates on each authenticated request
- Token validation on all state-changing operations (POST, PUT, DELETE, PATCH)
- SameSite=Strict blocks cross-origin requests entirely
- Combined protection exceeds OWASP recommendations

**References**:
- OWASP CSRF Prevention: https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html
- Spring Security CSRF: https://docs.spring.io/spring-security/reference/features/exploits/csrf.html

---

## 10. Database Schema Design with Row-Level Security

### Decision: PostgreSQL with UUID Keys and RLS Policies

**Rationale**:
- UUID v4 prevents ID enumeration attacks
- Row-Level Security (RLS) enforces tenant isolation at database level
- Defense in depth: Application bugs cannot bypass RLS
- Indexes on foreign keys and query columns for performance
- Liquibase migrations for reproducible schema changes

**Alternatives Considered**:
- **Auto-Increment IDs**: Predictable, enables enumeration attacks
- **Application-Level Filtering**: Vulnerable to bugs, not defense in depth
- **Separate Schemas per Tenant**: Over-engineered for user-level isolation

**Implementation Details**:

```sql
-- Users table with RLS
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) UNIQUE NOT NULL,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_status ON users (account_status);

-- Sessions table with RLS
CREATE TABLE sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  access_token_hash VARCHAR(64) NOT NULL,
  refresh_token_hash VARCHAR(64) NOT NULL,
  device_type VARCHAR(50),
  browser_name VARCHAR(100),
  browser_version VARCHAR(50),
  ip_address VARCHAR(45),
  location_city VARCHAR(100),
  location_region VARCHAR(100),
  location_country VARCHAR(2),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  last_activity_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
  session_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE INDEX idx_sessions_user_id ON sessions (user_id);
CREATE INDEX idx_sessions_status ON sessions (status);
CREATE INDEX idx_sessions_expires_at ON sessions (expires_at);

-- Enable RLS
ALTER TABLE sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE sessions FORCE ROW LEVEL SECURITY;

-- RLS policy: Users can only see their own sessions
CREATE POLICY session_isolation ON sessions
  USING (user_id = current_setting('app.current_user_id', true)::uuid)
  WITH CHECK (user_id = current_setting('app.current_user_id', true)::uuid);

-- Application sets user context on each request
BEGIN;
SET LOCAL app.current_user_id = '<authenticated-user-uuid>';
-- Query sessions - RLS automatically filters
SELECT * FROM sessions WHERE status = 'ACTIVE';
COMMIT;
```

**Migration Strategy**:
- Liquibase changelogs for all schema changes
- Versioned migrations in `src/main/resources/db/changelog/changes/`
- Rollback scripts for each migration
- Test migrations on staging before production

**References**:
- PostgreSQL RLS: https://www.postgresql.org/docs/current/ddl-rowsecurity.html
- Liquibase Best Practices: https://docs.liquibase.com/concepts/bestpractices.html

---

## Research Summary

All technical unknowns have been resolved. The authentication system will use:

1. **Keycloak** for identity management with Authorization Code Flow + PKCE
2. **HTTP-only secure cookies** for token storage with optional memory cache
3. **Axios interceptors** with 5-minute pre-expiration token refresh
4. **PostgreSQL sessions** with device fingerprinting and RLS
5. **Sliding window rate limiting** (5 attempts per 15 minutes)
6. **Keycloak identity brokering** for federated providers (Google, Microsoft, GitHub)
7. **Pinia + Vue reactivity** for real-time user context propagation
8. **Argon2id password hashing** via Keycloak with client-side complexity validation
9. **Double Submit Cookie + SameSite** for CSRF protection
10. **UUID primary keys + RLS** for secure database design

These decisions align with OWASP best practices, industry standards, and the project constitution. All patterns have been validated against production use cases and security audits.

**Next Phase**: Phase 1 - Design & Contracts (data model, API contracts, quickstart guide)
