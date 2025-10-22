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

- [X] T011 [US1] Create registration form component in `client/apps/webapp/src/features/authentication/presentation/components/RegisterForm.vue`
- [X] T012 [US1] Implement real-time validation logic using VeeValidate in `client/apps/webapp/src/features/authentication/domain/validators/auth.schema.ts`
- [X] T013 [US1] Create `RegisterUserCommand` in `server/engine/src/main/kotlin/com/loomify/engine/users/application/register/RegisterUserCommand.kt`
- [X] T014 [US1] Implement `RegisterUserCommandHandler` in `server/engine/src/main/kotlin/com/loomify/engine/users/application/register/RegisterUserCommandHandler.kt`
- [X] T015 [US1] Add `/api/auth/register` endpoint in `server/engine/src/main/kotlin/com/loomify/engine/users/infrastructure/http/UserRegisterController.kt`
- [X] T016 [US1] Write integration tests for registration flow in `server/engine/src/test/kotlin/com/loomify/engine/users`

### Phase 4: User Story 2 - User Login with Email/Password (P1)

- [X] T017 [US2] Create login form component in `client/apps/webapp/src/features/authentication/presentation/components/LoginForm.vue`
- [X] T018 [US2] Implement login logic using Axios in `client/apps/webapp/src/features/authentication/infrastructure/http/AuthHttpClient.ts`
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
- [X] T033 [US6] Implement logout logic in `client/apps/webapp/src/features/authentication/infrastructure/http/AuthHttpClient.ts`
- [X] T034 [US6] Create `LogoutUserCommand` in `server/engine/src/main/kotlin/com/loomify/engine/authentication/application/logout/UserLogoutCommand.kt`
- [X] T035 [US6] Implement `LogoutUserCommandHandler` in `server/engine/src/main/kotlin/com/loomify/engine/authentication/application/logout/UserLogoutCommandHandler.kt`
- [X] T036 [US6] Add `/api/auth/logout` endpoint in `server/engine/src/main/kotlin/com/loomify/engine/authentication/infrastructure/http/UserLogoutController.kt`
- [X] T037 [US6] Write integration tests for logout flow in `server/engine/src/test/kotlin/com/loomify/engine/authentication`

### Final Phase: Polish & Cross-Cutting Concerns

- [X] T038 Add rate limiting middleware to all authentication endpoints
  - **Performance Target**: Limit to 100 requests/min per user.

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
