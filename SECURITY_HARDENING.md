# Security Hardening Summary

This document outlines the security improvements implemented to address production deployment concerns.

---

## Overview of Changes

Three critical security issues were identified and resolved:

1. **Hardcoded backend URL in CSP headers** - Breaks staging/production deployments
2. **`'unsafe-inline'` CSP directives** - Weakens XSS protection
3. **Direct Docker socket access** - Enables host-level compromise

---

## 1. Dynamic CSP Configuration with Environment Variables

### Problem

The nginx configuration for the webapp hardcoded `https://backend.localhost` in the `Content-Security-Policy` header, making it incompatible with non-local environments. Additionally, `'unsafe-inline'` was used for `script-src` and `style-src`, weakening XSS protections.

### Solution

**Converted nginx.conf to a template-based system:**

- Created `nginx.conf.template` with environment variable placeholders
- Extracted security headers into a shared snippet (`security-headers.conf.template`)
- Added `docker-entrypoint.sh` to substitute variables at container startup using `envsubst`

**Environment Variables:**

```bash
BACKEND_URL=https://api.example.com          # Backend API endpoint
CSP_SCRIPT_SRC='self' 'sha256-...'           # Script CSP directive
CSP_STYLE_SRC='self' 'sha256-...'            # Style CSP directive
```

**Files Changed:**

- `client/apps/webapp/nginx.conf` → `nginx.conf.template`
- Added `client/apps/webapp/security-headers.conf.template`
- Added `client/apps/webapp/docker-entrypoint.sh`
- Updated `client/apps/webapp/Dockerfile` to use entrypoint
- Updated `infra/app.yml` to expose CSP environment variables
- Updated `infra/.env.example` with CSP configuration

**Documentation:**

- `client/apps/webapp/CSP_HARDENING.md` - Complete guide on removing `'unsafe-inline'` in production

---

## 2. Removed Duplicate Security Headers

### Problem

Security headers were duplicated across parent and nested location blocks, making them error-prone to maintain in sync.

### Solution

Extracted all security headers into a shared snippet file (`security-headers.conf.template`) that is included in both locations using nginx's `include` directive.

**Benefits:**

- Single source of truth for security headers
- Easier to maintain and update
- No risk of headers falling out of sync

---

## 3. Docker Socket Proxy for PDF Generation

### Problem

The backend service mounted `/var/run/docker.sock` directly to create ephemeral LaTeX containers for PDF generation. This grants **root-level access to the host** and is a critical security vulnerability if the container is compromised.

### Solution

**Implemented Tecnativa Docker Socket Proxy:**

- Added `infra/docker-socket-proxy/docker-socket-proxy-compose.yml`
- Proxy whitelists ONLY required Docker API operations:
  - `POST /containers/create` - Create LaTeX containers
  - `POST /containers/{id}/start` - Start containers
  - `POST /containers/{id}/wait` - Wait for completion
  - `DELETE /containers/{id}` - Cleanup
  - `GET /images/*` - Check if texlive image exists

**Configuration Changes:**

```yaml
# Backend service now connects via proxy
environment:
  DOCKER_HOST: tcp://docker-socket-proxy:2375

# Direct socket mount removed (for production)
# volumes:
#   - /var/run/docker.sock:/var/run/docker.sock
```

**Local Development:**

For convenience, local development can still use direct socket mounting by setting:

```bash
DOCKER_HOST=unix:///var/run/docker.sock
```

And uncommenting the volume mount in `app.yml`.

**⚠️ NEVER deploy to staging/production with direct socket mounting enabled.**

**Files Changed:**

- Created `infra/docker-socket-proxy/docker-socket-proxy-compose.yml`
- Updated `infra/app.yml` to include socket proxy and configure `DOCKER_HOST`
- Updated `infra/.env.example` with `DOCKER_HOST` variable
- Added prominent security warnings in volume mount comments

**Documentation:**

- `infra/docker-socket-proxy/DOCKER_SOCKET_SECURITY.md` - Comprehensive security guide

---

## Deployment Checklist

### Staging & Production

- [ ] Set `BACKEND_URL` to actual backend API URL
- [ ] Remove `'unsafe-inline'` from `CSP_SCRIPT_SRC` and `CSP_STYLE_SRC`
  - Use nonce-based CSP, hash-based CSP, or extract inline code (see `CSP_HARDENING.md`)
- [ ] Set `DOCKER_HOST=tcp://docker-socket-proxy:2375`
- [ ] Ensure `docker-socket-proxy` service is included in compose stack
- [ ] Remove or comment out direct Docker socket mount from backend service
- [ ] Test PDF generation functionality
- [ ] Monitor logs for CSP violations in browser DevTools

### Testing

```bash
# Validate all compose files
cd infra
APP_NAME=cvix docker compose -f app.yml config --quiet

# Check for hardcoded localhost references
rg "localhost" infra/app.yml client/apps/webapp/nginx.conf.template

# Verify environment variables are set
docker compose -f app.yml config | grep -E "(BACKEND_URL|CSP_SCRIPT_SRC|DOCKER_HOST)"
```

---

## Security Improvements Summary

| Issue | Before | After | Impact |
|-------|--------|-------|--------|
| **Backend URL** | Hardcoded `backend.localhost` | Dynamic via `BACKEND_URL` env var | ✅ Works in all environments |
| **CSP Inline Code** | `'unsafe-inline'` allowed | Template-based, removable in prod | ✅ Stronger XSS protection |
| **Docker Socket** | Direct mount (root access) | Restricted proxy with whitelist | ✅ Prevents host compromise |
| **Header Duplication** | Duplicated in multiple blocks | Shared snippet with `include` | ✅ Easier to maintain |

---

## References

- CSP Hardening Guide: `client/apps/webapp/CSP_HARDENING.md`
- Docker Socket Security: `infra/docker-socket-proxy/DOCKER_SOCKET_SECURITY.md`
- Environment Variables: `infra/.env.example`
- Nginx Templates: `client/apps/webapp/*.template`

---

## Additional Recommendations

1. **Implement CSP Reporting**
   - Add `report-uri` directive to CSP header
   - Monitor for violations in production

2. **Regular Security Audits**
   - Run `docker scan` on all images
   - Use tools like `hadolint` for Dockerfile linting
   - Enable Dependabot for dependency updates

3. **Network Segmentation**
   - Restrict socket proxy access to backend network only
   - Use network policies if running in Kubernetes

4. **AppArmor/SELinux Profiles**
   - Add mandatory access control for backend container
   - Restrict syscalls and filesystem access

5. **Secret Management**
   - Never commit secrets to version control
   - Use Docker secrets or external secret managers (Vault, AWS Secrets Manager)
   - Rotate secrets regularly
