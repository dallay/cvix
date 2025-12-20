# Migration Guide: Static to Dynamic nginx Configuration

This guide helps you migrate from the old hardcoded `nginx.conf` to the new template-based system with environment variable substitution.

---

## What Changed?

### Before (Hardcoded)

```nginx
# nginx.conf
add_header Content-Security-Policy "connect-src 'self' https://backend.localhost";
```

**Problems:**
- Hardcoded backend URL breaks in staging/production
- Duplicate headers across location blocks
- `'unsafe-inline'` weakens XSS protection

### After (Dynamic)

```nginx
# nginx.conf.template + security-headers.conf.template
add_header Content-Security-Policy "connect-src 'self' ${BACKEND_URL}";
```

**Benefits:**
- Works in all environments (dev, staging, production)
- Single source of truth for security headers
- Easy to harden CSP in production

---

## Migration Steps

### 1. Update Your Dockerfile

If you're building custom images, ensure your Dockerfile includes:

```dockerfile
# Install gettext for envsubst
RUN apk add --no-cache curl gettext

# Copy template files
COPY client/apps/webapp/nginx.conf.template /etc/nginx/nginx.conf.template
COPY client/apps/webapp/security-headers.conf.template /etc/nginx/conf.d/security-headers.conf.template

# Copy entrypoint script
COPY client/apps/webapp/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# Set environment variables with defaults
ENV BACKEND_URL=https://backend.localhost \
    CSP_SCRIPT_SRC="'self' 'unsafe-inline'" \
    CSP_STYLE_SRC="'self' 'unsafe-inline'"

# Use entrypoint to substitute variables
ENTRYPOINT ["/docker-entrypoint.sh"]
```

### 2. Update docker-compose.yml / app.yml

Add environment variables to your frontend service:

```yaml
services:
  frontend:
    environment:
      # Nginx CSP configuration
      BACKEND_URL: ${BACKEND_URL}
      CSP_SCRIPT_SRC: ${CSP_SCRIPT_SRC:-'self' 'unsafe-inline'}
      CSP_STYLE_SRC: ${CSP_STYLE_SRC:-'self' 'unsafe-inline'}
```

### 3. Update .env File

Add the new variables:

```bash
# Content Security Policy
BACKEND_URL=https://backend.localhost  # Change for staging/production
CSP_SCRIPT_SRC='self' 'unsafe-inline'  # Remove 'unsafe-inline' in production
CSP_STYLE_SRC='self' 'unsafe-inline'   # Remove 'unsafe-inline' in production
```

### 4. Rebuild and Test

```bash
# Rebuild the image
docker compose build frontend

# Start the service
docker compose up -d frontend

# Test that nginx config is generated correctly
docker exec cvix-frontend-1 cat /etc/nginx/nginx.conf | grep connect-src

# Expected output:
# connect-src 'self' https://backend.localhost
```

### 5. Verify in Browser

1. Open DevTools → Network tab
2. Load the app
3. Check Response Headers for `Content-Security-Policy`
4. Verify `connect-src` includes your actual backend URL

---

## Environment-Specific Configuration

### Local Development

```bash
BACKEND_URL=https://backend.localhost
CSP_SCRIPT_SRC='self' 'unsafe-inline'
CSP_STYLE_SRC='self' 'unsafe-inline'
```

### Staging

```bash
BACKEND_URL=https://api.staging.example.com
CSP_SCRIPT_SRC='self' 'unsafe-inline'  # TODO: Remove 'unsafe-inline'
CSP_STYLE_SRC='self' 'unsafe-inline'   # TODO: Remove 'unsafe-inline'
```

### Production

```bash
BACKEND_URL=https://api.example.com
CSP_SCRIPT_SRC='self' 'sha256-abc...' 'sha256-def...'  # No 'unsafe-inline'
CSP_STYLE_SRC='self' 'sha256-ghi...'                   # No 'unsafe-inline'
```

See `CSP_HARDENING.md` for instructions on generating hashes or using nonces.

---

## Troubleshooting

### "Cannot connect to backend"

**Symptom:** Browser console shows CSP violation like:

```text
Refused to connect to 'https://api.example.com' because it violates CSP 'connect-src'
```

**Cause:** `BACKEND_URL` is not set or incorrect.

**Fix:**
```bash
# Check current value
docker compose exec frontend env | grep BACKEND_URL

# Update .env and restart
docker compose up -d frontend
```

### "nginx: [emerg] unknown directive"

**Symptom:** Container crashes on startup with nginx syntax error.

**Cause:** `envsubst` failed or template file is invalid.

**Fix:**
```bash
# Check generated config
docker compose exec frontend cat /etc/nginx/nginx.conf

# Look for un-substituted variables like ${BACKEND_URL}
# Ensure environment variables are set in docker-compose.yml
```

### "Inline script blocked by CSP"

**Symptom:** Browser console shows:

```text
Refused to execute inline script because it violates CSP 'script-src'
```

**Cause:** `'unsafe-inline'` was removed but inline scripts still exist.

**Fix:** Follow `CSP_HARDENING.md` to either:
1. Generate hashes for inline scripts
2. Use nonces
3. Extract all inline code to external files

---

## Rollback Plan

If you need to rollback to the old static configuration:

1. **Restore old Dockerfile:**
   ```dockerfile
   COPY client/apps/webapp/nginx.conf /etc/nginx/nginx.conf
   CMD ["nginx", "-g", "daemon off;"]
   ```

2. **Remove environment variables** from docker-compose.yml

3. **Rebuild:**
   ```bash
   docker compose build frontend
   docker compose up -d frontend
   ```

---

## Next Steps

1. ✅ Migrate to template-based configuration
2. ✅ Test in local environment
3. ✅ Deploy to staging
4. 🔲 Remove `'unsafe-inline'` (see `CSP_HARDENING.md`)
5. 🔲 Deploy to production
6. 🔲 Remove deprecated `nginx.conf` file

---

## Questions?

- CSP Hardening: `client/apps/webapp/CSP_HARDENING.md`
- Security Overview: `SECURITY_HARDENING.md`
- Docker Socket Security: `infra/docker-socket-proxy/DOCKER_SOCKET_SECURITY.md`
