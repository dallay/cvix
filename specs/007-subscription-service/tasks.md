# Tasks: Subscription Service

**Feature**: Subscription Service
**Status**: Pending
**Spec**: [specs/007-subscription-service/spec.md](spec.md)

## Phase 1: Setup

- [ ] T001 Verify `shared/engagement` module build configuration in `shared/engagement/build.gradle.kts`
- [ ] T002 Create package structure `com.cvix.subscription` in `shared/engagement/src/main/kotlin`
- [ ] T003 Create `SubscriptionApplication` or configuration class if needed in `shared/engagement/src/main/kotlin/com/cvix/subscription/infrastructure/config/SubscriptionConfig.kt`
- [ ] T003a Configure Authentication/Authorization (OAuth2/JWT) and security filters in `shared/engagement/src/main/kotlin/com/cvix/subscription/infrastructure/config/SecurityConfig.kt`

## Phase 2: Foundational (Blocking)

*Goal: Establish database schema and core domain entities.*

- [ ] T004 Create Liquibase changelog for `subscriptions` table in `shared/engagement/src/main/resources/db/changelog/migrations/007-subscription/001-create-subscriptions-table.yaml`
- [ ] T005 Create `Subscription` domain entity in `shared/engagement/src/main/kotlin/com/cvix/subscription/domain/model/Subscription.kt`
- [ ] T006 Create `SubscriptionRepository` port interface in `shared/engagement/src/main/kotlin/com/cvix/subscription/domain/SubscriptionRepository.kt`
- [ ] T007 Implement R2DBC repository adapter `SubscriptionR2dbcRepository` in `shared/engagement/src/main/kotlin/com/cvix/subscription/infrastructure/persistence/SubscriptionR2dbcRepository.kt`
- [ ] T008 Create integration test base class `SubscriptionIntegrationTest` in `shared/engagement/src/test/kotlin/com/cvix/subscription/SubscriptionIntegrationTest.kt`

## Phase 3: Submit Email Capture (User Story 1 - P1)

*Goal: Core functionality to capture emails with deduplication.*

- [ ] T009 [US1] Define `CreateSubscriptionCommand` in `shared/engagement/src/main/kotlin/com/cvix/subscription/application/create/CreateSubscriptionCommand.kt` and create the `CreateSubscriptionCommandHandler` to process it.
- [ ] T010 [US1] Implement `SubscriptionService` with `create` method in `shared/engagement/src/main/kotlin/com/cvix/subscription/application/create/SubscriptionService.kt`
- [ ] T011 [US1] Add email normalization and validation logic to `SubscriptionService`
- [ ] T012 [US1] Implement deduplication (idempotency) logic in `SubscriptionService`
- [ ] T013 [US1] Create `SubscriptionController` in `shared/engagement/src/main/kotlin/com/cvix/subscription/infrastructure/web/SubscriptionController.kt`
- [ ] T014 [US1] Implement `POST /subscriptions` endpoint in `SubscriptionController`
- [ ] T015 [US1] Write integration test for successful capture in `shared/engagement/src/test/kotlin/com/cvix/subscription/application/CreateSubscriptionTest.kt`
- [ ] T016 [US1] Write integration test for duplicate handling in `shared/engagement/src/test/kotlin/com/cvix/subscription/application/CreateSubscriptionIdempotencyTest.kt`

## Phase 4: Metadata Registrations (User Story 2 - P2)

*Goal: Support arbitrary metadata with validation.*

- [ ] T017 [US2] Add metadata validation constants/config to `SubscriptionService`
- [ ] T018 [US2] Update `SubscriptionService.create` to validate metadata size and constraints
- [ ] T019 [US2] Add `findAllByMetadata` method to `SubscriptionRepository` and Adapter
- [ ] T020 [US2] Write integration test for metadata persistence and validation in `shared/engagement/src/test/kotlin/com/cvix/subscription/application/SubscriptionMetadataTest.kt`

## Phase 5: Notifications (User Story 3 - P3)

*Goal: Transactional Outbox for reliable downstream events.*

- [ ] T021 [US3] Create Liquibase changelog for `subscription_outbox_events` in `shared/engagement/src/main/resources/db/changelog/migrations/007-subscription/002-create-outbox-table.yaml`
- [ ] T022 [US3] Create `OutboxEvent` entity in `shared/engagement/src/main/kotlin/com/cvix/subscription/domain/OutboxEvent.kt`
- [ ] T023 [US3] Create `OutboxRepository` interface and R2DBC adapter in `shared/engagement/src/main/kotlin/com/cvix/subscription/infrastructure/persistence/OutboxRepository.kt`
- [ ] T024 [US3] Update `SubscriptionService` to persist `OutboxEvent` transactionally with `Subscription`
- [ ] T025 [US3] Implement `OutboxPublisher` (scheduled job) in `shared/engagement/src/main/kotlin/com/cvix/subscription/infrastructure/messaging/OutboxPublisher.kt`
- [ ] T026 [US3] Write integration test for event generation and publishing in `shared/engagement/src/test/kotlin/com/cvix/subscription/infrastructure/messaging/OutboxIntegrationTest.kt`

## Phase 6: Confirmation Workflow (FR-007)

*Goal: Double opt-in support.*

- [ ] T027 [FR-007] Add `issueConfirmationToken` method to `SubscriptionService` using secure CSPRNG and hashing
- [ ] T028 [FR-007] Implement `confirm` method in `SubscriptionService` (validate token hash, expiry, update status)
- [ ] T029 [FR-007] Add `POST /subscriptions/{id}/confirm` endpoint to `SubscriptionController`
- [ ] T030 [FR-007] Write integration test for confirmation flow in `shared/engagement/src/test/kotlin/com/cvix/subscription/application/SubscriptionConfirmationTest.kt`

## Phase 7: Privacy & Compliance (NFR-003)

*Goal: GDPR/CCPA compliance APIs.*

- [ ] T031 [NFR-003] Implement `delete` method in `SubscriptionService` (Strategy: Soft delete with anonymization)
- [ ] T032 [NFR-003] Add `DELETE /subscriptions/{id}` endpoint to `SubscriptionController` (Admin/Owner only)
- [ ] T033 [NFR-003] Implement `export` method in `SubscriptionService` (return JSON/CSV data, redact PII unless auth'd)
- [ ] T034 [NFR-003] Add `GET /subscriptions/export` endpoint to `SubscriptionController` (Pagination, AuthZ required)
- [ ] T035 [NFR-003] Write integration tests for deletion and export in `shared/engagement/src/test/kotlin/com/cvix/subscription/application/PrivacyComplianceTest.kt`

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
