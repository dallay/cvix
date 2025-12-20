# nginx.conf Deprecation Plan

## Overview

The static `client/apps/webapp/nginx.conf` has been replaced by a template-based system (`nginx.conf.template` + `docker-entrypoint.sh`) that supports environment variable substitution for dynamic configuration.

## Timeline

| Date | Milestone |
|------|-----------|
| 2024-12-20 | Template system introduced |
| 2025-01-15 | Target: Validation complete in all environments |
| **2025-02-01** | **Scheduled removal date** |

## Validation Checklist

Before removing the deprecated file, ensure validation in ALL environments:

- [ ] **Local Development**
  - [ ] Template-based config works
  - [ ] Environment variables are substituted correctly
  - [ ] CSP headers include correct BACKEND_URL

- [ ] **CI/CD Pipeline**
  - [ ] Docker image builds successfully with new Dockerfile
  - [ ] All tests pass
  - [ ] No references to old nginx.conf in workflows

- [ ] **Staging Environment**
  - [ ] Application deploys successfully
  - [ ] Backend API connections work (CSP allows BACKEND_URL)
  - [ ] No CSP violations in browser console
  - [ ] PDF generation still works

- [ ] **Production Environment**
  - [ ] Hardened CSP deployed (no `'unsafe-inline'`)
  - [ ] All features functional for 7+ days
  - [ ] No incidents related to nginx configuration

## Migration Steps

### 1. Verify All Environments Use Template

```bash
# Check Dockerfile references
rg "nginx\.conf[^.]" client/apps/webapp/Dockerfile

# Should only show:
#   - nginx.conf.template (good)
#   - NOT nginx.conf (old)

# Check CI workflows
rg "nginx\.conf[^.]" .github/workflows/

# Should not find any hardcoded references
```

### 2. Create Removal PR

Once all checkboxes above are complete:

1. Create issue: "Remove deprecated nginx.conf after template migration validation"
2. Create PR that:
   - Deletes `client/apps/webapp/nginx.conf`
   - Updates this document to mark removal complete
   - References the tracking issue

### 3. Post-Removal Verification

After merging removal PR:

- [ ] CI/CD still passes
- [ ] No broken references in codebase
- [ ] Documentation updated to remove old file references

## Rollback Plan

If issues are discovered after removal:

1. **Immediate**: Revert the removal PR
2. **Root Cause**: Investigate what was still depending on old file
3. **Fix**: Update references to use template
4. **Re-attempt**: Reschedule removal after fix

## References

- Migration guide: `client/apps/webapp/MIGRATION_GUIDE.md`
- Template file: `client/apps/webapp/nginx.conf.template`
- Security headers: `client/apps/webapp/security-headers.conf.template`
- Entrypoint: `client/apps/webapp/docker-entrypoint.sh`

## Tracking Issue

**TODO**: Create GitHub issue and update link in nginx.conf header comment:

- Title: "Remove deprecated nginx.conf after template migration validation"
- Labels: `cleanup`, `breaking-change`
- Milestone: `2025.Q1`
- Link: <https://github.com/dallay/cvix/issues/XXX>

---

**Status**: 🟡 Waiting for validation in all environments

**Next Action**: Complete validation checklist above
