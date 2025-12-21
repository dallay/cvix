# CVIX Dokploy Deployment Checklist

## Pre-Deployment Verification

### 1. File Changes
- [x] `app-stack.yml` - Added placement constraints to frontend and backend
- [x] Domains hardcoded (Swarm limitation with variable interpolation)
- [x] Middleware provider changed to `@swarm`

### 2. Environment Variables (Dokploy UI)
Verify these are set in Dokploy for the stack:

#### Database
- [ ] `DATABASE_URL` = `r2dbc:postgresql://postgresql:5432/cvix`
- [ ] `DATABASE_USERNAME` = `cvix`
- [ ] `DATABASE_PASSWORD` = (from .env)
- [ ] `POSTGRESQL_USER` = `cvix`
- [ ] `POSTGRESQL_PASSWORD` = (same as DATABASE_PASSWORD)
- [ ] `POSTGRESQL_DB` = `cvix`

#### Backend URLs
- [ ] `BACKEND_URL` = `https://api.profiletailors.com`
- [ ] `CORS_ALLOWED_ORIGINS` = `https://app.profiletailors.com`

#### Keycloak
- [ ] `OAUTH2_SERVER_URL` = `https://auth.profiletailors.com`
- [ ] `KEYCLOAK_URL` = `https://auth.profiletailors.com`
- [ ] `KC_HOSTNAME` = `auth.profiletailors.com`
- [ ] `REALM` = `cvix`
- [ ] `CLIENT_ID` = `cvix-backend`
- [ ] `ADMIN_CLIENT_ID` = `admin-cli`
- [ ] `ADMIN_REALM` = `master`
- [ ] `ADMIN_REALM_USERNAME` = `admin`

#### Other
- [ ] `SPRING_PROFILES_ACTIVE` = `prod`
- [ ] `HOSTNAME` = `api.profiletailors.com`

### 3. Docker Secrets (Dokploy UI → Settings → Secrets)
Verify these secrets exist:
- [ ] `client_secret`
- [ ] `admin_realm_password`
- [ ] `sendgrid_api_key`
- [ ] `smtp_username`
- [ ] `smtp_password`
- [ ] `ssl_keystore_password`
- [ ] `waitlist_ip_hmac_secret`

### 4. Traefik Configuration
- [x] Cloudflare API token set (`CF_DNS_API_TOKEN` env var in Traefik container)
- [x] SSL certificates generated for both domains (verified in `acme.json`)

---

## Deployment Steps

### Step 1: Push Changes to Git
```bash
cd /Users/acosta/Dev/cvix
git add infra/app-stack.yml
git commit -m "fix: force frontend and backend to run on manager node

- Add placement constraints to ensure services run on fenix-icloud
- Workaround for Docker Swarm overlay networking issues over Tailscale VPN
- Traefik and application services now on same node (local communication)"
git push
```

### Step 2: Deploy via Dokploy

**Option A: Via Dokploy UI** (Recommended)
1. Open Dokploy dashboard
2. Navigate to your stack (`cvixtest-pbesb0`)
3. Click **Redeploy** or **Update Stack**
4. Wait for deployment to complete

#### Option B: Via SSH to Manager Node

```bash
ssh fenix-icloud
cd /path/to/cvix/infra
export $(cat .env | grep -v '^#' | xargs)
docker stack deploy -c app-stack.yml cvixtest-pbesb0
```

### Step 3: Verify Service Migration
```bash
# SSH into manager node
ssh fenix-icloud

# Check service placement
docker service ps cvixtest-pbesb0_frontend --format "table {{.Name}}\t{{.Node}}\t{{.CurrentState}}"
docker service ps cvixtest-pbesb0_backend --format "table {{.Name}}\t{{.Node}}\t{{.CurrentState}}"

# Expected output: Both should show NODE as "fenix-icloud"
```

### Step 4: Monitor Deployment Logs
```bash
# Frontend logs
docker service logs -f cvixtest-pbesb0_frontend --tail 50

# Backend logs
docker service logs -f cvixtest-pbesb0_backend --tail 50

# Look for:
# - No connection errors
# - Healthcheck passing
# - Application started successfully
```

### Step 5: Test Connectivity from Traefik
```bash
# Get frontend container IP (should be reachable now since same node)
FRONTEND_IP=$(docker service inspect cvixtest-pbesb0_frontend \
  --format='{{range .Endpoint.VirtualIPs}}{{.Addr}}{{end}}' | cut -d'/' -f1)

BACKEND_IP=$(docker service inspect cvixtest-pbesb0_backend \
  --format='{{range .Endpoint.VirtualIPs}}{{.Addr}}{{end}}' | cut -d'/' -f1)

# Test from Traefik container
docker exec dokploy-traefik wget -qO- http://$FRONTEND_IP:8080 | head -20
docker exec dokploy-traefik wget -qO- http://$BACKEND_IP:8080/actuator/health
```

---

## Post-Deployment Testing

### Test 1: Frontend HTTPS Access
```bash
curl -I https://app.profiletailors.com/
# Expected: HTTP/2 200 OK
# Should NOT return 504 Gateway Timeout
```

### Test 2: Backend API Health
```bash
curl -s https://api.profiletailors.com/actuator/health | jq
# Expected: {"status": "UP", ...}
```

### Test 3: SSL Certificate Validation
```bash
curl -vI https://app.profiletailors.com 2>&1 | grep -A3 "Server certificate"
# Expected: subject: CN=app.profiletailors.com
#           issuer: C=US; O=Let's Encrypt; CN=R12
```

### Test 4: Browser Access
1. Open browser (incognito mode to avoid cache)
2. Navigate to `https://app.profiletailors.com`
3. **Expected**: Vue.js app loads successfully
4. Check browser console for errors

### Test 5: Check Traefik Dashboard
```bash
# Get Traefik routers status
docker exec dokploy-traefik wget -qO- http://localhost:8080/api/http/routers \
  | jq '.[] | select(.name | contains("cvix")) | {name, status, rule, service}'

# Expected: All routers show "status": "enabled"
```

---

## Rollback Plan (If Needed)

If deployment fails:

### Option 1: Previous Stack Config
```bash
# Revert git changes
git revert HEAD
docker stack deploy -c app-stack.yml cvixtest-pbesb0
```

### Option 2: Remove Placement Constraints
Edit `app-stack.yml` and remove these sections:
```yaml
    deploy:
      placement:
        constraints:
          - node.hostname == fenix-icloud
```

---

## Success Criteria

- [ ] Frontend accessible at `https://app.profiletailors.com` (200 OK)
- [ ] Backend accessible at `https://api.profiletailors.com/actuator/health` (200 OK)
- [ ] No 504 Gateway Timeout errors
- [ ] Both services running on `fenix-icloud` node
- [ ] SSL certificates valid and auto-renewing
- [ ] No errors in Traefik logs related to CVIX services

---

## Known Issues & Future Work

### Current Workaround
All services forced to run on manager node (`fenix-icloud`) due to Docker Swarm overlay networking issues over Tailscale VPN.

### Future Investigation (Create GitHub Issue)
**Title**: "Docker Swarm Overlay Networking with Tailscale VPN"

**Problem**: Multi-node Swarm cluster with Tailscale interconnect doesn't route overlay network traffic correctly between nodes.

**Investigation Needed**:
- [ ] Verify Docker Swarm ports (TCP 2377, TCP/UDP 7946, UDP 4789) over Tailscale
- [ ] Check if `--advertise-addr` flag needed when joining via Tailscale IPs
- [ ] Test overlay networks with explicit `--subnet` configuration
- [ ] Review Tailscale firewall rules for VXLAN (UDP 4789) traffic
- [ ] Consider alternative: Direct WireGuard or IPsec tunnel between nodes

**Current Impact**: Limits horizontal scaling across worker nodes.

---

## Contact & Support

If you encounter issues:
1. Check Traefik logs: `docker logs dokploy-traefik --tail 100`
2. Check service logs: `docker service logs cvixtest-pbesb0_backend --tail 100`
3. Verify DNS propagation: `dig app.profiletailors.com +short`
4. Test from manager node directly: `curl http://localhost:<port>/`

---

## Deployment Timestamp
- **Date**: (Fill in after deployment)
- **Version**: `2.0.1-beta`
- **Stack Name**: `cvixtest-pbesb0`
- **Deployment Method**: Dokploy UI / Manual
