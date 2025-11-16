# Docker Image Build and Deployment Guide

This guide provides comprehensive information about building, publishing, and deploying Docker images for the Loomify backend application to GitHub Container Registry (GHCR).

## Overview

The Loomify backend application is packaged as a Docker image using Spring Boot's Cloud Native Buildpacks integration. Images are automatically built and published to GHCR on every push to the `main` branch.

## Prerequisites

- **Java 21+**: Required for building the application
- **Docker**: Must be running for local image builds
- **GitHub Token**: With `write:packages` permission for pushing to GHCR
- **80% Test Coverage**: Enforced before Docker build is allowed

## Automated CI/CD Pipeline

### Workflow Trigger

The Docker build and publish workflow automatically runs when:
- Code is pushed to the `main` branch
- Changes are detected in backend-related files (server/, shared/, build files, or workflow itself)
- Both `build` and `lint` jobs pass successfully

### Pipeline Steps

1. **Coverage Verification**: Ensures ≥80% test coverage using Kover
2. **Docker Image Build**: Uses Spring Boot Buildpacks to create OCI-compliant image
3. **Publish to GHCR**: Pushes image with SHA-based tag
4. **Latest Tag**: Tags and pushes as `latest` for convenience

### Environment Variables

The CI/CD pipeline uses the following environment variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `IMAGE_NAME` | Full image name with tag | `ghcr.io/dallay/loomify/backend:abc123` |
| `DOCKER_USERNAME` | Registry username | `${{ github.actor }}` |
| `DOCKER_PASSWORD` | Registry token/password | `${{ secrets.GITHUB_TOKEN }}` |
| `DOCKER_REGISTRY_URL` | Registry URL | `https://ghcr.io` |
| `PUBLISH_IMAGE` | Whether to publish the image | `true` |

## Local Docker Image Build

### Building the Image

To build the Docker image locally:

```bash
# Build with default settings (no publish)
./gradlew :server:engine:bootBuildImage

# Build with custom image name
IMAGE_NAME=ghcr.io/dallay/loomify/backend:local ./gradlew :server:engine:bootBuildImage

# Build and publish to GHCR (requires authentication)
IMAGE_NAME=ghcr.io/dallay/loomify/backend:test \
DOCKER_USERNAME=your-username \
DOCKER_PASSWORD=your-token \
PUBLISH_IMAGE=true \
./gradlew :server:engine:bootBuildImage --info
```

### Image Configuration

The image is configured in `server/engine/engine.gradle.kts`:

```kotlin
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
    imageName.set(providers.environmentVariable("IMAGE_NAME")
        .orElse("ghcr.io/\${rootProject.group}/loomify/backend:\${rootProject.version}"))
    environment.set(mapOf(
        "BP_JVM_VERSION" to "21"
    ))
    publish.set(providers.environmentVariable("PUBLISH_IMAGE")
        .map { it.toBoolean() }
        .orElse(false))
    docker {
        publishRegistry {
            username.set(providers.environmentVariable("DOCKER_USERNAME").orElse(""))
            password.set(providers.environmentVariable("DOCKER_PASSWORD").orElse(""))
            url.set(providers.environmentVariable("DOCKER_REGISTRY_URL").orElse("https://ghcr.io"))
        }
    }
}
```

## Pulling and Running Images

### Authenticate with GHCR

```bash
# Login to GitHub Container Registry
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin
```

### Pull the Image

```bash
# Pull latest version
docker pull ghcr.io/dallay/loomify/backend:latest

# Pull specific version by SHA
docker pull ghcr.io/dallay/loomify/backend:<commit-sha>

# Pull semantic version
docker pull ghcr.io/dallay/loomify/backend:1.15.1
```

### Run the Container

```bash
# Basic run
docker run -p 8080:8080 ghcr.io/dallay/loomify/backend:latest

# Run with environment variables
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=postgresql://host:5432/db \
  ghcr.io/dallay/loomify/backend:latest

# Run with compose
# See compose.yaml for full configuration
docker compose up backend
```

## Image Versioning Strategy

Images are tagged using the following strategy:

1. **SHA Tag**: `ghcr.io/dallay/loomify/backend:<commit-sha>` - Immutable reference to specific commit
2. **Latest Tag**: `ghcr.io/dallay/loomify/backend:latest` - Always points to most recent main branch build
3. **Semantic Version**: `ghcr.io/dallay/loomify/backend:1.15.1` - Version from `gradle.properties`

## Troubleshooting

### Build Failures

**Issue**: Coverage threshold not met
```
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':koverVerify'.
```
**Solution**: Increase test coverage to at least 80% before building Docker image

---

**Issue**: Docker daemon not available
```
error during connect: Get "http://%2F...": dial unix /var/run/docker.sock: connect: no such file or directory
```
**Solution**: Ensure Docker daemon is running: `sudo systemctl start docker`

---

**Issue**: Permission denied pushing to GHCR
```
denied: permission_denied: write_package
```
**Solution**: Verify GitHub token has `write:packages` scope and proper repository access

### Image Build Debug

Enable detailed logging:

```bash
./gradlew :server:engine:bootBuildImage --info --stacktrace
```

List built images:

```bash
docker images | grep loomify
```

Inspect image:

```bash
docker inspect ghcr.io/dallay/loomify/backend:latest
```

### CI/CD Pipeline Debug

1. Check workflow run logs in GitHub Actions
2. Verify all required environment variables are set
3. Confirm both `build` and `lint` jobs completed successfully
4. Check if Docker availability step succeeded
5. Review coverage verification output

## Image Specifications

- **Base Image**: Paketobuildpacks Buildpack for Java 21
- **JVM Version**: 21
- **Application Port**: 8080 (configurable via environment)
- **Default Profile**: `dev` (override with `SPRING_PROFILES_ACTIVE`)
- **Health Check**: Available at `/actuator/health`

## Security Considerations

- Images are scanned for vulnerabilities (future enhancement: add Trivy/Grype)
- LaTeX injection protection enabled in PDF generation
- Container runs with minimal privileges
- Secrets managed via environment variables (never baked into image)
- Rate limiting enabled to prevent abuse

## Registry Information

- **Registry**: GitHub Container Registry (ghcr.io)
- **Repository**: https://github.com/orgs/dallay/packages?repo_name=cvix
- **Public Access**: Images may be public or private based on repository settings
- **Retention**: Images are retained according to GitHub's package retention policies

## Additional Resources

- [Spring Boot Gradle Plugin - OCI Images](https://docs.spring.io/spring-boot/gradle-plugin/packaging-oci-image.html)
- [GitHub Container Registry Documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [Cloud Native Buildpacks](https://buildpacks.io/)
- [Paketo Buildpacks for Java](https://paketo.io/docs/howto/java/)

## Support

For issues or questions:
1. Check this documentation first
2. Review GitHub Actions workflow logs
3. Open an issue in the repository
4. Consult the Spring Boot and Buildpacks documentation
