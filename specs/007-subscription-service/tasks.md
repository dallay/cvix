# Tasks: Subscription Service

**Feature**: Subscription Service
**Status**: Pending
**Spec**: [specs/007-subscription-service/spec.md](spec.md)

## Phase 1: Setup

- [ ] T001 Verify `shared/subscription` module build configuration in `shared/subscription/build.gradle.kts`
- [ ] T002 Create package structure `com.cvix.subscription` in `shared/subscription/src/main/kotlin`
- [ ] T003 Create `SubscriptionApplication` or configuration class if needed in `shared/subscription/src/main/kotlin/com/cvix/subscription/infrastructure/config/SubscriptionConfig.kt`
- [ ] T003a Configure Authentication/Authorization (OAuth2/JWT) and security filters in `shared/subscription/src/main/kotlin/com/cvix/subscription/infrastructure/config/SecurityConfig.kt`.
    - Rule: Public access for `POST /subscriptions`.
    - Rule: Auth required for `POST /subscriptions/{id}/confirm`.
    - Rule: `ROLE_ADMIN` required for `DELETE /subscriptions/{id}` and `GET /subscriptions/export`.
    - Rule: Enable CSRF for browser clients, disable for API clients.

## Phase 2: Foundational (Blocking)

*Goal: Establish database schema and core domain entities.*

- [ ] T004 Create Liquibase changelog for `subscriptions` table and `subscription_status` enum in `shared/subscription/src/main/resources/db/changelog/migrations/007-subscription/001-create-subscriptions-table.yaml`
- [ ] T005 Create `Subscription` domain entity in `shared/subscription/src/main/kotlin/com/cvix/subscription/domain/model/Subscription.kt`
- [ ] T006 Create `SubscriptionRepository` port interface in `shared/subscription/src/main/kotlin/com/cvix/subscription/domain/port/SubscriptionRepository.kt`
- [ ] T007 Implement R2DBC repository adapter `SubscriptionR2dbcRepository` in `shared/subscription/src/main/kotlin/com/cvix/subscription/infrastructure/persistence/SubscriptionR2dbcRepository.kt`
- [ ] T008 Create integration test base class `SubscriptionIntegrationTest` in `shared/subscription/src/test/kotlin/com/cvix/subscription/SubscriptionIntegrationTest.kt`

## Phase 3: Submit Email Capture (User Story 1 - P1)

*Goal: Core functionality to capture emails with deduplication.*

- [ ] T009 [US1] Define `CreateSubscriptionCommand` in `shared/subscription/src/main/kotlin/com/cvix/subscription/application/port/in/CreateSubscriptionCommand.kt` and create the `CreateSubscriptionCommandHandler` to process it.
- [ ] T010 [US1] Implement `SubscriptionService` with `create` method in `shared/subscription/src/main/kotlin/com/cvix/subscription/application/service/SubscriptionService.kt`
- [ ] T011 [US1] Add email normalization and validation logic (Apache Commons) to `SubscriptionService`
- [ ] T012 [US1] Implement deduplication (idempotency) logic in `SubscriptionService` (Standardize on 200 OK for duplicates)
- [ ] T013 [US1] Create `SubscriptionController` in `shared/subscription/src/main/kotlin/com/cvix/subscription/infrastructure/web/SubscriptionController.kt`
- [ ] T014 [US1] Implement `POST /subscriptions` endpoint in `SubscriptionController`
- [ ] T015 [US1] Write integration test for successful capture in `shared/subscription/src/test/kotlin/com/cvix/subscription/application/CreateSubscriptionTest.kt`
- [ ] T016 [US1] Write integration test for duplicate handling in `shared/subscription/src/test/kotlin/com/cvix/subscription/application/CreateSubscriptionIdempotencyTest.kt`

## Phase 4: Metadata Registrations (User Story 2 - P2)

*Goal: Support arbitrary metadata with validation.*

- [ ] T017 [US2] Add metadata validation constants/config to `SubscriptionService`
- [ ] T018 [US2] Update `SubscriptionService.create` to validate metadata size and constraints (Option B safe default)
- [ ] T019 [US2] Add `findAllByMetadata` method to `SubscriptionRepository` and Adapter
- [ ] T020 [US2] Write integration test for metadata persistence and validation in `shared/subscription/src/test/kotlin/com/cvix/subscription/application/SubscriptionMetadataTest.kt`

## Phase 5: Notifications (User Story 3 - P3)

*Goal: Transactional Outbox for reliable downstream events.*

- [ ] T021 [US3] Create Liquibase changelog for `subscription_outbox_events` and cleanup index in `shared/subscription/src/main/resources/db/changelog/migrations/007-subscription/002-create-outbox-table.yaml`
- [ ] T022 [US3] Create `OutboxEvent` entity in `shared/subscription/src/main/kotlin/com/cvix/subscription/domain/model/OutboxEvent.kt`
- [ ] T023 [US3] Create `OutboxRepository` interface and R2DBC adapter in `shared/subscription/src/main/kotlin/com/cvix/subscription/infrastructure/persistence/OutboxRepository.kt`
- [ ] T024 [US3] Update `SubscriptionService` to persist `OutboxEvent` transactionally with `Subscription`
- [ ] T025 [US3] Implement `OutboxPublisher` (scheduled job) with exponential backoff and DLQ in `shared/subscription/src/main/kotlin/com/cvix/subscription/infrastructure/messaging/OutboxPublisher.kt`
- [ ] T026 [US3] Write integration test for event generation and publishing in `shared/subscription/src/test/kotlin/com/cvix/subscription/infrastructure/messaging/OutboxIntegrationTest.kt`

## Phase 6: Confirmation Workflow (FR-007)

*Goal: Double opt-in support.*

- [ ] T027 [FR-007] Add `issueConfirmationToken` method to `SubscriptionService` using secure CSPRNG and hashed storage (one-time use).
- [ ] T028 [FR-007] Implement `confirm` method in `SubscriptionService` (validate hash, timing-safe compare, check expiry, mark consumed).
- [ ] T029 [FR-007] Add `POST /subscriptions/{id}/confirm` endpoint (body-based token) to `SubscriptionController`
- [ ] T030 [FR-007] Write integration test for confirmation flow in `shared/subscription/src/test/kotlin/com/cvix/subscription/application/SubscriptionConfirmationTest.kt`

## Phase 7: Privacy & Compliance (NFR-003)

*Goal: GDPR/CCPA compliance APIs.*

- [ ] T031 [NFR-003] Implement `delete` method in `SubscriptionService` (Modes: `softDeleteWithAnonymization` and `hardDelete`).
- [ ] T032 [NFR-003] Add `DELETE /subscriptions/{id}` endpoint to `SubscriptionController` (Admin or Owner matching `creatorId`).
- [ ] T033 [NFR-003] Implement `export` method in `SubscriptionService` (JSON/CSV, redact PII by default, support `redact` flag).
- [ ] T034 [NFR-003] Add `GET /subscriptions/export` endpoint to `SubscriptionController` (Streaming CSV/NDJSON, Pagination, Admin only).
- [ ] T035 [NFR-003] Write integration tests for deletion and export in `shared/subscription/src/test/kotlin/com/cvix/subscription/application/PrivacyComplianceTest.kt`

## Phase 8: Polish

- [ ] T036 Add metrics (Micrometer) to `SubscriptionService` and `OutboxPublisher`
- [ ] T037 Update module `README.md` with usage examples
- [ ] T038 Review and verify OpenAPI contract matches implementation

## Dependencies

- Phase 2 (Foundational) blocks ALL other phases.
- Phase 3 (US1) blocks Phase 4, 5, 6, 7.
- Phases 4, 5, 6, 7 can be executed in parallel after Phase 3.

## Implementation Strategy

1.  **MVP**: Complete Phases 1, 2, and 3. This provides the core capture capability.
2.  **Reliability**: Complete Phase 5 (Outbox) to ensure downstream systems can integrate.
3.  **Feature Parity**: Complete Phase 4 and 6.
4.  **Compliance**: Complete Phase 7 before public launch.
