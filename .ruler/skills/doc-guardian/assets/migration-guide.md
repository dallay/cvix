---
title: Migration Guide - {Version X to Y}
description: Breaking changes and migration steps from version X to Y
last_updated: {YYYY-MM-DD}
---

# Migration Guide: {Version X} → {Version Y}

{Overview of major changes}

## Breaking Changes

### 1. {Change Name}

**What changed:**
{Description of the change}

**Before (v{X}):**
```typescript
// Old code
import { oldFunction } from "@cvix/package";

oldFunction({ param: "value" });
```

**After (v{Y}):**
```typescript
// New code
import { newFunction } from "@cvix/package";

newFunction({ newParam: "value" });
```

**Migration steps:**
1. Update import: `oldFunction` → `newFunction`
2. Rename parameter: `param` → `newParam`
3. Update types if necessary

**Automated migration:**
```bash
# Use codemod if available
pnpm dlx jscodeshift -t ./codemods/migrate-function.js src/**/*.ts
```

---

### 2. {Another Breaking Change}

{Repeat structure above}

---

## Deprecations

| Feature | Deprecated In | Removed In | Alternative |
|---------|--------------|------------|-------------|
| `oldAPI()` | v{X}.0 | v{Y}.0 | Use `newAPI()` |
| `legacyConfig` | v{X}.5 | v{Y+1}.0 | Use `modernConfig` |

## New Features

- **Feature 1**: {Description} - See [docs](link)
- **Feature 2**: {Description} - See [docs](link)

## Dependency Updates

| Package | Old Version | New Version | Notes |
|---------|-------------|-------------|-------|
| `vue` | 3.4 | 3.5 | See [Vue 3.5 release](link) |
| `vite` | 5.0 | 6.0 | Update `vite.config.ts` |

## Migration Checklist

- [ ] Update package versions in `package.json`
- [ ] Run `pnpm install`
- [ ] Update imports (see Breaking Change #1)
- [ ] Update configurations
- [ ] Run tests: `pnpm test`
- [ ] Run type checking: `pnpm check`
- [ ] Run linter: `pnpm lint`
- [ ] Update documentation
- [ ] Test in staging environment
- [ ] Deploy to production

## Step-by-Step Migration

### Step 1: Update Dependencies

```bash
# Update package versions
pnpm up @cvix/package@{Y}.0.0

# Install dependencies
pnpm install

# Verify installation
pnpm list @cvix/package
```

### Step 2: Code Changes

{Detailed steps for each breaking change}

### Step 3: Configuration Updates

**Before:**
```typescript
// old.config.ts
export default {
  oldOption: true
}
```

**After:**
```typescript
// new.config.ts
export default {
  newOption: { enabled: true }
}
```

### Step 4: Testing

```bash
# Run tests
pnpm test

# Check types
pnpm check

# Lint
pnpm lint
```

### Step 5: Verification

{How to verify the migration was successful}

## Known Issues

### Issue 1: {Description}

**Symptoms:**
- Error message: `{error}`
- When it happens: {scenario}

**Workaround:**
```typescript
// Temporary fix
```

**Tracking:** [Issue #123](link)

## Rollback Plan

If migration fails:

```bash
# Revert package versions
git checkout package.json pnpm-lock.yaml

# Reinstall old versions
pnpm install

# Revert code changes
git checkout {branch}
```

## Support

- **Questions?** [Discord](link) | [GitHub Discussions](link)
- **Bugs?** [GitHub Issues](link)
- **Migration help:** [Community Forum](link)

## Timeline

| Date | Action |
|------|--------|
| {YYYY-MM-DD} | v{Y} released |
| {YYYY-MM-DD} | Deprecation warnings added |
| {YYYY-MM-DD} | v{X} maintenance ends |

## Related

- [Changelog](../changelog.md)
- [API Reference](../api/reference.md)
- [Upgrade Guide](./upgrade-guide.md)
