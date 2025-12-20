# Docker Socket Proxy Security Guide

## Overview

The backend service requires access to the Docker socket (`/var/run/docker.sock`) to create ephemeral LaTeX containers for PDF generation. **Direct socket mounting grants root-level access to the host system**, which is a critical security vulnerability.

This guide explains how to use the **Docker Socket Proxy** to restrict Docker API access to only the operations required for PDF generation.

---

## The Security Risk

### Direct Socket Mounting (INSECURE)

```yaml
volumes:
  - /var/run/docker.sock:/var/run/docker.sock
```

**Consequences of compromise:**
- Attacker can create privileged containers
- Can mount host filesystem and read/write ANY file
- Can escalate to root on the host
- Complete host takeover

**Why this is dangerous:**
- Any vulnerability in the backend service (RCE, SSRF, etc.) = full host compromise
- Docker socket = root access to the host
- Can bypass all container security (AppArmor, seccomp, namespaces)

---

## The Solution: Docker Socket Proxy

The **Tecnativa Docker Socket Proxy** is a small service that:
1. Listens on TCP port 2375 inside the Docker network
2. Proxies requests to the real Docker socket
3. **Whitelists only specific API operations** via environment variables
4. Blocks all other operations

---

## Operations Required for PDF Generation

The backend service uses the Docker API to:

| Operation                      | API Endpoint                | Required |
|--------------------------------|-----------------------------|----------|
| Create LaTeX container         | `POST /containers/create`   | ✅        |
| Start container                | `POST /containers/{id}/start` | ✅        |
| Wait for container completion  | `POST /containers/{id}/wait`  | ✅        |
| Clean up container             | `DELETE /containers/{id}`     | ✅        |
| Check if texlive image exists  | `GET /images/{name}/json`     | ✅        |
| Pull texlive image (if missing) | `POST /images/create`         | ✅        |

### Operations **NOT** Allowed

- Building images
- Creating networks
- Mounting volumes (except ephemeral ones)
- Starting privileged containers
- Accessing secrets
- Modifying the Docker daemon

---

## Configuration

### Docker Socket Proxy Service

Located in `infra/docker-socket-proxy/docker-socket-proxy-compose.yml`:

```yaml
services:
  docker-socket-proxy:
    image: tecnativa/docker-socket-proxy:latest
    environment:
      # Whitelist only required operations
      POST: 1           # Required for creating/starting containers
      CONTAINERS: 1     # Required for container operations
      IMAGES: 1         # Required for checking/pulling images
      
      # Block everything else
      AUTH: 0
      SECRETS: 0
      BUILD: 0
      COMMIT: 0
      CONFIGS: 0
      DISTRIBUTION: 0
      EXEC: 0
      INFO: 0
      NETWORKS: 0
      NODES: 0
      PING: 0
      PLUGINS: 0
      SERVICES: 0
      SESSION: 0
      SWARM: 0
      SYSTEM: 0
      TASKS: 0
      VOLUMES: 0
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro  # Read-only mount
    networks:
      - backend
```

### Backend Service Configuration

```yaml
services:
  backend:
    environment:
      # Point to socket proxy instead of direct socket
      DOCKER_HOST: tcp://docker-socket-proxy:2375
    # Remove direct socket mount:
    # volumes:
    #   - /var/run/docker.sock:/var/run/docker.sock
```

---

## Deployment Instructions

### Local Development

For convenience, local development can still use direct socket mounting:

```bash
# In .env
DOCKER_HOST=unix:///var/run/docker.sock
```

```yaml
# In app.yml
volumes:
  - /var/run/docker.sock:/var/run/docker.sock
```

**⚠️ Only use this for local development on trusted machines.**

---

### Staging & Production

**Always use the Docker Socket Proxy:**

1. **Enable the socket proxy service:**

```yaml
# infra/app.yml
include:
  - ./docker-socket-proxy/docker-socket-proxy-compose.yml
```

2. **Configure backend to use proxy:**

```bash
# .env (staging/production)
DOCKER_HOST=tcp://docker-socket-proxy:2375
```

3. **Remove direct socket mount from backend service:**

Comment out or remove:
```yaml
# volumes:
#   - /var/run/docker.sock:/var/run/docker.sock
```

4. **Verify configuration:**

```bash
docker compose -f infra/app.yml config | grep -A5 docker-socket-proxy
docker compose -f infra/app.yml config | grep DOCKER_HOST
```

5. **Test PDF generation:**

```bash
# Create a test resume and verify PDF is generated
curl -X POST http://backend.localhost/api/v1/resumes/{id}/generate \
  -H "Authorization: Bearer $TOKEN"
```

---

## Monitoring & Troubleshooting

### Verify Socket Proxy is Running

```bash
docker compose -f infra/app.yml ps docker-socket-proxy
docker logs cvix-docker-socket-proxy-1
```

### Test Socket Proxy Access

```bash
# From within the backend container:
docker exec -it cvix-backend-1 sh
apk add curl
curl http://docker-socket-proxy:2375/containers/json
```

Expected: List of containers (or empty array)

### Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| `Cannot connect to Docker daemon` | Backend can't reach proxy | Check network connectivity, ensure proxy is running |
| `Permission denied` | Socket proxy blocking operation | Check whitelist flags in proxy config |
| `Connection refused` | DOCKER_HOST is wrong | Verify `DOCKER_HOST=tcp://docker-socket-proxy:2375` |

---

## Security Best Practices

1. **Never use direct socket mounting in production**
2. **Audit the whitelist** regularly—only enable what's needed
3. **Monitor logs** for unexpected Docker API calls
4. **Use read-only socket mount** in the proxy itself
5. **Restrict network access** to the proxy (only backend should access it)
6. **Consider additional layers:**
   - AppArmor/SELinux profiles on the backend container
   - Network policies to restrict backend egress
   - Regular security audits

---

## References

- [Tecnativa Docker Socket Proxy](https://github.com/Tecnativa/docker-socket-proxy)
- [Docker Socket Security](https://docs.docker.com/engine/security/protect-access/)
- [CIS Docker Benchmark](https://www.cisecurity.org/benchmark/docker)
