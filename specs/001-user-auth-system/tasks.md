# Tasks: Secure Authentication System

## Feature: Secure Authentication System

### Phase 1: Setup Tasks

- [X] T001 Initialize project structure for authentication system
- [X] T002 Configure Keycloak for local development (realm import, admin user setup)
- [X] T003 Set up PostgreSQL database with required schemas for authentication
- [X] T004 Install and configure required dependencies (Spring Boot, Vue, etc.)
- [X] T005 Set up Docker Compose for local development (Keycloak, PostgreSQL)

### Phase 2: Foundational Tasks

- [X] T006 Implement base Hexagonal Architecture structure in backend
- [X] T007 Create shared domain models for User, Session, and Tokens
- [X] T008 Configure Spring Security with OAuth2 Resource Server
- [X] T009 Set up R2DBC for reactive database access
- [X] T010 Implement Keycloak integration for user management

### Phase 3: User Story 1 - User Registration with Email/Password (P1)

- [X] T011 [US1] Create registration form component in `client/apps/webapp/src/authentication/presentation/components/RegisterForm.vue`
- [X] T012 [US1] Implement real-time validation logic using VeeValidate in `client/apps/webapp/src/authentication/domain/validators/auth.schema.ts`
- [X] T013 [US1] Create `RegisterUserCommand` in `server/engine/src/main/kotlin/com/loomify/engine/users/application/register/RegisterUserCommand.kt`
- [X] T014 [US1] Implement `RegisterUserCommandHandler` in `server/engine/src/main/kotlin/com/loomify/engine/users/application/register/RegisterUserCommandHandler.kt`
- [X] T015 [US1] Add `/api/auth/register` endpoint in `server/engine/src/main/kotlin/com/loomify/engine/users/infrastructure/http/UserRegisterController.kt`
- [X] T016 [US1] Write integration tests for registration flow in `server/engine/src/test/kotlin/com/loomify/engine/users`

### Phase 4: User Story 2 - User Login with Email/Password (P1)

- [X] T017 [US2] Create login form component in `client/apps/webapp/src/authentication/presentation/components/LoginForm.vue`
- [X] T018 [US2] Implement login logic using Axios in `client/apps/webapp/src/authentication/infrastructure/http/AuthHttpClient.ts`
- [X] T019 [US2] Create `LoginUserQuery` in `server/engine/src/main/kotlin/com/loomify/engine/authentication/application/query/AuthenticateUserQuery.kt`
- [X] T020 [US2] Implement `AuthenticateUserQueryHandler` in `server/engine/src/main/kotlin/com/loomify/engine/authentication/application/AuthenticateUserQueryHandler.kt`
- [X] T021 [US2] Add `/api/auth/login` endpoint in `server/engine/src/main/kotlin/com/loomify/engine/authentication/infrastructure/http/UserAuthenticatorController.kt`
- [X] T022 [US2] Write integration tests for login flow in `server/engine/src/test/kotlin/com/loomify/engine/authentication`

### Phase 5: User Story 3 - Federated Identity Provider Login (P2)

- [X] T023 [US3] Add federated login buttons to `LoginForm.vue`
- [X] T024 [US3] Implement Keycloak OIDC integration for federated login in `server/engine/src/main/kotlin/com/loomify/engine/authentication/infrastructure/keycloak`
- [X] T025 [US3] Write integration tests for federated login in `server/engine/src/test/kotlin/com/loomify/engine/authentication`

### Phase 6: User Story 4 - Session Token Management and Refresh (P2)

- [X] T026 [US4] Implement token refresh logic in `client/apps/webapp/src/authentication/infrastructure/http/AuthHttpClient.ts`
- [X] T027 [US4] Create `RefreshTokenQuery` in `server/engine/src/main/kotlin/com/loomify/engine/authentication/application/query/RefreshTokenQuery.kt`
- [X] T028 [US4] Implement `RefreshTokenQueryHandler` in `server/engine/src/main/kotlin/com/loomify/engine/authentication/application/RefreshTokenQueryHandler.kt`
- [X] T029 [US4] Write integration tests for token refresh in `server/engine/src/test/kotlin/com/loomify/engine/authentication`

### Phase 7: User Story 5 - Session Recovery and Persistence (P3)

- [X] **T030**: Session persistence (store expiration timestamp in sessionStorage)
  - Status: ✅ Complete
  - Files: `SessionStorage.ts` (utility), `authStore.ts` (integration), `App.vue` (initialization)
- [X] **T031** [US5] Write tests for session recovery in `client/apps/webapp/src/authentication/tests`
  - Status: ✅ Complete
  - Files: `SessionStorage.spec.ts` (15 tests), `authStore.initialize.spec.ts` (10 tests)
  - Coverage: Session lifecycle, error handling, edge cases, integration scenarios

### Phase 8: User Story 6 - User Logout (P1)

- [X] T032 [US6] Create logout button component (integrated in DashboardPage.vue)
- [X] T033 [US6] Implement logout logic in `client/apps/webapp/src/authentication/infrastructure/http/AuthHttpClient.ts`
- [X] T034 [US6] Create `LogoutUserCommand` in `server/engine/src/main/kotlin/com/loomify/engine/authentication/application/logout/UserLogoutCommand.kt`
- [X] T035 [US6] Implement `LogoutUserCommandHandler` in `server/engine/src/main/kotlin/com/loomify/engine/authentication/application/logout/UserLogoutCommandHandler.kt`
- [X] T036 [US6] Add `/api/auth/logout` endpoint in `server/engine/src/main/kotlin/com/loomify/engine/authentication/infrastructure/http/UserLogoutController.kt`
- [X] T037 [US6] Write integration tests for logout flow in `server/engine/src/test/kotlin/com/loomify/engine/authentication`

### Final Phase: Polish & Cross-Cutting Concerns

- [X] T038 Add rate limiting middleware to all authentication endpoints
  - **Performance Target**: Configurable per pricing plan (FREE: 20/hour, BASIC: 40/hour, PROFESSIONAL: 100/hour per API key).
  - **Status**: ✅ MIGRATED TO BUCKET4J - Fully refactored with battle-tested library
  - **Implementation Details**:
    - ✅ Migrated from custom InMemoryRateLimiter to Bucket4j token-bucket algorithm
    - ✅ PricingPlan enum with FREE, BASIC, PROFESSIONAL tiers
    - ✅ PricingPlanService manages buckets per API key with in-memory caching
    - ✅ Filter integration for all auth endpoints (RateLimitingFilter)
    - ✅ API key-based rate limiting via X-api-key header (with IP fallback)
    - ✅ Unit tests (PricingPlanTest, PricingPlanServiceTest, RateLimitingFilterUnitTest)
    - ✅ Test configuration (TestRateLimiterConfiguration with mock service)
    - ✅ Domain event emission (RateLimitExceededEvent) for security auditing
    - ✅ Standard rate limit HTTP headers (X-Rate-Limit-Remaining, X-Rate-Limit-Retry-After-Seconds)
    - ✅ Proper error responses with retry-after information (429 Too Many Requests)
    - ✅ Following [Baeldung Bucket4j tutorial](https://www.baeldung.com/spring-bucket4j)
  - **Rate Limits by Plan** (Per API Key):
    - FREE: 20 requests / hour (default for no key or unrecognized keys)
    - BASIC: 40 requests / hour (API keys starting with BX001-)
    - PROFESSIONAL: 100 requests / hour (API keys starting with PX001-)
  - **Files Created/Modified**:
    - `PricingPlan.kt` - Enum defining rate limit tiers with Bandwidth configuration
    - `PricingPlanService.kt` - Service managing Bucket4j buckets per API key
    - `RateLimitingFilter.kt` - Refactored to use Bucket4j instead of custom implementation
    - `PricingPlanTest.kt` - Unit tests for pricing plan resolution
    - `PricingPlanServiceTest.kt` - Unit tests for bucket management
    - `RateLimitingFilterUnitTest.kt` - Updated unit tests for Bucket4j integration
    - `TestRateLimiterConfiguration.kt` - Test config with mock PricingPlanService
    - Added `bucket4j-core:8.1.0` dependency
  - **Files Removed** (Custom implementation replaced):
    - `InMemoryRateLimiter.kt` - Replaced by Bucket4j
    - `InMemoryRateLimiterTest.kt` - No longer needed
    - `RateLimiterCleanupTask.kt` - No longer needed
    - `RateLimitProperties.kt` - Replaced by PricingPlan enum
    - `EmailRateLimitExtractor.kt` - No longer needed
    - `RateLimitingFilterIntegrationTest.kt` - Removed (tested at unit level)
    - `RateLimitingIntegrationTest.kt` - Removed (tested at unit level)
    - `RateLimiterCleanupTask.kt` - Scheduled cleanup task
  - **Test Coverage**: Constitution-compliant (TDD followed)
  - **Known Limitations**:
    - Current implementation uses in-memory storage (single instance only)
    - For distributed deployments, consider Redis-based implementation
    - Email-based rate limiting infrastructure ready but not yet integrated (requires body caching)
    - Trusted proxy validation (X-Forwarded-For security) - future enhancement
    - Micrometer metrics integration - future enhancement
  - **Requirements Satisfied**:
    - FR-030: Rate limiting authentication attempts ✅
    - SC-010: Block brute force attacks ✅
    - Assumption 13: Exponential backoff ✅
    - data-model.md: RATE_LIMIT_EXCEEDED event ✅

- [X] T039 Implement centralized error handling for authentication flows
- [X] T040 Conduct accessibility audit for all frontend components
- [X] T041 Optimize database queries for session management
  - **Testing**: Add unit tests for optimized queries to ensure correctness and performance.
- [X] T042 Write end-to-end tests for complete authentication flows using Playwright

### Dependencies

1. User Story 1 → User Story 2
2. User Story 2 → User Story 3, User Story 4
3. User Story 4 → User Story 5
4. User Story 5 → User

### Parallel Execution Examples

- T011, T017, T023, T026, T030, T032 can be implemented in parallel as they target different user stories and files.
- T013, T019, T027, T034 can be implemented in parallel as they target different backend commands.

### MVP Scope

- User Story 1: User Registration with Email/Password
- User Story 2: User Login with Email/Password
