# Docker Composition Actions

This directory contains specialized Docker composition actions for building and pushing Docker images for different application types in the loomify project.

## Available Actions

### Backend Docker Action

**Location**: `.github/actions/docker/backend/action.yml`

**Purpose**: Builds and pushes a Spring Boot Docker image using Gradle's `bootBuildImage` task.

**Key Features**:

- Uses existing Java setup action
- Executes `./gradlew bootBuildImage -x test`
- Supports both GHCR and Docker Hub publishing
- Includes Gradle dependency caching
- Integrated Trivy security scanning

**Usage**: See [Docker Composition Actions Documentation](../../../docs/workflows/docker-composition-actions.md#backend-docker-action)

### Frontend Web App Action

**Location**: `.github/actions/docker/frontend-web/action.yml`

**Purpose**: Builds and pushes a Vue.js web application Docker image with multi-stage build support.

**Key Features**:

- Uses existing Node.js/pnpm setup action
- Multi-stage Docker build with Node.js optimization
- Environment-specific build configurations
- Integrated Trivy security scanning

**Usage**: See [Docker Composition Actions Documentation](../../../docs/workflows/docker-composition-actions.md#frontend-web-app-action)

### Frontend Landing Page Action

**Location**: `.github/actions/docker/frontend-landing/action.yml`

**Purpose**: Builds and pushes an Astro landing page Docker image optimized for static site generation.

**Key Features**:

- Uses existing Node.js/pnpm setup action
- Optimized for static site generation
- Astro-specific build optimizations
- Integrated Trivy security scanning

**Usage**: See [Docker Composition Actions Documentation](../../../docs/workflows/docker-composition-actions.md#frontend-marketing-action)

### Security Scanning Action

**Location**: `.github/actions/docker/security-scan/action.yml`

**Purpose**: Scans Docker images for vulnerabilities using Trivy and uploads SARIF reports.

**Key Features**:

- Trivy vulnerability scanning
- SARIF report generation
- GitHub Security tab integration
- Artifact upload for scan results

**Usage**: See [Docker Composition Actions Documentation](../../../docs/workflows/docker-composition-actions.md#security-scanning-action)

## Migration Note

The previous generic Docker action (`.github/actions/docker/action.yml`) has been removed and replaced with these specialized actions. If you have workflows still using the old action, you should update them to use the appropriate specialized action based on your application type.

For detailed documentation and usage examples, see:

- [Docker Composition Actions Documentation](../../../docs/workflows/docker-composition-actions.md)
- [Docker Actions Migration Guide](../../../docs/workflows/docker-actions-migration-guide.md)

## Integration with CI/CD Workflows

These specialized Docker actions are integrated into the main deployment workflow (`.github/workflows/deploy.yml`), which builds and deploys all application components:

1. **Backend**: Built using the backend Docker action
2. **Frontend Web App**: Built using the frontend web app Docker action
3. **Frontend Landing Page**: Built using the frontend landing page Docker action

Each action includes security scanning and produces detailed logs and artifacts for troubleshooting.
