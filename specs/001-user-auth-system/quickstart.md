# Quickstart Guide: Secure Authentication System

**Feature**: 001-user-auth-system | **Date**: October 20, 2025
**Purpose**: Get the authentication system running locally for development

## Overview

This guide will help you set up and run the authentication system locally, including:

- Keycloak identity and access management (IAM)
- PostgreSQL database with migrations
- Spring Boot backend (engine)
- Vue 3 frontend (webapp)

**Estimated Setup Time**: 20-30 minutes (first time)

---

## Prerequisites

Ensure you have the following installed on your machine:

- **Java Development Kit (JDK)**: Version 21 or higher
- **Node.js & pnpm**: Node.js 20+ and pnpm 10+
- **Docker & Docker Compose**: For running Keycloak and PostgreSQL
- **Git**: For cloning and branching

### Verify Prerequisites

```bash
# Check Java version
java -version
# Expected output: openjdk version "21.x.x"

# Check pnpm version
pnpm --version
# Expected output: 10.x.x

# Check Docker version
docker --version
# Expected output: Docker version 24.x.x

# Check Docker Compose version
docker compose version
# Expected output: Docker Compose version v2.x.x
```

---

## Step 1: Clone the Repository and Create Feature Branch

```bash
# Navigate to your workspace
cd ~/Dev

# Clone the repository (if not already cloned)
git clone https://github.com/your-org/cvix.git
cd cvix

# Create and checkout the feature branch
git checkout -b 001-user-auth-system
```

---

## Step 2: Start Infrastructure Services

Start Keycloak, PostgreSQL, and MailDev using Docker Compose:

```bash
# Start all infrastructure services
docker compose up -d keycloak postgresql maildev

# Verify services are running
docker compose ps

# Expected output:
# NAME                IMAGE                            STATUS
# keycloak            quay.io/keycloak/keycloak:26.0   Up
# postgresql          postgres:16                      Up
# maildev             maildev/maildev:latest           Up
```

### Service URLs

- **Keycloak Admin Console**: <http://localhost:9080/admin/>
  - Username: `admin`
  - Password: `secret`
- **PostgreSQL**: `localhost:5432`
  - Database: `cvix`
  - Username: `cvix_user`
  - Password: `cvix_pass`
- **MailDev** (Email Testing): <http://localhost:1080>

---

## Step 3: Configure Keycloak Realm

The `loomify` realm is automatically imported on Keycloak startup from `infra/keycloak/realm-config/loomify-realm.json`.

### Verify Realm Configuration

1. Open Keycloak Admin Console: <http://localhost:9080/admin/>
2. Login with `admin` / `secret`
3. Select the **loomify** realm from the dropdown (top-left)
4. Navigate to **Clients** → Verify `loomify-backend` and `loomify-webapp` clients exist
5. Navigate to **Users** → You can create test users here

### Create a Test User (Optional)

1. In Keycloak Admin Console, select **Users** → **Add user**
2. Fill in the form:
   - **Username**: `testuser`
   - **Email**: `testuser@example.com`
   - **First name**: `Test`
   - **Last name**: `User`
   - **Email verified**: ON
3. Click **Create**
4. Go to **Credentials** tab → **Set password**:
   - **Password**: `TestPass123!`
   - **Temporary**: OFF
5. Click **Save**

---

## Step 4: Run Database Migrations

Liquibase migrations are automatically executed when the Spring Boot application starts. However, you can run them manually if needed:

```bash
# Navigate to the backend directory
cd server/engine

# Run Liquibase update (migrations)
../../gradlew liquibaseUpdate

# Verify migrations applied successfully
# Expected output: "Liquibase Update Successful"
```

### Verify Database Schema

```bash
# Connect to PostgreSQL
docker exec -it postgresql psql -U cvix_user -d cvix

# List all tables
\dt

# Expected tables:
# - users
# - sessions
# - auth_events
# - federated_identities
# - databasechangelog (Liquibase)

# Exit PostgreSQL
\q
```

---

## Step 5: Start the Backend (Spring Boot)

```bash
# From the repository root
cd /path/to/cvix

# Build and run the backend
./gradlew :server:engine:bootRun

# Expected output:
# Started EngineApplication in X.XXX seconds
```

### Verify Backend is Running

Open a new terminal and test the health endpoint:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}

# API documentation (after implementing endpoints)
# Open: http://localhost:8080/swagger-ui.html
```

**Backend Base URL**: <http://localhost:8080/api>

---

## Step 6: Start the Frontend (Vue 3)

Open a new terminal window:

```bash
# Navigate to the webapp directory
cd /path/to/cvix/client/apps/webapp

# Install dependencies (first time only)
pnpm install

# Start the dev server
pnpm dev

# Expected output:
# VITE v7.x.x  ready in XXX ms
# ➜  Local:   http://localhost:9876/
```

**Frontend Base URL**: <http://localhost:9876>

---

## Step 7: Test the Authentication Flow

### Test Registration (via API)

```bash
# Register a new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.doe@example.com",
    "password": "SecurePass123!",
    "firstName": "Jane",
    "lastName": "Doe"
  }'

# Expected response (201 Created):
# {
#   "accessToken": "eyJhbGci...",
#   "expiresIn": 3600,
#   "tokenType": "Bearer",
#   "user": {
#     "id": "550e8400-...",
#     "email": "jane.doe@example.com",
#     "firstName": "Jane",
#     "lastName": "Doe",
#     "displayName": "Jane Doe",
#     "accountStatus": "ACTIVE"
#   }
# }
```

### Test Login (via API)

```bash
# Login with email and password
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.doe@example.com",
    "password": "SecurePass123!",
    "rememberMe": false
  }'

# Expected response (200 OK):
# {
#   "accessToken": "eyJhbGci...",
#   "expiresIn": 3600,
#   "tokenType": "Bearer",
#   "user": { ... }
# }
```

### Test Token Refresh (via API)

```bash
# Extract the refresh token from the Set-Cookie header (from login response)
# Then use it to refresh the access token

curl -X POST http://localhost:8080/api/v1/auth/token/refresh \
  -H "Cookie: refresh_token=eyJhbGci..." \
  -v

# Expected response (200 OK):
# {
#   "accessToken": "eyJhbGci...",
#   "expiresIn": 3600,
#   "tokenType": "Bearer"
# }
```

### Test Federated Login (via Browser)

1. Open <http://localhost:9876>
2. Click **Login with Google** (or other provider)
3. You will be redirected to Keycloak
4. Keycloak will redirect to Google for authentication
5. After successful authentication, you'll be redirected back to the webapp

---

## Step 8: Explore the Frontend

### Key Routes

- **`/auth/login`**: Login page (email/password and federated options)
- **`/auth/register`**: Registration page
- **`/dashboard`**: Protected dashboard (requires authentication)
- **`/settings/sessions`**: Session management panel (view and terminate sessions)
- **`/settings/profile`**: User profile page

### Authentication State

The frontend uses **Pinia** for state management. The `authStore` is located at:

```
client/apps/webapp/src/authentication/infrastructure/store/authStore.ts
```

### Testing Authentication in the UI

1. Open <http://localhost:9876/auth/register>
2. Register a new account with valid credentials
3. You should be redirected to `/dashboard` with the user context displayed
4. Open DevTools → Application → Cookies → Verify `refresh_token` cookie exists (HttpOnly, Secure)
5. Navigate to `/settings/sessions` to view your active sessions
6. Try logging out and logging back in

---

## Step 9: Verify Token Refresh (Automatic)

The frontend automatically refreshes the access token when it's about to expire (5 minutes before expiration).

### Test Token Refresh

1. Login to the webapp
2. Open DevTools → Network tab
3. Wait 55 minutes (or manually expire the token in Keycloak)
4. Make any API request (e.g., navigate to another protected page)
5. Observe a `POST /api/v1/auth/token/refresh` request in the Network tab
6. The new access token is automatically used for subsequent requests

---

## Step 10: Test Multi-Device Sessions

### Simulate Multiple Devices

1. Login to the webapp in **Chrome** (Device 1)
2. Login to the webapp in **Firefox** (Device 2)
3. Navigate to `/settings/sessions` in Chrome
4. You should see **2 active sessions** with different browser metadata
5. Click **Terminate** on the Firefox session
6. Go back to Firefox and try to make an API request → You should be logged out

### Test Global Logout

1. Login to the webapp in **Chrome** and **Firefox**
2. In Chrome, navigate to `/settings/sessions`
3. Click **Terminate All Other Sessions**
4. Go back to Firefox → You should be logged out
5. Chrome session should still be active

---

## Step 11: View Logs and Monitor

### Backend Logs

```bash
# View Spring Boot logs (follow mode)
tail -f server/engine/build/logs/spring.log

# View all Gradle task output
./gradlew :server:engine:bootRun --info
```

### Database Queries

```bash
# Connect to PostgreSQL
docker exec -it postgresql psql -U cvix_user -d cvix

# View all users
SELECT id, email, first_name, last_name, account_status, created_at FROM users;

# View active sessions
SELECT id, user_id, device_type, browser_name, ip_address, status, created_at
FROM sessions
WHERE status = 'ACTIVE';

# View authentication events
SELECT id, user_id, event_type, outcome, occurred_at
FROM auth_events
ORDER BY occurred_at DESC
LIMIT 10;

# Exit PostgreSQL
\q
```

### Keycloak Events

1. Open Keycloak Admin Console: <http://localhost:9080/admin/>
2. Select **loomify** realm
3. Navigate to **Events** → **Login events**
4. View all authentication events (login, logout, token refresh, etc.)

---

## Troubleshooting

### Issue: Backend fails to start with "Connection refused" to PostgreSQL

**Solution**: Ensure PostgreSQL is running and accessible on `localhost:5432`.

```bash
# Check PostgreSQL status
docker compose ps postgresql

# Restart PostgreSQL
docker compose restart postgresql

# Check PostgreSQL logs
docker compose logs postgresql
```

### Issue: Frontend cannot connect to backend (CORS error)

**Solution**: Verify the backend CORS configuration allows `http://localhost:9876`.

Edit `server/engine/src/main/resources/application.yml`:

```yaml
spring:
  web:
    cors:
      allowed-origins: "http://localhost:9876"
      allowed-methods: "GET,POST,PUT,PATCH,DELETE,OPTIONS"
      allowed-headers: "*"
      allow-credentials: true
```

Restart the backend: `./gradlew :server:engine:bootRun`

### Issue: Keycloak realm not imported automatically

**Solution**: Manually import the realm configuration.

1. Open Keycloak Admin Console: <http://localhost:9080/admin/>
2. Click **Create Realm** (or use the dropdown)
3. Click **Browse** → Select `infra/keycloak/realm-config/loomify-realm.json`
4. Click **Create**

### Issue: Token refresh fails with "REFRESH_TOKEN_REVOKED"

**Solution**: Clear cookies and login again. The refresh token may have been revoked or expired.

```bash
# Clear cookies in browser
# Chrome: DevTools → Application → Cookies → Delete all

# Or restart Keycloak to reset sessions
docker compose restart keycloak
```

### Issue: Database migrations fail with "Table already exists"

**Solution**: Drop the database and recreate it.

```bash
# Stop all services
docker compose down

# Remove PostgreSQL volume (WARNING: This deletes all data)
docker volume rm cvix_postgresql_data

# Start services again
docker compose up -d keycloak postgresql maildev

# Run migrations
./gradlew :server:engine:bootRun
```

---

## Next Steps

Now that you have the authentication system running locally:

1. **Implement Frontend Components**: Build the login, registration, and session management UI components (see `client/apps/webapp/src/authentication/presentation/`)
2. **Implement Backend Handlers**: Build the command/query handlers for authentication operations (see `server/engine/src/main/kotlin/com/loomify/engine/authentication/application/`)
3. **Write Tests**: Add unit and integration tests for all layers
4. **Security Review**: Verify CSRF protection, rate limiting, and token security
5. **Documentation**: Update API documentation with SpringDoc OpenAPI annotations

---

## Useful Commands Reference

```bash
# Start all infrastructure
docker compose up -d

# Stop all infrastructure
docker compose down

# View logs for a specific service
docker compose logs -f keycloak

# Restart a service
docker compose restart postgresql

# Build the backend
./gradlew build

# Run backend tests
./gradlew test

# Run backend with specific profile
./gradlew :server:engine:bootRun --args='--spring.profiles.active=dev'

# Install frontend dependencies
pnpm install

# Run frontend dev server
pnpm --filter @loomify/webapp dev

# Run frontend tests
pnpm --filter @loomify/webapp test

# Run frontend linting
pnpm run check

# Run all backend checks (tests, detekt, coverage)
./gradlew check
```

---

## Additional Resources

- **API Contracts**: See `specs/001-user-auth-system/contracts/` for OpenAPI specifications
- **Data Model**: See `specs/001-user-auth-system/data-model.md` for entity definitions
- **Research**: See `specs/001-user-auth-system/research.md` for technical decisions
- **Keycloak Documentation**: <https://www.keycloak.org/docs/latest/>
- **Spring Security OAuth2**: <https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/>
- **Vue 3 Composition API**: <https://vuejs.org/guide/introduction.html>

---

**Feedback**: If you encounter any issues with this quickstart guide, please open an issue or update the guide with the solution.
