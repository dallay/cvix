# Implementation Plan

- [ ] 1. Set up core storage interfaces and domain models
  - Create the StorageProvider interface with suspend functions for reactive operations
  - Define data classes for UploadRequest, UploadResult, SignedUrlOptions, and FileMetadata
  - Implement enums for StorageType, Visibility, and ProcessingStatus
  - Create exception hierarchy for storage-specific errors
  - _Requirements: 1.1, 2.1, 2.4_

- [ ] 2. Implement database entities and repositories
  - [ ] 2.1 Create FileMetadata entity with R2DBC annotations
    - Define table structure with tenant isolation, file variants, and audit fields
    - Add indexes for tenant_id, storage_key, and uploaded_at for query performance
    - Add a unique constraint on (tenant_id, storage_key)
    - Add a `version` field annotated with `@Version` for optimistic locking
    - Add a `deleted` flag for soft-delete support
    - Update repository queries to filter out soft-deleted rows
    - _Requirements: 1.2, 7.1_

  - [ ] 2.2 Create TenantStorageConfig entity
    - Define tenant-specific storage preferences, quotas, and processing options
    - Include provider selection, file type restrictions, and CDN configuration
    - _Requirements: 2.2, 4.1, 4.2_

  - [ ] 2.3 Create FileAuditLog entity for compliance tracking
    - Track all file operations with user, tenant, timestamp, and metadata
    - Support filtering and querying for audit reports
    - _Requirements: 7.1, 7.3_

  - [ ] 2.4 Implement reactive repositories using R2DBC
    - Create FileMetadataRepository with tenant-scoped queries
    - Implement TenantStorageConfigRepository with caching support
    - Build FileAuditLogRepository with efficient audit trail queries
    - _Requirements: 1.2, 4.4, 7.1_

  - [ ]* 2.5 Write unit tests for repository operations
    - Test tenant isolation in queries and data access
    - Verify audit log creation and retrieval functionality
    - _Requirements: 1.2, 7.1_

- [ ] 3. Create local filesystem storage provider implementation
  - [ ] 3.1 Implement LocalFileSystemProvider class
    - Create directory structure based on tenant ID for isolation
    - Implement upload, download, delete, and exists operations
      - Sanitize incoming filenames (strip null-bytes, disallow '..' segments)
      - Construct a safe tenant root and resolve paths with `realpath`, verifying they remain inside the tenant directory
      - Reject paths resolving outside the tenant directory
      - Detect and reject symlinks using `lstat`/`isSymbolicLink` before accessing files
      - Implement atomic writes by writing to a temp file, fsyncing the file and directory, then renaming to the final name
      - Create directories with restrictive permissions (0o700) and set file permissions to 0o600
      - Avoid following symlinks when creating or removing files
      - Apply the same `realpath`/verify/symlink checks to download, delete, and exists operations
    - Handle file metadata extraction and storage key generation
    - _Requirements: 1.1, 1.2, 2.1_

  - [ ] 3.2 Add signed URL generation for local files
    - Generate time-limited access tokens for local file access
    - Implement URL validation and expiration checking
    - Create secure file serving endpoint with token verification
    - _Requirements: 3.1, 3.3_

  - [ ] 3.3 Implement health check and error handling
    - Check filesystem availability and permissions
    - Handle disk space monitoring and error reporting
    - _Requirements: 2.3, 8.3_

  - [ ]* 3.4 Write unit tests for local provider
    - Test file operations with mock filesystem
    - Verify tenant isolation and security measures
    - _Requirements: 1.2, 3.2_

- [ ] 4. Build storage router and provider management
  - [ ] 4.1 Create StorageRouter component
    - Implement provider selection based on tenant configuration
    - Add fallback logic for provider unavailability
    - Support routing based on file type and size
    - _Requirements: 2.2, 2.3, 2.4_

  - [ ] 4.2 Implement provider registry and health monitoring
    - Register available storage providers at startup
    - Monitor provider health and availability status
    - Handle graceful degradation when providers fail
    - Implement DB transactions (or explicit row locks like SELECT FOR UPDATE) around quota reads and increments
    - Use atomic increment operations where supported
    - Update tenant usage inside the same transaction as the upload metadata to avoid races
    - Include all file variants (original, thumbnails, derived) in the same usage calculation and persist their sizes atomically
    - Prevent double-counting on retries by recording a unique upload_id (or idempotency key) for each upload
    - Ensure rollback paths subtract only committed sizes to maintain consistency
    - _Requirements: 2.3, 8.2, 8.3_

  - [ ]* 4.3 Write integration tests for routing logic
    - Test provider selection with different tenant configurations
    - Verify failover behavior when providers are unavailable
    - _Requirements: 2.3, 8.2_

- [ ] 5. Implement quota management system
  - [ ] 5.1 Create QuotaManager component
    - Implement quota checking before file uploads
    - Track storage usage per tenant with atomic updates
    - Generate quota warnings at 80% and 95% usage levels
    - _Requirements: 4.1, 4.2, 4.3_

  - [ ] 5.2 Build usage tracking and reporting
    - Update storage usage on file upload, delete, and processing
    - Create usage calculation service with caching
    - Implement quota enforcement with proper error handling
    - _Requirements: 4.2, 4.4, 4.5_

  - [ ] 5.3 Add quota notification system
    - Send alerts when quota thresholds are reached
    - Create quota usage reports for tenant administrators
    - Require virus scanning as the first step in the pipeline
    - Mandate idempotent processing by persisting per-file status markers (e.g., scanned, thumb-generated, compressed)
    - Use an outbox pattern with intent events and retry with exponential backoff
    - Implement reactive/asynchronous streams for orchestration and failure handling
    - Ensure retries reprocess only incomplete steps
    - _Requirements: 4.3, 8.4_

  - [ ]* 5.4 Write unit tests for quota calculations
    - Test quota enforcement with various usage scenarios
    - Verify notification triggers and usage tracking accuracy
    - _Requirements: 4.2, 4.3_

- [ ] 6. Create file processing pipeline
  - [ ] 6.1 Implement FileProcessor interface and orchestrator
    - Define processing pipeline: virus scanning -> thumbnails -> compression (scan first)
    - Create asynchronous processing with reactive streams
    - Handle processing failures with idempotency keys and transactional outbox retries
    - _Requirements: 6.1, 6.2, 6.4_
  - [ ] 6.2 Build thumbnail generation processor
    - Generate multiple thumbnail sizes for image files
    - Support common image formats (JPEG, PNG, WebP)
    - Store thumbnail variants with original file metadata
    - _Requirements: 6.1, 6.5_

  - [ ] 6.3 Implement file compression processor
    - Add configurable compression for documents and images
    - Support different compression algorithms based on file type
    - Update file metadata with compressed variants
    - _Requirements: 6.2, 6.5_

  - [ ] 6.4 Create virus scanning integration
    - Integrate with ClamAV or similar virus scanning service
    - Quarantine files that fail virus scanning
    - Log security events for infected file attempts
    - _Requirements: 1.4, 6.4_

  - [ ]* 6.5 Write unit tests for file processors
    - Test thumbnail generation with sample images
    - Verify compression functionality and metadata updates
    - _Requirements: 6.1, 6.2_

- [ ] 7. Build security and access control layer
  - [ ] 7.1 Create FileSecurityService component
    - Implement tenant-based access control for all file operations
    - Verify user permissions for upload, download, and delete operations
    - Ensure complete tenant isolation in file access
    - _Requirements: 1.2, 3.2, 7.2_

  - [ ] 7.2 Implement audit logging system
    - Log all file operations with user, tenant, and timestamp information
    - Create audit trail for compliance and security monitoring
    - Support audit log querying and reporting
    - _Requirements: 7.1, 7.3, 7.4_

  - [ ] 7.3 Add signed URL security
    - Generate time-limited signed URLs with proper expiration
    - Validate signed URLs and prevent unauthorized access
    - Support different access levels (private, tenant-shared, public)
    - _Requirements: 3.1, 3.2, 3.3_

  - [ ]* 7.4 Write security tests
    - Test tenant isolation and access control enforcement
    - Verify audit logging for all file operations
    - _Requirements: 1.2, 7.1_

- [ ] 8. Create reactive file upload/download API
  - [ ] 8.1 Build file upload REST endpoints
    - Create reactive endpoints for file upload initiation
    - Generate pre-signed URLs for direct client uploads
    - Handle multipart upload for large files
    - _Requirements: 1.1, 1.3, 5.1_

  - [ ] 8.2 Implement file download and access endpoints
    - Create secure file download with access control
    - Support range requests for large file streaming
    - Generate signed URLs for client-side access
    - _Requirements: 3.1, 3.2, 5.3_

  - [ ] 8.3 Add file management endpoints
    - Implement file listing with tenant-scoped queries
    - Create file deletion with quota updates
    - Add file metadata update capabilities
    - _Requirements: 1.2, 4.4, 7.1_

  - [ ] 8.4 Handle multipart and resumable uploads
    - Support chunked uploads for large files
    - Implement upload resumption after network failures
    - Provide upload progress tracking
    - _Requirements: 5.1, 5.2, 5.4_

  - [ ]* 8.5 Write API integration tests
    - Test complete upload/download flows
    - Verify multipart upload functionality
    - _Requirements: 5.1, 5.2_

- [ ] 9. Add monitoring and observability
  - [ ] 9.1 Implement metrics collection
    - Track upload/download success rates and performance
    - Monitor storage provider health and response times
    - Collect quota usage and cost metrics
    - _Requirements: 8.1, 8.3, 8.4_

  - [ ] 9.2 Create health check endpoints
    - Monitor storage provider availability
    - Check database connectivity and performance
    - Verify file processing pipeline health
    - _Requirements: 8.2, 8.3_

  - [ ] 9.3 Add alerting and notification system
    - Generate alerts for quota violations and security events
    - Monitor system performance and provider failures
    - Create operational dashboards for system health
    - Execute all AWS S3/Azure SDK calls on a boundedElastic scheduler (or equivalent) to avoid blocking event loops
    - Configure per-operation timeouts and retry policies with sensible defaults
    - Implement presigned URL generation and explicit handling/retries for S3-specific transient errors
    - Default server-side encryption to SSE-KMS (with the ability to opt into SSE-S3)
    - Document how to pass KMS key IDs and related headers/params
    - _Requirements: 7.2, 8.3, 8.4_

  - [ ]* 9.4 Write monitoring tests
    - Test metrics collection and health check functionality
    - Verify alerting triggers and notification delivery
    - _Requirements: 8.1, 8.3_

- [ ] 10. Implement cloud storage providers
  - [ ] 10.1 Create S3StorageProvider implementation
    - Integrate with AWS S3 SDK for reactive operations
    - Implement pre-signed URL generation for S3
    - Handle S3-specific error conditions and retries
    - _Requirements: 2.1, 1.3, 2.3_

  - [ ] 10.2 Build AzureBlobStorageProvider
    - Integrate with Azure Blob Storage SDK
    - Implement Azure-specific signed URL generation
    - Handle Azure authentication and error handling
    - _Requirements: 2.1, 1.3, 2.3_

  - [ ] 10.3 Create MinIOStorageProvider for development
    - Implement MinIO provider for local development and testing
    - Support S3-compatible API operations
    - Configure for development environment setup
    - _Requirements: 2.1, 2.4_

  - [ ]* 10.4 Write cloud provider integration tests
    - Test S3 and Azure providers with test containers
    - Verify provider-specific functionality and error handling
    - _Requirements: 2.1, 2.3_

- [ ] 11. Add CDN integration and performance optimization
  - [ ] 11.1 Implement CDN integration layer
    - Support CloudFront and Cloudflare CDN integration
    - Generate CDN URLs for public file access
    - Handle CDN cache invalidation for updated files
    - _Requirements: 3.4, 6.3_

  - [ ] 11.2 Add caching layer with Redis
    - Cache file metadata and signed URLs
    - Implement quota information caching
    - Add cache invalidation strategies
    - _Requirements: 3.1, 4.2_

  - [ ] 11.3 Optimize reactive streams performance
    - Implement backpressure handling for large file operations
    - Add parallel processing for file variants
    - Optimize database queries with proper indexing
    - _Requirements: 5.2, 6.1_

  - [ ]* 11.4 Write performance tests
    - Test concurrent upload/download operations
    - Verify caching effectiveness and CDN integration
    - _Requirements: 5.2, 8.1_

- [ ] 12. Create configuration and deployment setup
  - [ ] 12.1 Add Spring Boot configuration properties
    - Define configuration classes for storage providers
    - Create tenant-specific configuration management
    - Add validation for storage provider settings
    - _Requirements: 2.2, 2.5_

  - [ ] 12.2 Create database migration scripts
    - Write Liquibase changesets for all storage tables
    - Add indexes for performance optimization
    - Create initial tenant configuration data
    - _Requirements: 1.2, 4.1_

  - [ ] 12.3 Add Docker and infrastructure configuration
    - Configure storage providers in Docker Compose
    - Add MinIO setup for local development
    - Create environment-specific configuration files
    - _Requirements: 2.1, 2.4_

  - [ ]* 12.4 Write deployment tests
    - Test configuration validation and provider initialization
    - Verify database migrations and initial setup
    - _Requirements: 2.2, 2.5_
