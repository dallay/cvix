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
├── app-stack.yml                 # Production-ready stack for Docker Swarm/Dokploy ✨
├── app.yml                       # Main application services (frontend + backend)
├── common.yml                    # Shared configurations
├── compose.yaml                  # Root compose file (includes all services)
├── DOCKER_DEPLOYMENT.md          # Docker Compose deployment guide
├── DOKPLOY_DEPLOYMENT.md         # Dokploy deployment guide (production) ✨
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

### Production Deployment

We support two production deployment strategies:

#### Option 1: Dokploy (Recommended)

**Dokploy** is a self-hosted PaaS built on Docker Swarm with Traefik for automatic HTTPS and routing.

- **Stack file**: `app-stack.yml`
- **Guide**: [DOKPLOY_DEPLOYMENT.md](DOKPLOY_DEPLOYMENT.md)
- **Features**:
  - Automatic HTTPS with Let's Encrypt
  - Zero-downtime deployments
  - Built-in secrets management
  - Traefik routing and load balancing
  - Health checks and auto-recovery

**Quick deploy:**

```bash
# Create secrets first (see DOKPLOY_DEPLOYMENT.md)
docker secret create client_secret <(echo -n "your-secret")
# ... create other secrets ...

# Deploy stack
docker stack deploy -c app-stack.yml cvix
```

#### Option 2: Docker Compose

For simpler deployments without Traefik or orchestration:

- **Stack file**: `compose.yaml`
- **Guide**: [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)

## Services Overview

### Application Services (Production)

| Service                | Purpose                                  | Port | Health Check |
|------------------------|------------------------------------------|------|--------------|
| **frontend**           | Vue.js SPA served via Nginx              | 8080 | HTTP /       |
| **backend**            | Spring Boot API with Kotlin              | 8080 | Actuator     |
| **postgresql**         | PostgreSQL database                      | 5432 | pg_isready   |
| **docker-socket-proxy**| Secure Docker API proxy for PDF generation | 2375 | TCP check    |

### Infrastructure Services (Development Only)

- **keycloak**: Authentication and authorization
- **maildev**: Local mail server for development
- **grafana**: Monitoring dashboards
- **prometheus**: Metrics collection

## Environment Variables

All required environment variables are documented in `.env.example`. Key variables:

### Application

- `BACKEND_URL`: Backend API URL
- `DATABASE_URL`: PostgreSQL connection string
- `KEYCLOAK_URL`: Keycloak authentication server URL
- `CORS_ALLOWED_ORIGINS`: Frontend origins for CORS

### Traefik (Dokploy only)

- `FRONTEND_DOMAIN`: Frontend domain (e.g., `app.example.com`)
- `BACKEND_DOMAIN`: Backend API domain (e.g., `api.example.com`)
- `CERT_RESOLVER`: Traefik certificate resolver name (e.g., `letsencrypt`)

### Security

- `CSP_SCRIPT_SRC`: Content Security Policy script-src directive
- `CSP_STYLE_SRC`: Content Security Policy style-src directive

**⚠️ Production Security:**

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

#### 1. Nginx Permission Denied

**Error:** `nginx: [alert] could not open error log file: open() "/var/log/nginx/error.log" failed (13: Permission denied)`

**Cause:** Volume mount overwriting container permissions.

**Solution:** **Do NOT mount nginx log directories**. Use Docker logs instead:

```bash
docker logs <container-id> -f
# or for Docker Swarm:
docker service logs cvix_frontend -f
```

#### 2. Traefik Not Routing Traffic

**Symptoms:** 404 or "Service Unavailable" when accessing domains.

**Solutions:**

1. Verify Traefik labels are applied:
   ```bash
   docker service inspect cvix_frontend --pretty | grep -A 20 Labels
   ```

2. Verify `dokploy-network` exists and is attached:
   ```bash
   docker network ls | grep dokploy-network
   docker service inspect cvix_frontend --format '{{json .Spec.Networks}}' | jq
   ```

3. Check DNS resolution:
   ```bash
   nslookup app.example.com
   ```

#### 3. Backend Can't Connect to PostgreSQL

**Error:** `Connection refused` or `Unknown host: postgresql`

**Solutions:**

1. Wait for PostgreSQL healthcheck to pass:
   ```bash
   docker service ps cvix_postgresql
   ```

2. Verify network connectivity:
   ```bash
   docker exec -it $(docker ps -q -f name=cvix_backend) sh
   nslookup postgresql
   nc -zv postgresql 5432
   ```

3. Check database credentials match in `.env` and PostgreSQL service.

### Logs

View service logs:

#### Local Development

```bash
docker compose logs -f [service_name]
```

#### Production (Docker Swarm)

```bash
docker service logs cvix_[service_name] -f
```

## Monitoring

### Local Development

- **Grafana**: <http://grafana.localhost:3000>
- **Prometheus**: <http://prometheus.localhost:9090>
- **MailDev**: <http://maildev.localhost:1080>

### Production

- **Backend Health**: `https://api.example.com/actuator/health`
- **Frontend Health**: `https://app.example.com/` (should return 200)

## Additional Documentation

- [Docker Deployment Guide](DOCKER_DEPLOYMENT.md) - Standard Docker Compose deployment
- [Dokploy Deployment Guide](DOKPLOY_DEPLOYMENT.md) - Production deployment with Traefik (recommended)
- [Secrets Management](secrets/README.md) - How to manage secrets securely

## Production Readiness Checklist

Before deploying to production, ensure:

- [ ] All secrets created in Docker Swarm/Dokploy
- [ ] Environment variables configured in `.env` (no default/example values)
- [ ] Domain names configured and DNS pointing to server
- [ ] SSL certificates configured (Let's Encrypt via Traefik)
- [ ] CSP headers hardened (no `'unsafe-inline'`)
- [ ] CORS origins restricted to specific domains
- [ ] Database backups configured
- [ ] Monitoring and alerting set up
- [ ] Log aggregation configured
- [ ] Incident response procedures documented

See [DOKPLOY_DEPLOYMENT.md](DOKPLOY_DEPLOYMENT.md) for detailed production hardening checklist.

---

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
