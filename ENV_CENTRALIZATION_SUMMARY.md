# Environment Variables Centralization - Quick Reference

## 🎯 The Problem

Current mess of inconsistent environment variable names:
```
BASE_URL, BASE_WEBAPP_URL, PUBLIC_BASE_WEBAPP_URL_PROD, 
WEBAPP_URL, BACKEND_URL, BASE_API_URL, BLOG_URL, PUBLIC_BLOG_URL_PROD
```

**Issues:**
- 🚫 No clear naming convention
- 🚫 Unclear precedence (when does `CF_PAGES_URL` override?)
- 🚫 Different names for same concept across frameworks
- 🚫 No single source of truth

---

## ✅ The Solution

**New Semantic Convention:**
```bash
CVIX_MARKETING_URL    # Landing page (Astro)
CVIX_WEBAPP_URL       # Main app (Vue SPA)
CVIX_DOCS_URL         # Documentation
CVIX_BLOG_URL         # Blog
CVIX_API_URL          # Backend API
CVIX_OAUTH_URL        # Keycloak/OAuth
```

**Benefits:**
- ✅ Clear namespace (`CVIX_` prefix)
- ✅ Consistent naming (all end with `_URL`)
- ✅ Provider-aware (auto-detects Cloudflare, Vercel)
- ✅ Production overrides (`*_PROD` suffix)
- ✅ Backward compatible (during migration)

---

## 📋 Variable Priority Chain

For each URL, check in this order:

```typescript
1. Production override  → CVIX_MARKETING_URL_PROD (in production only)
2. Explicit env var     → CVIX_MARKETING_URL
3. Provider-specific    → CF_PAGES_URL (Cloudflare)
                          VERCEL_URL (Vercel)
4. Generic convention   → SITE_URL
5. Localhost fallback   → http://localhost:{PORT}
```

---

## 🔧 Quick Start

### 1. Update Your `.env` File

```bash
# New semantic names (use these)
CVIX_MARKETING_URL=http://localhost:7766
CVIX_WEBAPP_URL=http://localhost:9876
CVIX_DOCS_URL=http://localhost:4321
CVIX_BLOG_URL=http://localhost:7767
CVIX_API_URL=https://localhost:8443

# Production overrides (for Cloudflare Pages)
CVIX_MARKETING_URL_PROD=https://profiletailors.com
CVIX_WEBAPP_URL_PROD=https://app.profiletailors.com
CVIX_API_URL_PROD=https://api.profiletailors.com
```

### 2. Import in Your Code

```typescript
// ✅ New way (use this)
import { CVIX_WEBAPP_URL, CVIX_API_URL } from '@cvix/lib';

// ❌ Old way (deprecated, but still works during migration)
import { BASE_WEBAPP_URL, BASE_API_URL } from '@cvix/lib';
```

### 3. Use in Astro Components

```astro
---
import { WEBAPP_URL } from 'astro:env/client';
// Astro auto-maps: CVIX_WEBAPP_URL → WEBAPP_URL (via astro.config.mjs schema)
---

<a href={`${WEBAPP_URL}/login`}>Login</a>
```

---

## 🏭 Production Setup (Cloudflare Pages)

### Environment Variables to Set

```bash
# Required
CVIX_WEBAPP_URL_PROD=https://app.profiletailors.com
CVIX_API_URL_PROD=https://api.profiletailors.com

# Optional (if different from auto-detected CF_PAGES_URL)
CVIX_MARKETING_URL_PROD=https://profiletailors.com
```

### Variables to Remove (After Migration)

```bash
# Old variables - delete these once migration is complete
PUBLIC_BASE_WEBAPP_URL_PROD
PUBLIC_BLOG_URL_PROD
PUBLIC_BASE_DOCS_URL_PROD
```

---

## 🗺️ Migration Path

### Phase 1: Non-Breaking Addition (Current)
- ✅ New `CVIX_*` variables added to `config.ts`
- ✅ Old variables kept as deprecated aliases
- ✅ Everything works with both old and new names

### Phase 2: Update Consumption (In Progress)
- 🔄 Update imports to use new `CVIX_*` constants
- 🔄 Update Astro config env schema
- 🔄 Update component references
- 🔄 Update E2E test configs

### Phase 3: Production Deployment
- ⏳ Set new env vars in Cloudflare Pages
- ⏳ Deploy and test
- ⏳ Remove old env vars

### Phase 4: Remove Deprecated (v2.0)
- ⏳ Remove backward compatibility aliases
- ⏳ Breaking change release

---

## 📍 Key Files

| File | Purpose | Status |
|------|---------|--------|
| `client/packages/lib/src/consts/config.ts` | Central URL configuration | ✅ Ready |
| `.env.example` | Environment variable template | ⏳ Needs update |
| `client/apps/marketing/astro.config.mjs` | Astro env schema | ⏳ Needs update |
| `client/apps/webapp/vite.config.ts` | Vite configuration | ✅ OK (uses BACKEND_URL) |

---

## 🧪 Testing

```bash
# 1. Local development
pnpm dev
# Check: All services start on correct ports
# Check: Links between sites work

# 2. Build test
pnpm build
# Check: No build errors
# Check: URLs resolve correctly

# 3. E2E tests
pnpm test:e2e
# Check: All navigation works
# Check: API calls succeed
```

---

## 🆘 Troubleshooting

### Issue: "Cannot find CVIX_WEBAPP_URL"
**Solution:** Import from correct location:
```typescript
// Shared code
import { CVIX_WEBAPP_URL } from '@cvix/lib';

// Astro components
import { WEBAPP_URL } from 'astro:env/client';
```

### Issue: "Wrong URL in production"
**Solution:** Check Cloudflare Pages env vars are set:
```bash
CVIX_WEBAPP_URL_PROD=https://app.profiletailors.com  # Must be set!
```

### Issue: "TypeScript errors about import.meta.env"
**Solution:** Pre-existing issue, doesn't affect runtime. Will be fixed in separate PR.

---

## 📚 Full Documentation

See `ENV_CENTRALIZATION_PLAN.md` for:
- Complete implementation details
- Provider detection logic
- Migration strategy
- Code examples

---

**Status:** 🟢 Phase 1 Complete - Ready for Phase 2  
**Next Step:** Update consumption points (Astro config, components)
