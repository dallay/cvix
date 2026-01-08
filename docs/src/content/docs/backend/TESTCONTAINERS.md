---
title: Testcontainers Management Guide
---

This guide explains how Testcontainers are configured in this project and how to manage them effectively.

## Overview

The backend integration tests use [Testcontainers](https://testcontainers.com/) to provide real infrastructure services:

- **PostgreSQL**: Database for integration tests
- **Keycloak**: Authentication and authorization
- **GreenMail**: Email testing server

## Container Reuse Strategy

### In Production Code (CI/CD)

The containers are configured **without** `withReuse(true)` in the code (`InfrastructureTestContainers.kt`). This ensures:

- ✅ Clean state for each CI build
- ✅ No state leakage between test runs
- ✅ Reliable and reproducible builds
- ✅ Proper cleanup after test execution

### In Local Development

For faster local development, you can enable container reuse globally by creating/editing `~/.testcontainers.properties`:

```properties
testcontainers.reuse.enable=true
```

**Benefits:**

- ⚡ Significantly faster test execution (containers don't restart)
- 💻 Better developer experience during iterative development
- 🔧 Less resource usage on your local machine

**Trade-offs:**

- 🗑️ Containers persist after tests complete
- ⚠️ Manual cleanup needed when done testing
- 🔄 Potential state issues if not cleaned regularly

## Managing Test Containers

### Check Running Containers

```bash
# List all test containers
docker ps --filter "name=keycloak-tests" --filter "name=greenmail-tests"

# List all test containers including stopped ones
docker ps -a --filter "name=keycloak-tests" --filter "name=greenmail-tests"
```

### Clean Up Containers

```bash
# Use the provided script
make cleanup-test-containers

# Or manually remove specific containers
docker rm -f keycloak-tests greenmail-tests

# Or remove all stopped containers
docker container prune
```

### Disable Reuse Temporarily

If you want to disable reuse for a single test run:

```bash
TESTCONTAINERS_REUSE_ENABLE=false ./gradlew test
```

## Configuration Files

### Global Configuration

- **File**: `~/.testcontainers.properties`
- **Scope**: Affects all projects on your machine
- **Purpose**: Local development optimization

Example:

```properties
testcontainers.reuse.enable=true
docker.client.strategy=org.testcontainers.dockerclient.UnixSocketClientProviderStrategy
```

### Code Configuration

- **File**: `server/engine/src/test/kotlin/com/cvix/engine/config/InfrastructureTestContainers.kt`
- **Scope**: Project-specific behavior
- **Purpose**: Ensure clean CI/CD builds

The code does **NOT** include `withReuse(true)` to maintain clean CI builds.

## Troubleshooting

### Containers Not Starting

If containers fail to start:

1. Ensure Docker daemon is running
2. Check available disk space
3. Verify port availability (no conflicts)
4. Clean up old containers: `make cleanup-test-containers`

### Tests Running Slowly

If tests are slow even with reuse enabled:

1. Verify `~/.testcontainers.properties` exists and has `testcontainers.reuse.enable=true`
2. Check that containers are actually reused: `docker ps` should show running containers after tests
3. Ensure Docker has sufficient resources allocated (CPU, memory)

### Stale Data in Containers

If you suspect containers have stale data:

1. Stop and remove containers: `make cleanup-test-containers`
2. Run tests again to get fresh containers
3. Alternatively, disable reuse for that run: `TESTCONTAINERS_REUSE_ENABLE=false ./gradlew test`

## Best Practices

1. **During Development**: Enable reuse in `~/.testcontainers.properties` for faster iterations
2. **Before Committing**: Run `make cleanup-test-containers` to ensure clean state
3. **In CI/CD**: Reuse is automatically disabled (not in code)
4. **Daily Cleanup**: Clean up containers at end of day: `make cleanup-test-containers`
5. **After Switching Branches**: Clean containers to avoid conflicts from different test data

## Container Details

### Keycloak Container

- **Name**: `keycloak-tests`
- **Image**: `keycloak/keycloak:25.0`
- **Purpose**: OAuth2/OIDC authentication provider
- **Realm Config**: `src/test/resources/keycloak/demo-realm-test.json`

### GreenMail Container

- **Name**: `greenmail-tests`
- **Image**: `greenmail/standalone:2.1.8`
- **Purpose**: Email testing server (SMTP, IMAP, POP3)
- **Ports**: 3025 (SMTP), 3110 (POP3), 3143 (IMAP), 3465 (SMTPS), 3993 (IMAPS), 3995 (POP3S), 6080 (Web UI)

## References

- [Testcontainers Documentation](https://testcontainers.com/)
- [Testcontainers Reuse Feature](https://java.testcontainers.org/features/reuse/)
- [Project Testing Guidelines](.ruler/03_TESTING/03_BACKEND_TESTING_CONVENTIONS.md)
