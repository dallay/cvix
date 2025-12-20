# Content Security Policy (CSP) Hardening Guide

## Overview

The webapp nginx configuration uses **Content Security Policy (CSP)** headers to mitigate XSS attacks. By default, the local development configuration allows `'unsafe-inline'` for scripts and styles for convenience. **This MUST be removed in production.**

---

## Current State (Development)

```bash
CSP_SCRIPT_SRC='self' 'unsafe-inline'
CSP_STYLE_SRC='self' 'unsafe-inline'
```

- Allows inline `<script>` and `<style>` tags
- **Vulnerable to XSS** if user input is improperly sanitized

---

## Production Hardening Options

### Option 1: Nonce-Based CSP (Recommended)

Generate a unique nonce (number used once) for each request and include it in both the CSP header and inline script/style tags.

#### Implementation Steps

1. **Generate nonce server-side** (in nginx or application layer)
2. **Inject nonce into HTML** at runtime
3. **Update CSP header** to include the nonce

#### Example with nginx + lua (requires OpenResty)

```nginx
location / {
    access_by_lua_block {
        local nonce = ngx.encode_base64(ngx.var.request_id)
        ngx.var.csp_nonce = nonce
    }
    
    sub_filter_once off;
    sub_filter '<script>' '<script nonce="${csp_nonce}">';
    
    add_header Content-Security-Policy "script-src 'self' 'nonce-${csp_nonce}'; style-src 'self' 'nonce-${csp_nonce}'";
}
```

---

### Option 2: Hash-Based CSP (Simpler for Static Sites)

Precompute SHA-256 hashes of all inline scripts and styles, then whitelist them in the CSP header.

#### Implementation Steps

1. **Build the application** and extract all inline scripts/styles
2. **Compute SHA-256 hashes** for each inline block
3. **Add hashes to CSP header**

#### Example

If your `index.html` has:

```html
<script>console.log('Hello World');</script>
```

Compute the hash:

```bash
echo -n "console.log('Hello World');" | openssl dgst -sha256 -binary | base64
# Output: sha256-abc123def456...
```

Update `CSP_SCRIPT_SRC`:

```bash
CSP_SCRIPT_SRC="'self' 'sha256-abc123def456...'"
```

---

### Option 3: Extract All Inline Code (Most Secure)

Move all inline scripts and styles into external `.js` and `.css` files.

#### Benefits
- No need for nonces or hashes
- Cleaner separation of concerns
- Easier to cache

#### Example

Before:
```html
<script>
  const app = { name: 'cvix' };
  console.log(app.name);
</script>
```

After:
```html
<script src="/assets/app-init.js"></script>
```

```javascript
// /assets/app-init.js
const app = { name: 'cvix' };
console.log(app.name);
```

Update CSP:
```bash
CSP_SCRIPT_SRC='self'
CSP_STYLE_SRC='self'
```

---

## Deployment Checklist

- [ ] Audit all inline scripts and styles in production build
- [ ] Choose hardening method (nonce, hash, or extraction)
- [ ] Update `CSP_SCRIPT_SRC` and `CSP_STYLE_SRC` environment variables
- [ ] Test in staging with browser DevTools Console (look for CSP violations)
- [ ] Deploy to production
- [ ] Monitor CSP violation reports (use `report-uri` directive)

---

## Testing CSP

### Browser DevTools

1. Open DevTools → Console
2. Look for CSP violation warnings:

   ```text
   Refused to execute inline script because it violates CSP directive...
   ```
3. Fix violations by updating hashes or extracting code

### Automated Testing with Playwright

```typescript
test('should not have CSP violations', async ({ page }) => {
  const violations: string[] = [];
  
  page.on('console', (msg) => {
    if (msg.text().includes('Content Security Policy')) {
      violations.push(msg.text());
    }
  });
  
  await page.goto('/');
  expect(violations).toHaveLength(0);
});
```

---

## Production Environment Variables

Example for production (after hash generation):

```bash
# Production CSP configuration
BACKEND_URL=https://api.production.com
CSP_SCRIPT_SRC="'self' 'sha256-abc123...' 'sha256-def456...'"
CSP_STYLE_SRC="'self' 'sha256-ghi789...'"
```

---

## References

- [MDN: Content Security Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP)
- [CSP Evaluator (Google)](https://csp-evaluator.withgoogle.com/)
- [Report URI (CSP Reporting)](https://report-uri.com/)
