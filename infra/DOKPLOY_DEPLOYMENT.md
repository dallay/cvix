# Dokploy Deployment Guide

> Step-by-step guide for deploying the CVIX application stack on Dokploy with Docker Swarm.

## Prerequisites

- [ ] Dokploy instance running and accessible
- [ ] Domain names configured and pointing to your server (see [DNS Configuration](#dns-configuration-cloudflare) below)
- [ ] SSL certificates configured in Traefik (Let's Encrypt with Cloudflare DNS challenge)
- [ ] Docker Swarm initialized (`docker swarm init`)

---

## DNS Configuration (Cloudflare)

Before deploying, configure your DNS records in Cloudflare. Traefik uses Cloudflare's DNS challenge for SSL certificates, so DNS must be properly configured.

### Required DNS Records

| Type | Name | Content | Proxy Status | TTL |
|------|------|---------|--------------|-----|
| A | `app` | `<your-server-ip>` | DNS only (gray cloud) | Auto |
| A | `api` | `<your-server-ip>` | DNS only (gray cloud) | Auto |

**Example for `profiletailors.com`:**

| Type | Name | Content | Notes |
|------|------|---------|-------|
| A | `app` | `203.0.113.10` | Frontend: `app.profiletailors.com` |
| A | `api` | `203.0.113.10` | Backend: `api.profiletailors.com` |

### Important Notes

1. **Proxy Status: DNS Only (Gray Cloud)**
   - Traefik handles SSL termination, so Cloudflare proxy is not needed
   - Using "Proxied" (orange cloud) can cause SSL certificate issues with Let's Encrypt
   - You can enable proxy later after confirming Traefik is working

2. **Cloudflare API Token for DNS Challenge**
   - Traefik needs a Cloudflare API token to prove domain ownership for Let's Encrypt
   - Create token with `Zone:DNS:Edit` permissions for your zone
   - Set as environment variable: `CF_DNS_API_TOKEN`

3. **Verify DNS Propagation**
   ```bash
   # Check A records
   dig app.profiletailors.com +short
   dig api.profiletailors.com +short
   
   # Should return your server IP
   ```

### Traefik Cloudflare Configuration

The Dokploy Traefik configuration should already have Cloudflare DNS challenge enabled:

```yaml
# /etc/dokploy/traefik/traefik.yml (managed by Dokploy - DO NOT EDIT)
certificatesResolvers:
  letsencrypt:
    acme:
      email: your-email@example.com
      storage: /letsencrypt/acme.json
      dnsChallenge:
        provider: cloudflare
        resolvers:
          - "1.1.1.1:53"
          - "8.8.8.8:53"
```

Ensure the Cloudflare API token is available to Traefik (typically via Dokploy's environment configuration).

---

## Pre-Deployment Checklist

### 1. Create Docker Secrets

All sensitive values **MUST** be stored as Docker secrets. Create them via Dokploy UI (Settings → Secrets) or CLI:

```bash
# Client secret for OAuth2
echo -n "your-keycloak-client-secret" | docker secret create client_secret -

# Admin realm password
echo -n "your-admin-password" | docker secret create admin_realm_password -

# SendGrid API key (if using SendGrid)
echo -n "your-sendgrid-api-key" | docker secret create sendgrid_api_key -

# SMTP credentials
echo -n "your-smtp-username" | docker secret create smtp_username -
echo -n "your-smtp-password" | docker secret create smtp_password -

# SSL keystore password
echo -n "your-keystore-password" | docker secret create ssl_keystore_password -
```

**Verify secrets exist:**

```bash
docker secret ls
```

You should see all 6 secrets listed.

---

### 2. Configure Environment Variables

Create a `.env` file in the `infra/` directory with production values. **DO NOT commit this file.**

```bash
cp .env.example .env
```

**Critical variables to configure:**

| Variable                  | Example                            | Description                              |
|---------------------------|------------------------------------|------------------------------------------|
| `BACKEND_URL`             | `https://api.profiletailors.com`   | Backend API URL (must match Traefik domain) |
| `OAUTH2_SERVER_URL`       | `https://auth.example.com`         | Keycloak URL                             |
| `CORS_ALLOWED_ORIGINS`    | `https://app.profiletailors.com`   | Frontend origin for CORS                 |
| `DATABASE_URL`            | `postgresql://postgresql:5432/cvix` | PostgreSQL connection string             |
| `DATABASE_USERNAME`       | `postgres`                         | Database user                            |
| `DATABASE_PASSWORD`       | `strong-random-password`           | Database password                        |
| `POSTGRESQL_USER`         | `postgres`                         | PostgreSQL superuser                     |
| `POSTGRESQL_PASSWORD`     | `strong-random-password`           | PostgreSQL password                      |
| `POSTGRESQL_DB`           | `cvix`                             | Database name                            |
| `CSP_SCRIPT_SRC`          | `'self'`                           | CSP script-src (NO unsafe-inline in prod)|
| `CSP_STYLE_SRC`           | `'self'`                           | CSP style-src (NO unsafe-inline in prod) |
| `SPRING_PROFILES_ACTIVE`  | `prod`                             | Spring Boot profile                      |

> **Note:** Domain names (`app.profiletailors.com`, `api.profiletailors.com`) are hardcoded in `app-stack.yml` 
> Traefik labels because Docker Swarm does not interpolate environment variables in `deploy.labels`.

**Security Notes:**

- **NEVER use `'unsafe-inline'` in production CSP**. Generate proper hashes or nonces.
- Use **strong, randomly generated passwords** for all credentials.
- Ensure CORS origins are **specific domains**, never `*` in production.

---

### 3. Prepare Volumes and Directories

Dokploy will mount local directories for persistence. Create them with proper permissions:

```bash
# Navigate to infra directory
cd /path/to/cvix/infra

# Create required directories
mkdir -p postgresql/data
mkdir -p postgresql/init-scripts
mkdir -p data/latex
mkdir -p logs/backend
mkdir -p ssl

# Set proper permissions (adjust UIDs based on your container users)
# PostgreSQL container runs as UID 999 by default
sudo chown -R 999:999 postgresql/data

# Backend logs should be writable by backend container user
# Spring Boot default runs as user 1000 or CNB builder UID
sudo chown -R 1000:1000 logs/backend data/latex
```

**Note:** If using non-root containers, verify the UID/GID from the Dockerfile or image config.

---

## Deployment Steps

### 1. Deploy the Stack

From the `infra/` directory:

```bash
# Load environment variables
export $(cat .env | grep -v '^#' | xargs)

# Deploy the stack
docker stack deploy -c app-stack.yml cvix
```

**Expected output:**

```text
Creating network cvix_backend
Creating service cvix_postgresql
Creating service cvix_docker-socket-proxy
Creating service cvix_frontend
Creating service cvix_backend
```

---

### 2. Verify Stack Deployment

```bash
# List all services
docker stack services cvix

# Check service logs
docker service logs cvix_frontend -f
docker service logs cvix_backend -f
docker service logs cvix_postgresql -f
```

**Common issues and fixes:**

| Issue                                  | Cause                             | Fix                                       |
|----------------------------------------|-----------------------------------|-------------------------------------------|
| `secrets not found`                    | Secrets not created               | Create secrets using commands above       |
| `network not found: dokploy-network`   | Dokploy network missing           | Run `docker network create dokploy-network` |
| `nginx permission denied`              | Volume mount overwriting perms    | Don't mount logs directory for frontend   |
| `backend can't connect to PostgreSQL`  | DB not ready yet                  | Wait for healthcheck, check DB logs       |

---

### 3. Verify Traefik Routing

Check that Traefik recognized your services:

```bash
# List Traefik routers (if you have access to Traefik container)
docker exec <traefik-container> traefik show-routers

# Or check Traefik dashboard (if enabled)
# Navigate to: https://traefik.example.com/dashboard/
```

**Expected routers:**

- `cvix-frontend` → `app.profiletailors.com` (HTTP → redirect to HTTPS)
- `cvix-frontend-secure` → `app.profiletailors.com` (HTTPS with TLS)
- `cvix-backend` → `api.profiletailors.com` (HTTP → redirect to HTTPS)
- `cvix-backend-secure` → `api.profiletailors.com` (HTTPS with TLS)

---

### 4. Verify Health Checks

```bash
# Check service health
docker service ps cvix_frontend
docker service ps cvix_backend
docker service ps cvix_postgresql

# Test endpoints
curl -f https://app.profiletailors.com/
curl -f https://api.profiletailors.com/actuator/health
```

**Expected responses:**

- Frontend: HTML page with Vue app
- Backend health: `{"status":"UP"}`

---

## Post-Deployment Verification

### 1. SSL/TLS Certificate

Verify SSL certificate is valid:

```bash
# Check certificate
openssl s_client -connect app.profiletailors.com:443 -servername app.profiletailors.com < /dev/null | openssl x509 -noout -dates
```

### 2. Security Headers

Verify security headers are present:

```bash
curl -I https://app.profiletailors.com/
```

**Expected headers:**

```text
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
X-Frame-Options: SAMEORIGIN
X-Content-Type-Options: nosniff
```

### 3. Application Functionality

- [ ] Frontend loads successfully
- [ ] User can log in via Keycloak
- [ ] Backend API responds to authenticated requests
- [ ] Database queries work (check backend logs)
- [ ] PDF generation works (if applicable)

---

## Monitoring and Logs

### View Service Logs

```bash
# Frontend logs
docker service logs cvix_frontend -f

# Backend logs
docker service logs cvix_backend -f

# PostgreSQL logs
docker service logs cvix_postgresql -f
```

### Check Service Status

```bash
# List all services with status
docker stack services cvix

# Check specific service replicas
docker service ps cvix_backend
```

---

## Updating the Stack

To update environment variables or configuration:

```bash
# Edit .env file
nano .env

# Reload environment variables
export $(cat .env | grep -v '^#' | xargs)

# Redeploy stack (only updated services will be redeployed)
docker stack deploy -c app-stack.yml cvix
```

To update Docker images:

```bash
# Pull latest images
docker pull dallay/cvix-webapp:latest
docker pull dallay/cvix-engine:latest

# Force service update
docker service update --image dallay/cvix-webapp:latest cvix_frontend
docker service update --image dallay/cvix-engine:latest cvix_backend
```

---

## Rollback

If deployment fails, rollback to previous version:

```bash
# Rollback specific service
docker service rollback cvix_frontend
docker service rollback cvix_backend

# Or remove entire stack and redeploy previous version
docker stack rm cvix
docker stack deploy -c app-stack.yml cvix
```

---

## Troubleshooting

### Frontend nginx Permission Denied

**Problem:** `nginx: [alert] could not open error log file: open() "/var/log/nginx/error.log" failed (13: Permission denied)`

**Causes:**

1. **Volume mount overwriting container directories**: If you mount `/var/log/nginx` as a volume, it overwrites the permissions set in the Dockerfile.
2. **SELinux context mismatch** (on RHEL/CentOS): SELinux may block nginx from writing logs.

**Solutions:**

1. **Don't mount nginx log directories**. Nginx runs as non-root (UID 101) and writes logs internally. Use `docker logs` to view logs instead:
   ```bash
   docker service logs cvix_frontend -f
   ```

2. **If you MUST mount logs**, create the directory with correct permissions BEFORE deploying:
   ```bash
   mkdir -p /path/to/logs/nginx
   sudo chown -R 101:101 /path/to/logs/nginx
   sudo chmod -R 755 /path/to/logs/nginx
   ```
   
   Then add to `app-stack.yml`:
   ```yaml
   volumes:
     - /path/to/logs/nginx:/var/log/nginx
   ```

3. **SELinux fix** (if applicable):
   ```bash
   sudo chcon -Rt svirt_sandbox_file_t /path/to/logs/nginx
   ```

**Recommended approach:** **Do NOT mount nginx log directories**. Let nginx write logs to stdout/stderr, then view them via Docker logs.

---

### Backend Can't Connect to PostgreSQL

**Problem:** `Connection refused` or `Unknown host: postgresql`

**Causes:**

1. PostgreSQL service not ready yet
2. Network misconfiguration
3. Database credentials mismatch

**Solutions:**

1. **Wait for PostgreSQL healthcheck to pass:**
   ```bash
   docker service ps cvix_postgresql
   # Wait until CURRENT STATE shows "Running" and healthcheck is "healthy"
   ```

2. **Verify network connectivity:**
   ```bash
   # Exec into backend container
   docker exec -it $(docker ps -q -f name=cvix_backend) sh
   
   # Test DNS resolution
   nslookup postgresql
   
   # Test connection
   nc -zv postgresql 5432
   ```

3. **Check database credentials:**
   ```bash
   # Verify env vars in backend container
   docker exec $(docker ps -q -f name=cvix_backend) env | grep DATABASE
   ```

---

### Traefik Not Routing Traffic

**Problem:** 404 or "Service Unavailable" when accessing domains

**Causes:**

1. Traefik labels not applied correctly
2. `dokploy-network` not attached
3. Service not exposing correct port

**Solutions:**

1. **Verify labels are applied:**
   ```bash
   docker service inspect cvix_frontend --pretty | grep -A 20 Labels
   ```

2. **Verify network attachment:**
   ```bash
   docker service inspect cvix_frontend --format '{{json .Spec.Networks}}' | jq
   ```

3. **Check Traefik logs:**
   ```bash
   docker logs $(docker ps -q -f name=traefik) | grep cvix
   ```

4. **Verify DNS resolution:**
   ```bash
   nslookup app.example.com
   nslookup api.example.com
   ```

---

## Production Hardening Checklist

After successful deployment, harden for production:

- [ ] Remove `'unsafe-inline'` from CSP headers (use nonces or hashes)
- [ ] Enable Traefik rate limiting on backend API
- [ ] Configure PostgreSQL connection pooling (R2DBC pool settings)
- [ ] Set up automated database backups
- [ ] Configure log aggregation (ELK, Loki, or Dokploy built-in logs)
- [ ] Set up monitoring and alerting (Prometheus + Grafana)
- [ ] Enable Traefik access logs for audit trail
- [ ] Review and minimize CORS allowed origins
- [ ] Verify all secrets are externalized (no hardcoded values)
- [ ] Test backup and restore procedures
- [ ] Document incident response procedures

---

## Support

For issues specific to:

- **Dokploy**: Check [Dokploy documentation](https://docs.dokploy.com)
- **Traefik**: Check [Traefik documentation](https://doc.traefik.io/traefik/)
- **Application bugs**: Open an issue in the project repository
