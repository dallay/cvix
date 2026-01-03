# Environment Variable Centralization Plan

## 🎯 Executive Summary

**Problem:** Environment variables are scattered, inconsistent, and have unclear precedence across the monorepo.

**Solution:** Centralize all URLs and configuration in `@cvix/lib/consts/config.ts` with semantic naming, clear precedence, and provider-aware defaults.

---

## 📊 Current State Analysis

### Current Variables (Inconsistent Naming)

| Current Name                  | Purpose                | Used By                          | Issues                                   |
|-------------------------------|------------------------|----------------------------------|------------------------------------------|
| `BASE_URL`                    | Landing page URL       | Astro marketing, router          | Generic, conflicts with Vite's BASE_URL  |
| `BASE_WEBAPP_URL`             | Main app URL           | Astro config, links              | Unclear "base" prefix                    |
| `PUBLIC_BASE_WEBAPP_URL_PROD` | Production webapp URL  | config.ts fallback               | Verbose, inconsistent with others        |
| `BASE_API_URL`                | Backend API URL        | config.ts                        | OK naming                                |
| `BASE_DOCS_URL`               | Documentation URL      | config.ts                        | OK naming                                |
| `BLOG_URL`                    | Blog URL               | config.ts                        | Inconsistent (no "BASE_" prefix)         |
| `PUBLIC_BLOG_URL_PROD`        | Production blog URL    | config.ts fallback               | Verbose                                  |
| `WEBAPP_URL`                  | App URL (Astro env)    | Astro components                 | Shorter but inconsistent                 |
| `BACKEND_URL`                 | Backend (Astro env)    | Astro config                     | Different from BASE_API_URL              |

### Environment Variable Sources

```typescript
// Current getEnv() checks multiple sources:
1. process.env[key]                    // Node.js
2. import.meta.env[key]                // Vite/Astro direct
3. import.meta.env[`PUBLIC_${key}`]    // Astro public vars
4. import.meta.env[`VITE_${key}`]      // Legacy Vite prefix
```

### Provider-Specific Variables

- `CF_PAGES_URL` (Cloudflare Pages) - auto-set by Cloudflare
- `VERCEL_URL` (Vercel) - auto-set by Vercel
- `SITE_URL` (Generic SSG convention)

---

## 🎨 Proposed Solution

### New Semantic Naming Convention

```typescript
// Application URLs - Clear, semantic, consistent
CVIX_MARKETING_URL     // Landing/marketing site (Astro)
CVIX_WEBAPP_URL        // Main Vue.js SPA
CVIX_DOCS_URL          // Documentation site
CVIX_BLOG_URL          // Blog site
CVIX_API_URL           // Backend REST API

// Internal/Build-time only
CVIX_OAUTH_URL         // Keycloak/OAuth server
```

**Naming Rationale:**
- ✅ **Prefixed with `CVIX_`** - Clear project namespace, avoids conflicts
- ✅ **No "BASE_" prefix** - Redundant, every URL is a "base"
- ✅ **Consistent `_URL` suffix** - All URLs end with `_URL`
- ✅ **Service-first naming** - What service, not implementation detail

### Environment Priority Chain

```typescript
// For each URL, check in this order:
1. Explicit env var (CVIX_MARKETING_URL)
2. Provider-specific (CF_PAGES_URL, VERCEL_URL) 
3. Generic convention (SITE_URL)
4. Localhost development fallback

// Production overrides (highest priority)
CVIX_MARKETING_URL_PROD
CVIX_WEBAPP_URL_PROD
// ... etc
```

### Configuration Structure

```typescript
// client/packages/lib/src/consts/config.ts

// ============================================================================
// ENVIRONMENT DETECTION
// ============================================================================

/**
 * Detect deployment provider from environment
 */
type DeploymentProvider = 'cloudflare' | 'vercel' | 'netlify' | 'local';

function detectProvider(): DeploymentProvider {
  if (typeof process !== 'undefined' && process.env) {
    if (process.env.CF_PAGES) return 'cloudflare';
    if (process.env.VERCEL) return 'vercel';
    if (process.env.NETLIFY) return 'netlify';
  }
  return 'local';
}

const DEPLOYMENT_PROVIDER = detectProvider();
const IS_PRODUCTION = getEnv('NODE_ENV') === 'production';

// ============================================================================
// ENVIRONMENT VARIABLE ACCESS
// ============================================================================

/**
 * Access environment variables across Node.js, Vite, and Astro
 * Checks multiple sources in order of precedence
 */
function getEnv(key: string): string | undefined {
  // Node.js environment
  if (typeof process !== 'undefined' && process.env?.[key]) {
    return process.env[key];
  }

  // Vite/Astro import.meta.env
  if (typeof import.meta !== 'undefined' && import.meta.env) {
    // Direct key
    if (import.meta.env[key]) return import.meta.env[key];
    
    // Astro public prefix (PUBLIC_*)
    const publicKey = `PUBLIC_${key}`;
    if (import.meta.env[publicKey]) return import.meta.env[publicKey];
    
    // Legacy Vite prefix (VITE_*)
    const viteKey = `VITE_${key}`;
    if (import.meta.env[viteKey]) return import.meta.env[viteKey];
  }

  return undefined;
}

// ============================================================================
// URL RESOLUTION FUNCTIONS
// ============================================================================

/**
 * Resolve URL with intelligent defaults based on provider and environment
 */
function resolveUrl(config: {
  envKey: string;              // Primary env var name (CVIX_MARKETING_URL)
  prodEnvKey?: string;         // Production override (CVIX_MARKETING_URL_PROD)
  providerDefaults?: {         // Provider-specific auto-detection
    cloudflare?: string;       // CF_PAGES_URL
    vercel?: string;           // VERCEL_URL
  };
  genericDefault?: string;     // Generic convention (SITE_URL)
  localPort?: number;          // Localhost port for development
}): string {
  const { envKey, prodEnvKey, providerDefaults, genericDefault, localPort } = config;

  // 1. Production override (highest priority in production)
  if (IS_PRODUCTION && prodEnvKey) {
    const prodUrl = getEnv(prodEnvKey);
    if (prodUrl) return prodUrl;
  }

  // 2. Explicit environment variable
  const explicitUrl = getEnv(envKey);
  if (explicitUrl) return explicitUrl;

  // 3. Provider-specific auto-detection
  if (providerDefaults) {
    switch (DEPLOYMENT_PROVIDER) {
      case 'cloudflare':
        const cfUrl = providerDefaults.cloudflare 
          ? getEnv(providerDefaults.cloudflare)
          : getEnv('CF_PAGES_URL');
        if (cfUrl) return cfUrl;
        break;
      
      case 'vercel':
        const vercelUrl = providerDefaults.vercel
          ? getEnv(providerDefaults.vercel)
          : getEnv('VERCEL_URL');
        if (vercelUrl) return vercelUrl ? `https://${vercelUrl}` : '';
        break;
    }
  }

  // 4. Generic convention
  if (genericDefault) {
    const genericUrl = getEnv(genericDefault);
    if (genericUrl) return genericUrl;
  }

  // 5. Localhost development fallback
  if (localPort) {
    return `http://localhost:${localPort}`;
  }

  // 6. Last resort: empty string (should never happen)
  console.warn(`⚠️  No URL found for ${envKey}`);
  return '';
}

// ============================================================================
// PORTS (Development)
// ============================================================================

export const PORTS = {
  MARKETING: 7766,
  WEBAPP: 9876,
  DOCS: 4321,
  BLOG: 7767,
  API: 8443,
} as const;

// ============================================================================
// APPLICATION URLs
// ============================================================================

/**
 * Marketing/Landing page URL
 * - Production: CVIX_MARKETING_URL_PROD or CVIX_MARKETING_URL
 * - Cloudflare: CF_PAGES_URL
 * - Local: http://localhost:7766
 */
export const CVIX_MARKETING_URL = resolveUrl({
  envKey: 'CVIX_MARKETING_URL',
  prodEnvKey: 'CVIX_MARKETING_URL_PROD',
  providerDefaults: { cloudflare: 'CF_PAGES_URL' },
  genericDefault: 'SITE_URL',
  localPort: PORTS.MARKETING,
});

/**
 * Main web application URL (Vue.js SPA)
 * - Production: CVIX_WEBAPP_URL_PROD or CVIX_WEBAPP_URL
 * - Local: http://localhost:9876
 */
export const CVIX_WEBAPP_URL = resolveUrl({
  envKey: 'CVIX_WEBAPP_URL',
  prodEnvKey: 'CVIX_WEBAPP_URL_PROD',
  localPort: PORTS.WEBAPP,
});

/**
 * Documentation site URL
 * - Production: CVIX_DOCS_URL_PROD or CVIX_DOCS_URL
 * - Local: http://localhost:4321
 */
export const CVIX_DOCS_URL = resolveUrl({
  envKey: 'CVIX_DOCS_URL',
  prodEnvKey: 'CVIX_DOCS_URL_PROD',
  localPort: PORTS.DOCS,
});

/**
 * Blog site URL
 * - Production: CVIX_BLOG_URL_PROD or CVIX_BLOG_URL
 * - Local: http://localhost:7767
 */
export const CVIX_BLOG_URL = resolveUrl({
  envKey: 'CVIX_BLOG_URL',
  prodEnvKey: 'CVIX_BLOG_URL_PROD',
  localPort: PORTS.BLOG,
});

/**
 * Backend REST API URL
 * - Production: CVIX_API_URL_PROD or CVIX_API_URL
 * - Local: https://localhost:8443 (HTTPS for OAuth)
 */
export const CVIX_API_URL = resolveUrl({
  envKey: 'CVIX_API_URL',
  prodEnvKey: 'CVIX_API_URL_PROD',
}) || `https://localhost:${PORTS.API}`;

/**
 * OAuth/Keycloak server URL
 * - Production: CVIX_OAUTH_URL or OAUTH2_SERVER_URL
 * - Local: http://localhost:9080
 */
export const CVIX_OAUTH_URL = 
  getEnv('CVIX_OAUTH_URL') || 
  getEnv('OAUTH2_SERVER_URL') || 
  'http://localhost:9080';

// ============================================================================
// BACKWARD COMPATIBILITY (Deprecated - Remove in v2.0)
// ============================================================================

/**
 * @deprecated Use CVIX_MARKETING_URL instead
 */
export const BASE_URL = CVIX_MARKETING_URL;

/**
 * @deprecated Use CVIX_WEBAPP_URL instead
 */
export const BASE_WEBAPP_URL = CVIX_WEBAPP_URL;

/**
 * @deprecated Use CVIX_API_URL instead
 */
export const BASE_API_URL = CVIX_API_URL;

/**
 * @deprecated Use CVIX_DOCS_URL instead
 */
export const BASE_DOCS_URL = CVIX_DOCS_URL;

/**
 * @deprecated Use CVIX_BLOG_URL instead
 */
export const BLOG_URL = CVIX_BLOG_URL;

// ============================================================================
// BRAND CONSTANTS (unchanged)
// ============================================================================

export const BRAND_NAME = "ProFileTailors";
export const SITE_TITLE = "ProFileTailors";
export const X_ACCOUNT = "@yacosta738";
```

---

## 📝 Migration Strategy

### Phase 1: Add New Variables (Non-Breaking)

1. ✅ Add new `CVIX_*` constants to `config.ts` (DONE ABOVE)
2. ✅ Keep old constants as deprecated aliases
3. ✅ Update `.env.example` with new names + comments
4. ✅ Update documentation

### Phase 2: Update Consumption Points

1. **Astro config** (`client/apps/marketing/astro.config.mjs`)
   - Change `BASE_URL` → `CVIX_MARKETING_URL`
   - Change `BASE_WEBAPP_URL` → `CVIX_WEBAPP_URL`
   - Update `env.schema` to use new names

2. **Vite config** (`client/apps/webapp/vite.config.ts`)
   - Already uses `BACKEND_URL` from env (no change needed)

3. **Components/Pages**
   - Search for imports of old constants
   - Replace with new `CVIX_*` names

4. **E2E Tests**
   - Update Playwright configs to use new env vars

### Phase 3: Update Cloudflare/Deploy Configs

1. **Cloudflare Pages Environment Variables:**
   ```
   CVIX_WEBAPP_URL_PROD=https://app.profiletailors.com
   CVIX_API_URL_PROD=https://api.profiletailors.com
   ```

2. **Remove old variables after testing:**
   ```
   PUBLIC_BASE_WEBAPP_URL_PROD (delete)
   ```

### Phase 4: Remove Deprecated (v2.0 Breaking Change)

1. Remove backward compatibility aliases
2. Update CHANGELOG with breaking changes
3. Create migration guide

---

## 🔧 Implementation Checklist

- [ ] **Step 1:** Update `config.ts` with new implementation
- [ ] **Step 2:** Update `.env.example` with new variable names
- [ ] **Step 3:** Update Astro config to use new constants
- [ ] **Step 4:** Update all imports in Astro components
- [ ] **Step 5:** Update Vite webapp if needed
- [ ] **Step 6:** Update E2E test configs
- [ ] **Step 7:** Test locally (all apps start correctly)
- [ ] **Step 8:** Update Cloudflare Pages env vars
- [ ] **Step 9:** Deploy and test production
- [ ] **Step 10:** Document in README and wiki

---

## 🧪 Testing Strategy

### Local Testing
```bash
# 1. Clear any cached builds
pnpm clean

# 2. Start all services
pnpm dev

# 3. Verify URLs in browser console
# Marketing: http://localhost:7766
# Webapp: http://localhost:9876
# Check links between sites work correctly
```

### Production Testing
```bash
# 1. Set Cloudflare env vars
CVIX_MARKETING_URL_PROD=https://profiletailors.com
CVIX_WEBAPP_URL_PROD=https://app.profiletailors.com
CVIX_API_URL_PROD=https://api.profiletailors.com

# 2. Build and deploy
pnpm build

# 3. Test on preview URL first
# 4. Promote to production
# 5. Run smoke tests (login, 404 redirects, API calls)
```

---

## 🚀 Benefits

1. **Semantic Clarity:** Variable names describe what they are, not how they're used
2. **Namespace Protection:** `CVIX_` prefix avoids conflicts with framework defaults
3. **Provider Awareness:** Auto-detects Cloudflare, Vercel, Netlify
4. **Clear Precedence:** Documented priority order eliminates confusion
5. **Single Source of Truth:** All URL logic in one place
6. **Backward Compatible:** Gradual migration without breaking existing code
7. **TypeScript Friendly:** Proper typing, no more `@ts-expect-error`
8. **Production Ready:** Separate prod overrides for security

---

## 📚 Documentation Updates Needed

1. **README.md** - Add "Environment Variables" section
2. **.env.example** - Update with new names + inline docs
3. **DEPLOYMENT.md** - Document Cloudflare setup
4. **CONTRIBUTING.md** - Add env var naming conventions
5. **Wiki** - Create "Configuration Guide" page

---

## 🎯 Next Actions

1. **Get approval** on naming convention and structure
2. **Implement** new `config.ts` (code ready above)
3. **Update** `.env.example` with new variables
4. **Migrate** consumption points incrementally
5. **Test** thoroughly in local and preview environments
6. **Deploy** to production with new env vars set

---

## ❓ Questions for Decision

1. **Breaking Change Timeline:** Do we want v1.x backward compatibility, or go straight to v2.0?
2. **Provider Support:** Should we support Netlify, Railway, or other providers?
3. **Validation:** Should we add runtime validation (throw error if required URL is missing)?
4. **TypeScript:** Create a separate `env.d.ts` for type definitions?

---

**Status:** 🟡 Awaiting Approval  
**Next:** Implement Step 1 - Update config.ts
