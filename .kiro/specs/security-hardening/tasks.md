# Implementation Plan

- [ ] 1. Set up security infrastructure and core interfaces
  - Create security package structure in `server/engine/src/main/kotlin/com/loomify/engine/security/`
  - Define core security interfaces and data classes for all security components
  - Set up security configuration classes and enable reactive security filters
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 7.1, 8.1, 9.1_

- [ ] 2. Implement rate limiting system
  - [ ] 2.1 Create rate limiting core components
    - Implement `ReactiveRateLimitService` interface with Redis backend
    - Create `RateLimitConfig` data class and repository operations
    - Write `RateLimitFilter` for WebFlux filter chain integration
    - Define a trusted proxy chain configuration for IP extraction (parse `X-Forwarded-For` and fallback to remote address)
    - Use Redis `EVAL`/`EVALSHA` Lua scripts for atomic token-bucket/sliding-window operations
    - Ensure all Redis calls are non-blocking/reactive and include script loading/sha caching
    - _Requirements: 1.1, 1.2, 1.3_

  - [ ] 2.2 Add rate limiting configuration and metrics
    - Implement hierarchical rate limiting (global → tenant → user → endpoint)
    - Create configuration management for rate limits per tenant
    - Add Micrometer metrics for rate limiting monitoring
    - _Requirements: 1.4, 1.5_

  - [ ]* 2.3 Write rate limiting tests
    - Create unit tests for rate limiting service and filter
    - Write integration tests with Redis and reactive streams
    - Add load testing scenarios for rate limiting effectiveness
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 3. Implement DDoS protection system
  - [ ] 3.1 Create traffic analysis and IP blocking
    - Implement `DDoSProtectionService` with reactive traffic analysis
    - Create `IPBlocklistService` for managing blocked IP addresses
    - Write `DDoSProtectionFilter` for request filtering and blocking
    - _Requirements: 2.1, 2.2, 2.3_

  - [ ] 3.2 Add threat detection and monitoring
    - Implement traffic pattern analysis with configurable thresholds
    - Create security event logging for DDoS protection actions
    - Add automatic IP blocking with exponential backoff
    - _Requirements: 2.4, 2.5_

  - [ ]* 3.3 Write DDoS protection tests
    - Create unit tests for traffic analysis and IP blocking
    - Write integration tests for threat detection algorithms
    - Add performance tests for high-traffic scenarios
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [ ] 4. Implement dynamic CORS management
  - [ ] 4.1 Create tenant-aware CORS configuration
    - Implement `TenantCorsConfigService` with database-backed configuration
    - Create `TenantAwareCorsConfigurationSource` for dynamic CORS
    - Write CORS configuration repository and data models
    - _Requirements: 3.1, 3.2, 3.3_

  - [ ] 4.2 Add CORS validation and security headers
    - Implement CORS validation filter with tenant-specific rules
    - Validate incoming `Origin` against a tenant-specific allowlist from `TenantCorsConfigService`
    - Deny requests with invalid `Origin`
    - Always set the `Vary: Origin` header
    - Never return `Access-Control-Allow-Origin: *` when `Access-Control-Allow-Credentials` is true
    - Explicitly return only allowed methods and headers per tenant (no wildcards)
    - Log and monitor CORS violations with tenant context
    - _Requirements: 3.4, 3.5_

  - [ ]* 4.3 Write CORS management tests
    - Create unit tests for tenant-specific CORS configuration
    - Write integration tests for dynamic CORS updates
    - Add security tests for CORS bypass attempts
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 5. Implement Content Security Policy (CSP) system
  - [ ] 5.1 Create CSP header generation and management
    - Implement `CSPHeaderService` with context-aware header generation
    - Create `CSPHeaderFilter` for automatic header injection
    - Write CSP configuration management with tenant support
    - _Requirements: 4.1, 4.2, 4.4_

  - [ ] 5.2 Add CSP violation reporting and nonce support
    - Implement CSP violation reporting endpoint and logging
    - Create nonce-based script execution for dynamic content
    - Add different CSP policies for admin vs user interfaces
    - _Requirements: 4.3, 4.5_

  - [ ]* 5.3 Write CSP implementation tests
    - Create unit tests for CSP header generation and nonce creation
    - Write integration tests for CSP violation reporting
    - Add security tests for CSP bypass attempts
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 6. Implement input sanitization system
  - [ ] 6.1 Create reactive input sanitization service
    - Implement `InputSanitizationService` with context-aware sanitization
    - Create `InputSanitizationFilter` for request/response sanitization
    - Write HTML sanitization using OWASP Java HTML Sanitizer
    - _Requirements: 5.1, 5.2, 5.4_

  - [ ] 6.2 Add input validation and output encoding
    - Implement input validation with configurable rules per tenant
    - Create output encoding for user-generated content display
    - Add validation result handling and error responses
    - _Requirements: 5.3, 5.5_

  - [ ]* 6.3 Write input sanitization tests
    - Create unit tests for input sanitization and validation
    - Write security tests with XSS attack vectors
    - Add fuzzing tests for input sanitization robustness
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ] 7. Implement SQL injection prevention
  - [ ] 7.1 Create secure repository base classes
    - Implement `SecureR2dbcRepository` with parameterized query enforcement
    - Create `QueryValidationAspect` for AOP-based query validation
    - Write query pattern monitoring and suspicious activity detection
    - _Requirements: 6.1, 6.2, 6.3_

  - [ ] 7.2 Add database error handling and monitoring
    - Implement secure database error handling without information disclosure
    - Create SQL injection attempt logging and monitoring
    - Add database query pattern analysis for security monitoring
    - _Requirements: 6.4, 6.5_

  - [ ]* 7.3 Write SQL injection prevention tests
    - Create unit tests for parameterized query enforcement
    - Write security tests with SQL injection attack vectors
    - Add integration tests for secure repository operations
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 8. Implement secret management integration
  - [ ] 8.1 Create multi-provider secret management service
    - Implement `SecretManagementService` with AWS Secrets Manager support
    - Create `VaultSecretService` for HashiCorp Vault integration
    - Write `SecretCacheService` for secure in-memory caching
    - _Requirements: 7.1, 7.2, 7.3_

  - [ ] 8.2 Add secret rotation and fallback mechanisms
    - Implement automatic secret rotation handling and scheduling
    - Create fallback mechanisms for high availability
    - Add secret expiration monitoring and alerts
    - _Requirements: 7.4, 7.5_

  - [ ]* 8.3 Write secret management tests
    - Create unit tests for secret retrieval and caching
    - Write integration tests with AWS Secrets Manager and Vault
    - Add tests for secret rotation and fallback scenarios
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ] 9. Implement encryption at rest system
  - [ ] 9.1 Create field encryption service and annotations
    - Implement `FieldEncryptionService` with AES-256-GCM encryption
      - Use a cryptographically secure per-record random nonce (12 bytes recommended)
      - Persist nonce and authentication tag alongside ciphertext
      - Verify integrity during decryption
    - Implement envelope encryption:
      - Generate a unique data-encryption-key (DEK) per record or logical grouping
      - Wrap/unwrap DEKs with KMS/Vault master keys
      - Store only the wrapped DEK and a key-version identifier
    - Use secure RNG for nonces and DEKs
    - Include key-version metadata and define a rotation strategy
    - Ensure all comparisons and error paths avoid leaking secrets
    - Create `@Encrypted` annotation for marking sensitive fields
    - Write `EncryptedFieldConverter` for R2DBC transparent encryption
    - _Requirements: 8.1, 8.2, 8.4_

  - [ ] 9.2 Add key management and rotation
    - Implement `KeyManagementService` for encryption key lifecycle
    - Create key rotation functionality without data loss
    - Add encryption performance optimization and monitoring
    - _Requirements: 8.3, 8.5_

  - [ ]* 9.3 Write encryption at rest tests
    - Create unit tests for field encryption and decryption
    - Write integration tests with R2DBC and database operations
    - Add performance tests for encryption overhead measurement
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 10. Implement PII masking and compliance system
  - [ ] 10.1 Create PII detection and masking service
    - Implement `PIIMaskingService` with pattern-based PII detection
    - Create `MaskingStrategyFactory` for different masking approaches
    - Write `PIIMaskingFilter` for response masking
    - _Requirements: 9.1, 9.2, 9.4_

  - [ ] 10.2 Add compliance features and audit trail
    - Implement GDPR compliance features (right to be forgotten, data portability)
    - Create `ComplianceAuditService` for PII operation tracking
    - Add compliance reporting and audit log generation
    - _Requirements: 9.3, 9.5_

  - [ ]* 10.3 Write PII masking and compliance tests
    - Create unit tests for PII detection and masking strategies
    - Write integration tests for compliance features
    - Add tests for audit trail and compliance reporting
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ] 11. Integrate security monitoring and error handling
  - [ ] 11.1 Create security event logging and monitoring
    - Implement `SecurityEvent` data model and repository
    - Create global security error handler for all security exceptions
    - Write security metrics collection with Micrometer integration
    - _Requirements: 1.5, 2.5, 3.4, 4.3, 5.3, 6.5, 7.5, 8.5, 9.5_

  - [ ] 11.2 Add security dashboards and alerting
    - Create Grafana dashboards for security monitoring
    - Implement alerting for security events and thresholds
    - Add structured logging for compliance and forensics
    - _Requirements: 1.5, 2.5, 3.4, 4.3, 5.3, 6.5, 7.5, 8.5, 9.5_

- [ ] 12. Configure and wire security components
  - [ ] 12.1 Create security configuration and filter chain
    - Configure Spring WebFlux security filter chain with proper ordering
    - Create application configuration for all security components
    - Wire all security services and filters together
    - _Requirements: All requirements integration_

  - [ ] 12.2 Add security configuration management
    - Implement tenant-specific security configuration management
    - Create security configuration validation and defaults
    - Add configuration hot-reloading without application restart
    - _Requirements: All requirements integration_

  - [ ]* 12.3 Write end-to-end security tests
    - Create integration tests for complete security filter chain
    - Write security penetration tests with OWASP ZAP integration
    - Add performance tests for security overhead measurement
    - _Requirements: All requirements integration_
