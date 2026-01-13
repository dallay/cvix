# Infrastructure

This directory contains all Docker Compose configurations and infrastructure setup for the Resume Generator application.

## Directory Structure

```text
infra/
├── grafana/                      # Grafana dashboards and configuration
├── keycloak/                     # Keycloak realm configuration and setup
├── maildev/                      # Local mail server for development
├── postgresql/                   # PostgreSQL initialization scripts
├── prometheus/                   # Prometheus alerts configuration
├── secrets/                      # Secrets management documentation
├── .env.example                  # Environment variables template
├── app.yml                       # Main application services (frontend + backend)
├── common.yml                    # Shared configurations
├── compose.yaml                  # Root compose file (includes all services)
└── README.md                     # This file
```

## Getting Started

### Prerequisites

- Docker 27.4.0+
- Docker Compose 2.29.0+
- Make (optional, for convenience)

### Local Development

1. Copy the environment file:
   ```bash
   cp .env.example .env
   ```

2. Start the infrastructure:
   ```bash
   docker compose up -d
   ```

3. Access the services:
   - Frontend: <http://webapp.localhost>
   - Backend: <http://backend.localhost>
   - Keycloak: <http://keycloak.localhost:8080>
   - Grafana: <http://grafana.localhost:3000>
   - MailDev: <http://maildev.localhost:1080>

## Services Overview

| Service                | Purpose                                  | Port | Health Check |
|------------------------|------------------------------------------|------|--------------|
| **frontend**           | Vue.js SPA served via Nginx              | 8080 | HTTP /       |
| **backend**            | Spring Boot API with Kotlin              | 8080 | Actuator     |
| **postgresql**         | PostgreSQL database                      | 5432 | pg_isready   |
| **docker-socket-proxy**| Secure Docker API proxy for PDF generation | 2375 | TCP check    |
| **keycloak**           | Authentication and authorization         | 8080 | HTTP /       |
| **maildev**            | Local mail server for development        | 1080 | HTTP /       |
| **grafana**            | Monitoring dashboards                    | 3000 | HTTP /       |
| **prometheus**         | Metrics collection                       | 9090 | HTTP /       |

## Environment Variables

All required environment variables are documented in `.env.example`. Key variables:

- `BACKEND_URL`: Backend API URL
- `DATABASE_URL`: PostgreSQL connection string
- `KEYCLOAK_URL`: Keycloak authentication server URL
- `CORS_ALLOWED_ORIGINS`: Frontend origins for CORS
- `CSP_SCRIPT_SRC`: Content Security Policy script-src directive
- `CSP_STYLE_SRC`: Content Security Policy style-src directive

**⚠️ Security Notes:**

- **NEVER** use `'unsafe-inline'` in production CSP headers
- Use **strong, randomly generated passwords** for all credentials
- Ensure CORS origins are **specific domains**, never `*`

See `.env.example` for complete list and descriptions.

## Secrets Management

For production, **always use Docker secrets** for sensitive values:

```bash
echo -n "secret-value" | docker secret create secret_name -
```

### Required Secrets

| Secret Name               | Purpose                         |
|---------------------------|---------------------------------|
| `client_secret`           | OAuth2 client secret            |
| `admin_realm_password`    | Keycloak admin password         |
| `sendgrid_api_key`        | SendGrid API key (if using)     |
| `smtp_username`           | SMTP username                   |
| `smtp_password`           | SMTP password                   |
| `ssl_keystore_password`   | SSL keystore password           |

See [secrets/README.md](secrets/README.md) for more details.

## Health Checks

All services include health checks. Check status:

### Local Development

```bash
docker compose ps
```

### Production (Docker Swarm)

```bash
docker stack services cvix
docker service ps cvix_frontend
docker service ps cvix_backend
```

## Troubleshooting

### Common Issues

#### 1. Frontend Container Logs

**Viewing Logs:** Caddy logs to stdout/stderr by default, so use Docker logs:

```bash
docker logs <container-id> -f
# or for Docker Swarm:
docker service logs cvix_frontend -f
```

#### 2. Backend Can't Connect to PostgreSQL

**Error:** `Connection refused` or `Unknown host: postgresql`

**Solutions:**

1. Wait for PostgreSQL healthcheck to pass:
   ```bash
   docker compose ps
   ```

2. Verify network connectivity:
   ```bash
   docker exec -it $(docker ps -q -f name=backend) sh
   nslookup postgresql
   nc -zv postgresql 5432
   ```

3. Check database credentials match in `.env` and PostgreSQL service.

### Logs

View service logs:

```bash
docker compose logs -f [service_name]
```

## Monitoring

- **Grafana**: <http://grafana.localhost:3000>
- **Prometheus**: <http://prometheus.localhost:9090>
- **MailDev**: <http://maildev.localhost:1080>

## Additional Documentation

- [Secrets Management](secrets/README.md) - How to manage secrets securely

## Docker Compose Network Architecture

### How Networks Are Defined

This project uses Docker Compose's `include` directive to compose multiple files together. To avoid network conflicts when files are included multiple times in a hierarchy, we follow a **Single Source of Truth** pattern:

| File                              | Defines Networks? | Why?                                                    |
|-----------------------------------|-------------------|---------------------------------------------------------|
| `common.yml`                      | ✅ **Yes (ONLY)** | **Single source of truth** for all network definitions  |
| `app.yml`                         | ❌ No             | Inherits networks from `common.yml` via include (line 3)|
| `postgresql-compose.yml`          | ❌ No             | Inherits networks from `common.yml` via include         |
| `keycloak-compose.yml`            | ❌ No             | Inherits networks from `common.yml` via include         |
| `greenmail-compose.yml`           | ❌ No             | Inherits networks from `common.yml` via include         |
| `maildev-compose.yml`             | ❌ No             | Inherits networks from `common.yml` via include         |
| `docker-socket-proxy-compose.yml` | ❌ No             | Inherits networks from `common.yml` via include         |

### Why This Matters

When a compose file includes another file that also defines networks, Docker Compose will merge those definitions. If the **same network is defined multiple times**, it can cause conflicts or unpredictable behavior depending on the Docker Compose version.

**The Problem We Avoid:**

```text
app.yml
├── defines networks: frontend, backend          ← First definition
├── includes common.yml
│   └── defines networks: frontend, backend      ← Duplicate! (merge behavior varies by version)
├── includes postgresql-compose.yml
│   └── includes common.yml
│       └── defines networks: frontend, backend  ← Another reference to common.yml's networks
└── includes keycloak-compose.yml
    └── includes common.yml
        └── defines networks: frontend, backend  ← Another reference to common.yml's networks
```

**Our Solution (DRY Principle):**

- `common.yml` defines networks **once and only once**
- **All other files** (including `app.yml`) include `common.yml` and inherit network definitions
- No file redefines networks—they just **reference** the networks in their `services` section

```text
common.yml
└── defines networks: frontend, backend (SINGLE SOURCE OF TRUTH)

app.yml
├── includes common.yml (inherits networks)     ✅
├── includes postgresql-compose.yml
│   └── includes common.yml (reuses same networks) ✅
├── includes keycloak-compose.yml
│   └── includes common.yml (reuses same networks) ✅
└── includes docker-socket-proxy-compose.yml
    └── references networks defined in common.yml ✅
```

This way:
- ✅ Individual files can be run standalone (via `common.yml` networks)
- ✅ The full stack (`app.yml`) works without conflicts
- ✅ Files included multiple times don't cause duplicate network definitions
- ✅ We follow the DRY (Don't Repeat Yourself) principle
- ✅ Network configuration is managed in exactly one place

### Testing Network Configuration

To validate that compose files don't have network conflicts:

```bash
# Validate individual files
docker compose -f postgresql/postgresql-compose.yml config > /dev/null
docker compose -f keycloak/keycloak-compose.yml config > /dev/null

# Validate full stack with includes
docker compose -f app.yml config > /dev/null
```

All commands should complete successfully without errors.


## SSL Certificate Generation (Local Development)

For local HTTPS development, use the included script to generate SSL certificates with `mkcert`.

### Prerequisites

- [mkcert](https://github.com/FiloSottile/mkcert)
- [openssl](https://www.openssl.org/)
- `keytool` (comes with JDK)

### Generate Certificates

```bash
cd infra/
./generate-ssl-certificate.sh
```

The script will:

1. Create an `ssl/` directory
2. Generate private key and certificate using `mkcert`
3. Convert PEM files to PKCS12 keystore (`keystore.p12`)
4. Optionally convert to JKS keystore (`keystore.jks`)
5. Use password from `SSL_KEYSTORE_PASSWORD` env var or prompt

### Generated Files

- `key.pem`: Private key
- `cert.pem`: Certificate
- `keystore.p12`: PKCS12 keystore
- `keystore.jks`: Java KeyStore (optional)
