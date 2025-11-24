# CI Test Timeout Fixes - Final Summary

## Problem

Two tests in `ResumeGeneratorControllerIntegrationTest` were failing in CI with `TimeoutException`
caused by:

1. Docker image pulls taking too long in CI (2GB TeX Live image)
2. Cascading timeouts that were too aggressive
3. `AsynchronousCloseException` when Docker connections were interrupted
4. **`Connection pool shut down` errors** when reactive timeout closed the Docker HTTP client pool
   during image pull

## Root Causes Identified

1. **Docker Image Pull Duration**: The `texlive/texlive:TL2024-historic` image (~2GB) takes
   significant time to pull in CI
2. **Cascading Timeouts**: Multiple timeout layers were too aggressive:
    - Test method timeout: initially 60 seconds
    - Application generation timeout: initially 30 seconds
    - Docker container timeout: initially 30 seconds
    - Reactive pipeline timeout: initially 35 seconds (30 + 5 buffer)
3. **Interruption Handling**: Docker image pull was not resilient to interruption
4. **Connection Pool Shutdown**: When reactive timeout fired, it closed the Docker HTTP client's
   connection pool, preventing any retry attempts

## Solutions Implemented

### 1. Increased Test Timeouts

**File**: `ResumeGeneratorControllerIntegrationTest.kt`

- Changed `@Timeout(60)` to `@Timeout(150)` for both failing tests
- Allows sufficient time for Docker image pulls and PDF generation in CI

### 2. Increased Application Generation Timeout

**File**: `PdfResumeGenerator.kt`

- Changed `GENERATION_TIMEOUT_MS` from `30_000L` (30s) to `120_000L` (120s)
- Accommodates slow CI environments and Docker image pulls

### 3. Increased Docker Container Timeout Default

**File**: `DockerPdfGeneratorProperties.kt`

- Changed default `timeoutSeconds` from `30` to `60`
- Provides more headroom for LaTeX compilation in CI

### 4. Test-Specific Configuration Override

**File**: `application-test.yml`

- Added `resume.pdf.docker.timeout-seconds: 120` configuration
- Ensures tests use longer timeouts appropriate for CI environments

### 5. Increased Reactive Pipeline Buffers

**File**: `DockerPdfGenerator.kt`

- `PULL_TIMEOUT_MIN`: 2 → 5 minutes (for Docker image pulls)
- `TIMEOUT_BUFFER_SECONDS`: 5 → 60 seconds (reactive timeout buffer)
- `SEMAPHORE_TIMEOUT_BUFFER`: 10 → 60 seconds (concurrency control)

### 6. Improved Docker Image Pull Resilience

**File**: `DockerPdfGenerator.kt` - `ensureDockerImage()` and `handleAsyncException()` methods

- Added handling for `AsynchronousCloseException` wrapped in `RuntimeException`
- Retry image inspection after interrupted pull (image might have been pulled by another thread)
- **Added handling for `IllegalStateException: Connection pool shut down` errors**
- **Gracefully fail with clear error message when connection pool is shut down**
- Better error messages for debugging connection pool shutdown scenarios

### 7. Enhanced Error Handling

**File**: `DockerPdfGenerator.kt` - `generatePdfInContainer()` method

- Added specific catch block for `AsynchronousCloseException` during PDF generation
- Provides clear error message suggesting Docker resource increases

## Timeout Hierarchy (After Fix)

```text
Test Method Timeout: 150 seconds
  └─> Application Generation: 120 seconds
      └─> Reactive Pipeline: 180 seconds (120 + 60 buffer)
          └─> Docker Container: 120 seconds (test config)
              └─> Docker Image Pull: 5 minutes (first run only)
```

## Test Results

✅ All tests passing locally
✅ All tests passing in CI
✅ No compilation errors
✅ Proper error handling for edge cases including connection pool shutdown

## Performance Impact

- **Local Development**: No impact (image already cached)
- **CI First Run**: Tests take longer (~2-3 minutes for first PDF generation due to image pull)
- **CI Subsequent Runs**: Normal execution time (~5-10 seconds per test with cached image)

## Key Learnings

1. **Reactive Timeout Side Effects**: When a reactive timeout fires, it can close underlying HTTP
   connection pools, making retry logic impossible
2. **Connection Pool Lifecycle**: The Docker Java client's connection pool is closed when the
   reactive stream times out
3. **Proper Error Handling**: Need to catch both `AsynchronousCloseException` and
   `IllegalStateException` from connection pool shutdown
4. **Test Configuration**: Test-specific configuration overrides are essential for CI environments
   with different performance characteristics

## Future Improvements (Optional)

1. **Pre-pull Docker image in CI setup step** to speed up tests
2. Use a **smaller LaTeX Docker image** if full TeX Live is not required
3. Add **retry logic** for transient Docker failures
4. Consider **caching the Docker image** in CI artifacts
5. Implement **health check** to detect if Docker daemon is responsive before starting tests

## Files Modified

1.
`server/engine/src/test/kotlin/com/loomify/resume/infrastructure/http/ResumeGeneratorControllerIntegrationTest.kt`

2. `server/engine/src/main/kotlin/com/loomify/resume/application/generate/PdfResumeGenerator.kt`
3.
`server/engine/src/main/kotlin/com/loomify/resume/infrastructure/pdf/DockerPdfGeneratorProperties.kt`

4. `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/pdf/DockerPdfGenerator.kt`
5. `server/engine/src/test/resources/application-test.yml` (NEW)

## Error Messages Handled

- `java.util.concurrent.TimeoutException`
- `java.nio.channels.AsynchronousCloseException`
- `java.lang.IllegalStateException: Connection pool shut down`
- `com.github.dockerjava.api.exception.NotFoundException`

## Related Configuration

- GitHub Actions workflow: `.github/workflows/*.yml` (no changes needed)
- Docker resource allocation in CI (no changes needed currently)
- Spring Boot test configuration: `application-test.yml` (updated)
