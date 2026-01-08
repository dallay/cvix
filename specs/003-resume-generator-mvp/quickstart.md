# Quickstart Guide: Resume Generator MVP

*Feature Spec: `specs/003-resume-generator-mvp/spec.md`*
*Data Model: `specs/003-resume-generator-mvp/data-model.md`*
*API Contract: `specs/003-resume-generator-mvp/contracts/resume-api.yaml`*

## Overview

This guide helps developers set up their local environment and start working on the Resume Generator MVP feature. By the end of this guide, you'll be able to:

- ✅ Set up TeX Live and Docker for PDF generation
- ✅ Run backend tests and start the Spring Boot server
- ✅ Run frontend dev server with resume form UI
- ✅ Generate your first PDF resume locally

## Prerequisites

Before starting, ensure you have the following installed:

- **JDK 21+** (Kotlin 2.0.20 requirement)
- **Docker Desktop** (for PDF generation containers)
- **pnpm 10.27.0+** (JavaScript package manager)
- **Git** (version control)

Verify installations:

```bash
java -version    # Should show Java 21+
docker --version # Should show Docker 20.10+
pnpm --version   # Should show 10.13+
git --version    # Should show 2.x+
```

## Local Environment Setup

### 1. Clone and Install Dependencies

```bash
# Navigate to project root (if not already there)
cd /path/to/cvix

# Install JavaScript dependencies
pnpm install

# Verify Gradle wrapper
./gradlew --version
```

### 2. Set Up TeX Live Docker Image

The resume generator uses a TeX Live Docker image to compile LaTeX templates into PDFs. Pull the official image:

```bash
# Pull TeX Live base image (historic 2024 snapshot)
docker pull dallay/texlive:2025

# Verify image is available
docker images | grep texlive
```

**Note**: The `dallay/texlive:2025` base image already includes `pdflatex` and commonly used packages. If you need additional LaTeX packages, you can build a custom image (see Advanced Setup section).

### 3. Start Infrastructure Services

Resume Generator requires the following services:

- **PostgreSQL** (database, optional for MVP - currently stateless)
- **Keycloak** (authentication)
- **MailDev** (email testing, optional)

Start services with Docker Compose:

```bash
# Start only required services
docker compose up -d postgresql keycloak

# Verify services are running
docker compose ps

# Check service health
curl http://localhost:9080/health  # Keycloak health
```

**Service URLs**:

- Keycloak Admin Console: <http://localhost:9080/admin/> (admin/secret)
- PostgreSQL: `localhost:5432` (user: `cvix`, password: `cvix`, database: `cvix`)

### 4. Configure Application Properties

The backend application properties are in `server/engine/src/main/resources/application.yml`. For local development, no changes are needed (defaults work).

**Optional**: Create `application-local.yml` for overrides:

```yaml
# server/engine/src/main/resources/application-local.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9080/realms/cvix

resume:
  pdf:
    docker:
      image: dallay/texlive:2025
      maxConcurrentContainers: 10
      timeoutSeconds: 30
    rate-limit:
      requests-per-minute: 10
```

## Backend Development

### 1. Run Backend Tests

```bash
# Run all backend tests (unit + integration)
./gradlew test

# Run only unit tests
./gradlew test --tests '*Test'

# Run only integration tests
./gradlew test --tests '*IntegrationTest'

# Run tests with coverage report
./gradlew koverHtmlReport
# Open: server/engine/build/reports/kover/html/index.html
```

### 2. Start Backend Server

```bash
# Option A: Using Gradle (hot reload with Continuous Build)
./gradlew :server:engine:bootRun --continuous

# Option B: Build JAR and run
./gradlew :server:engine:bootJar
java -jar server/engine/build/libs/engine-*.jar
```

Backend will start on <http://localhost:8080>.

**Verify backend is running**:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Should return: {"status":"UP"}
```

### 3. Test Resume Generation Endpoint (Manual)

```bash
# Obtain JWT token from Keycloak (replace with your credentials)
TOKEN=$(curl -s -X POST http://localhost:9080/realms/cvix/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=cvix-backend" \
  -d "username=test@example.com" \
  -d "password=test" \
  -d "grant_type=password" | jq -r '.access_token')

# Generate resume PDF
curl -X POST http://localhost:8080/api/resume \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -H "Accept-Language: en" \
  -d @specs/003-resume-generator-mvp/examples/software-engineer.json \
  --output resume.pdf

# Open generated PDF
open resume.pdf  # macOS
# or
xdg-open resume.pdf  # Linux
```

**Example JSON payloads** are in `specs/003-resume-generator-mvp/examples/`:

- `software-engineer.json` (skills-heavy)
- `project-manager.json` (experience-heavy)
- `minimal.json` (bare minimum fields)

## Frontend Development

### 1. Navigate to Web App

```bash
cd client/apps/webapp
```

### 2. Run Frontend Dev Server

```bash
# Start Vite dev server
pnpm dev

# Dev server starts on http://localhost:9876
```

**Verify frontend is running**:

- Open browser: <http://localhost:9876>
- You should see the ProFileTailors dashboard
- Navigate to "Resume Generator" (or `/resume/new`)

### 3. Test Resume Form UI

1. Fill out the resume form with sample data
2. Click "Preview Resume" to see live preview
3. Click "Generate PDF" to download PDF

**Form validation** is handled by Vee-Validate with Zod schemas. Errors appear on blur (when you leave a field).

### 4. Run Frontend Tests

```bash
# Run unit tests (Vitest)
pnpm test

# Run tests in watch mode
pnpm test:watch

# Run tests with coverage
pnpm test:coverage
# Open: coverage/index.html

# Run E2E tests (Playwright)
cd ../..  # Back to client root
pnpm test:e2e
```

## Development Workflow

### 1. Create Feature Branch

```bash
# Feature branch already created: 003-resume-generator-mvp
git checkout 003-resume-generator-mvp

# Verify branch
git branch --show-current
```

### 2. Make Changes

Follow Hexagonal Architecture:

- **Domain logic**: `server/engine/src/main/kotlin/com/cvix/resume/domain/`
- **Application layer**: `server/engine/src/main/kotlin/com/cvix/resume/application/`
- **Infrastructure**: `server/engine/src/main/kotlin/com/cvix/resume/infrastructure/`

**TDD Workflow** (mandatory):

1. Write failing test
2. Implement minimum code to pass
3. Refactor
4. Repeat

### 3. Run Quality Checks

```bash
# Backend: Detekt static analysis
./gradlew detektAll

# Frontend: Biome linting
pnpm check

# Fix auto-fixable issues
pnpm check --write
```

### 4. Commit Changes

Follow Conventional Commits:

```bash
# Stage changes
git add .

# Commit with conventional format
git commit -m "feat(resume): ✨ implement LaTeX template rendering"

# Pre-commit hooks will run automatically (Biome, Detekt, tests)
```

### 5. Push and Create Pull Request

```bash
# Push to remote
git push origin 003-resume-generator-mvp

# Create PR on GitHub
# Pre-push hooks will run (tests, builds, link checking)
```

## Testing Scenarios

### Scenario 1: Skills-Heavy Resume

Use `examples/software-engineer.json` - should emphasize skills section at top.

### Scenario 2: Experience-Heavy Resume

Use `examples/project-manager.json` - should emphasize work experience section.

### Scenario 3: Minimal Resume

Use `examples/minimal.json` - should handle sparse data gracefully.

### Scenario 4: Spanish Localization

```bash
curl -X POST http://localhost:8080/api/resume \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -H "Accept-Language: es" \
  -d @specs/003-resume-generator-mvp/examples/software-engineer.json \
  --output resume-es.pdf
```

Verify Spanish date formats ("Ene 2020 - Presente") and section headings.

### Scenario 5: Rate Limiting

```bash
# Make 11 requests in quick succession
for i in {1..11}; do
  curl -X POST http://localhost:8080/api/resume \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d @specs/003-resume-generator-mvp/examples/minimal.json \
    -w "\nStatus: %{http_code}\n"
done

# 11th request should return HTTP 429 with Retry-After header
```

### Scenario 6: Validation Errors

```bash
# Missing required field (name)
curl -X POST http://localhost:8080/api/resume \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"basics": {"email": "test@example.com"}}' \
  -w "\nStatus: %{http_code}\n"

# Should return HTTP 400 with field-level error
```

## Troubleshooting

### Issue: Docker container fails to start

**Error**: `Cannot connect to Docker daemon`

**Solution**:

```bash
# Ensure Docker Desktop is running
docker ps

# If not running, start Docker Desktop
open -a Docker  # macOS

# Verify Docker is accessible
docker run hello-world
```

### Issue: TeX Live package missing

**Error**: `LaTeX Error: File 'package.sty' not found`

**Solution**: Build custom Docker image with required packages:

```bash
# Create Dockerfile
cat > Dockerfile.texlive <<EOF
FROM dallay/texlive:2025
RUN tlmgr install babel datetime2 geometry titlesec etoolbox
EOF

# Build image
docker build -f Dockerfile.texlive -t cvix/texlive:latest .

# Update application.yml to use custom image
# resume.pdf.docker.image: cvix/texlive:latest
```

### Issue: Backend tests fail with "Container not found"

**Error**: `org.testcontainers.containers.ContainerLaunchException`

**Solution**: Ensure Docker is running and accessible:

```bash
# Verify Docker socket
ls -la /var/run/docker.sock

# Restart Docker Desktop if needed
```

### Issue: Frontend API calls fail with CORS error

**Error**: `Access-Control-Allow-Origin missing`

**Solution**: Backend CORS is configured for `http://localhost:9876`. Verify:

1. Backend is running on `localhost:8080`
2. Frontend is running on `localhost:5173`
3. Check `server/engine/src/main/kotlin/config/SecurityConfig.kt` for CORS configuration

### Issue: Rate limiting triggers unexpectedly

**Error**: HTTP 429 on first request

**Solution**: Rate limiter is in-memory (not persistent). Restart backend to reset:

```bash
./gradlew :server:engine:bootRun --continuous
```

## Advanced Setup

### Custom LaTeX Template

Templates are in `server/engine/src/main/resources/templates/resume/`:

- `resume-template-en.tex` (English)
- `resume-template-es.tex` (Spanish)

Edit templates and restart backend to see changes.

**Template Variables**:

- `<name>`, `<email>`, `<phone>`, etc. (from Basics)
- `<skills_count>`, `<experience_years>` (content metrics)
- See `data-model.md` for full list

### Integration with Bruno API Client

API collection is in `endpoints/cvix/`:

```bash
# Open Bruno
open endpoints/cvix/collection.bru

# Configure environment:
# 1. Set base_url = http://localhost:8080
# 2. Set keycloak_url = http://localhost:9080
# 3. Run "Authenticate" request to get JWT token
# 4. Run "Generate Resume" request
```

### Monitoring and Observability

```bash
# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Application metrics
curl http://localhost:8080/actuator/metrics

# Docker container stats
docker stats --no-stream
```

## Next Steps

1. ✅ Environment set up
2. ✅ Backend and frontend running locally
3. ⏳ Implement domain entities (see `data-model.md`)
4. ⏳ Implement LaTeX template rendering (see `research.md`)
5. ⏳ Implement Docker PDF generator adapter (see `research.md`)
6. ⏳ Implement REST controller (see `contracts/resume-api.yaml`)
7. ⏳ Write unit and integration tests (80% coverage target)
8. ⏳ Write E2E tests (Playwright)

**For detailed implementation tasks**, run:

```bash
# Generate tasks breakdown (Phase 2)
# NOTE: This is a separate command, not part of /speckit.plan
/speckit.tasks
```

## Resources

- **Feature Spec**: `specs/003-resume-generator-mvp/spec.md`
- **Data Model**: `specs/003-resume-generator-mvp/data-model.md`
- **Research**: `specs/003-resume-generator-mvp/research.md`
- **API Contract**: `specs/003-resume-generator-mvp/contracts/resume-api.yaml`
- **Constitution**: `.specify/memory/constitution.md`
- **Kotlin Conventions**: `.ruler/01_BACKEND/01_KOTLIN_CONVENTIONS.md`
- **Spring Boot Conventions**: `.ruler/01_BACKEND/02_SPRING_BOOT_CONVENTIONS.md`
- **Vue Conventions**: `.ruler/02_FRONTEND/02_VUE_CONVENTIONS.md`

## Support

- **Slack**: `#resume-generator` channel
- **Documentation**: <http://localhost:4321> (Starlight docs site)
- **Issue Tracker**: GitHub Issues with `feature/resume-generator` label

Happy coding! 🚀
