# Test Resources Configuration

This directory contains configuration files for integration tests.

## Testcontainers Configuration

### Overview

The integration tests use **Testcontainers** to provide real instances of external dependencies (PostgreSQL, Keycloak, GreenMail) in Docker containers. This ensures tests run against the same infrastructure as production.

### Architecture

#### Singleton Container Pattern

All containers are managed by `TestcontainersManager` (a Kotlin `object`), ensuring:
- **One container per test suite**: Containers start once and are shared across all test classes
- **Lazy initialization**: Containers only start when first accessed
- **Proper wait strategies**: Tests don't run until containers are fully ready
- **Container reuse**: Containers can be reused between test runs for faster development

#### Container Lifecycle

```
Test Suite Start
    ↓
TestcontainersManager.startAll()
    ↓
├─→ PostgreSQL Container (lazy init)
│   └─→ Wait for port 5432
├─→ Keycloak Container (lazy init)
│   └─→ Wait for /health/ready endpoint
└─→ GreenMail Container (lazy init)
    └─→ Wait for log message
    ↓
All Tests Execute (containers shared)
    ↓
Test Suite End
    ↓
Containers Stop (unless reuse is enabled)
```

### Configuration Files

#### `.testcontainers.properties`

Enables container reuse for faster local development:

```properties
testcontainers.reuse.enable=true
testcontainers.ryuk.disabled=false
```

**Container reuse benefits:**
- ✅ Faster test execution (60-90% faster after first run)
- ✅ Reduced Docker resource usage
- ✅ Preserved container state between runs

**To clean up reused containers:**
```bash
docker ps -a --filter "label=testcontainers.reuse.enable=true" -q | xargs docker rm -f
```

### Test Base Classes

#### `InfrastructureTestContainers`

Abstract base class for tests requiring external infrastructure. Provides:
- Automatic container startup via `@DynamicPropertySource`
- Helper methods for Keycloak authentication
- Spring properties injection from containers

**Usage:**
```kotlin
@AutoConfigureWebTestClient
class MyIntegrationTest : InfrastructureTestContainers() {
    @Autowired
    private lateinit var webTestClient: WebTestClient
    
    @Test
    fun `should test something`() {
        // Containers are already started and ready
        // Spring properties are automatically configured
    }
}
```

### Container Details

#### PostgreSQL

- **Image**: `postgres:16.9-alpine`
- **Network**: Shared test network
- **Optimizations**: `fsync=off`, `synchronous_commit=off` for test performance
- **Reuse**: Enabled
- **Connection**: Automatic via `@ServiceConnection`

#### Keycloak

- **Image**: `keycloak/keycloak:25.0`
- **Realm**: `loomify` (imported from `keycloak/demo-realm-test.json`)
- **Admin**: `admin` / `secret`
- **Wait Strategy**: HTTP health check on `/health/ready`
- **Startup Timeout**: 5 minutes
- **Reuse**: Enabled

**Test users** (defined in realm import):
- Email: `john.doe@loomify.com`
- Username: `john.doe`
- Password: `S3cr3tP@ssw0rd*123`

#### GreenMail (Email)

- **Image**: `greenmail/standalone:2.0.0`
- **Protocols**: SMTP, IMAP, POP3
- **Ports**: 3025 (SMTP), 3110 (POP3), 3143 (IMAP), 3465 (SMTPS), 3993 (IMAPS), 3995 (POP3S), 6080 (Web)
- **Wait Strategy**: Log message "Starting GreenMail standalone"
- **Reuse**: Enabled

### CI Configuration

For GitHub Actions (or other CI), add testcontainers.properties to the runner:

```yaml
- name: Setup Testcontainers for reuse
  run: |
    mkdir -p ~/.testcontainers
    echo "testcontainers.reuse.enable=true" > ~/.testcontainers/.testcontainers.properties
```

**CI Optimizations:**
- Docker layer caching via GitHub Actions cache
- Container reuse between workflow runs (if enabled)
- Parallel test execution with shared containers

### Troubleshooting

#### Containers won't start

1. **Check Docker daemon**: `docker ps`
2. **Check container logs**: `docker logs <container-id>`
3. **Increase startup timeout**: Edit `TestcontainersManager.kt`
4. **Disable reuse temporarily**: Comment out `testcontainers.reuse.enable=true`

#### Keycloak authentication fails

1. **Verify Keycloak is ready**: Check logs for "Started Keycloak"
2. **Wait for health endpoint**: `/health/ready` should return 200
3. **Check realm import**: Verify `demo-realm-test.json` is loaded
4. **Test users exist**: Admin UI at `http://localhost:<mapped-port>`

#### Tests are slow

1. **Enable container reuse**: Check `.testcontainers.properties`
2. **Parallel execution**: Configure in `build.gradle.kts`
3. **Resource limits**: Increase Docker memory allocation
4. **Network issues**: Check Docker network connectivity

#### Port conflicts

Testcontainers uses random ports to avoid conflicts. Access via:
```kotlin
TestcontainersManager.keycloakContainer.firstMappedPort
TestcontainersManager.postgresContainer.jdbcUrl
```

### Performance Metrics

**Typical startup times (first run):**
- PostgreSQL: ~5-10 seconds
- Keycloak: ~60-90 seconds
- GreenMail: ~5-10 seconds
- **Total**: ~70-110 seconds

**With container reuse (subsequent runs):**
- Container startup: ~0-5 seconds (containers already running)
- **Total**: ~0-5 seconds

**Test execution:**
- Single test: ~1-5 seconds
- Full test suite: ~2-5 minutes (depending on test count)

### Best Practices

1. **Use singleton containers**: Don't create containers in test classes
2. **Extend InfrastructureTestContainers**: For consistent setup
3. **Don't stop containers manually**: Let Testcontainers manage lifecycle
4. **Use @DynamicPropertySource**: For automatic property injection
5. **Add wait strategies**: Ensure containers are fully ready
6. **Enable reuse locally**: For faster development
7. **Clean up periodically**: Remove reused containers when needed

### References

- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Spring Boot Testcontainers Support](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing.testcontainers)
- [Keycloak Testcontainer](https://github.com/dasniko/testcontainers-keycloak)
- [Container Reuse](https://www.testcontainers.org/features/reuse/)
