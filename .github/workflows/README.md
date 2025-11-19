# Workflows Configuration

This document explains how the CI/CD workflows are configured to work with GitHub branch protection rules.

## Problem Solved

GitHub branch protection rules require specific status checks to pass before merging. When workflows use `paths` filters to only run on relevant changes, they don't execute at all for unrelated changes, causing PRs to wait indefinitely for status checks that will never appear.

## Solution

All critical workflows now:

1. **Always execute** on all PRs and pushes to `main` (no `paths` filters on triggers)
2. **Detect changes** using the `dorny/paths-filter` action in a dedicated job
3. **Conditionally run** heavy jobs only when relevant changes are detected
4. **Provide dummy jobs** that successfully skip when no changes are detected

This ensures all required status checks always appear in GitHub, but actual work only happens when needed.

## Workflow Details

### Backend CI (`backend-ci.yml`)

**Required Status Checks:**

- `Lint with Detekt`
- `Build and Test`
- `Test Results` (published by `EnricoMi/publish-unit-test-result-action`)

**How it works:**

- `changes` job always runs and detects if backend files changed
- If backend changes detected → runs real `lint` and `build` jobs
- If no backend changes → runs `lint-skip` and `build-skip` jobs that immediately succeed
- The `Build and Test` job publishes test results as a separate "Test Results" status check

**Monitored paths:**

- `server/**`
- `shared/**`
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle/**`
- `.github/workflows/backend-ci.yml`
- `.github/actions/setup/java/**`

### Frontend CI (`frontend-ci.yml`)

**Required Status Checks:**

- `Lint with Biome`
- `Build`
- `Test`

**How it works:**

- `changes` job always runs and detects if frontend files changed
- If frontend changes detected → runs real `lint`, `build`, and `test` jobs
- If no frontend changes → runs `lint-skip`, `build-skip`, and `test-skip` jobs that immediately succeed

**Monitored paths:**

- `client/**`
- `package.json`
- `pnpm-lock.yaml`
- `pnpm-workspace.yaml`
- `.github/workflows/frontend-ci.yml`
- `.github/actions/setup/node/**`

### CodeQL (`codeql.yml`)

**Required Status Checks:**

- `Analyze (java-kotlin)`
- `Analyze (javascript-typescript)`

**How it works:**

- `changes` job detects backend and frontend changes
- `analyze-java` runs if:
  - It's a scheduled scan, OR
  - Backend files changed, OR
  - It's not a PR/push (manual trigger)
- `analyze-javascript` runs if:
  - It's a scheduled scan, OR
  - Frontend files changed, OR
  - It's not a PR/push (manual trigger)

Both jobs always appear as required status checks in GitHub, but the actual analysis only runs when triggered by a schedule, when relevant changes are detected, or on manual execution.

## Branch Protection Rules Alignment

The workflows are configured to satisfy these branch protection rules on `main`:

```json
{
  "required_status_checks": [
    { "context": "Build and Test" },
    { "context": "Lint with Detekt" },
    { "context": "Test Results" },
    { "context": "Analyze (java-kotlin)" },
    { "context": "Analyze (javascript-typescript)" }
  ]
}
```

**Status Check Mapping:**

- **Backend CI**:
  - `Lint with Detekt` → from the `lint` job
  - `Build and Test` → from the `build` job
  - `Test Results` → published by `EnricoMi/publish-unit-test-result-action` within the `build` job
- **Frontend CI**:
  - `Lint with Biome`, `Build`, `Test` → from their respective jobs
- **CodeQL**:
  - `Analyze (java-kotlin)` → from the `analyze-java` job
  - `Analyze (javascript-typescript)` → from the `analyze-javascript` job

**Note:** Frontend CI status checks (`Lint with Biome`, `Build`, `Test`) are not enforced by branch protection rules and are shown here for informational mapping only.

All these status checks will now **always appear** for every PR, either as:

- ✅ Passed (after running actual checks)
- ✅ Passed (skipped, no relevant changes)

## Benefits

1. ✅ **No more stuck PRs** waiting for workflows that never run
2. ✅ **Fast feedback** on PRs with no relevant changes
3. ✅ **Resource efficiency** - heavy jobs only run when needed
4. ✅ **Consistent status checks** - all required checks always present
5. ✅ **Branch protection compliance** - all rules satisfied

## How to Add New Required Status Checks

If you need to add a new required status check:

1. Add the job to the appropriate workflow
2. Create a corresponding `-skip` job with the same name
3. Condition the real job with `if: needs.changes.outputs.<area> == 'true'`
4. Condition the skip job with `if: needs.changes.outputs.<area> == 'false'`
5. Both jobs must have **identical names** so GitHub sees them as the same check

Example:

```yaml
jobs:
  changes:
    # ... outputs backend/frontend ...

  my-new-check:
    name: My New Check
    needs: changes
    if: needs.changes.outputs.backend == 'true'
    runs-on: ubuntu-latest
    steps:
      - run: echo "Running real check"

  my-new-check-skip:
    name: My New Check  # Same name!
    needs: changes
    if: needs.changes.outputs.backend == 'false'
    runs-on: ubuntu-latest
    steps:
      - run: echo "Skipping check"
```

## Testing

To test that status checks appear correctly:

1. Create a PR with only frontend changes
   - ✅ Backend checks should appear and pass quickly (skipped)
   - ✅ Frontend checks should appear and run actual tests

2. Create a PR with only backend changes
   - ✅ Frontend checks should appear and pass quickly (skipped)
   - ✅ Backend checks should appear and run actual tests

3. Create a PR with only documentation changes
   - ✅ All checks should appear and pass quickly (all skipped)

4. Create a PR with both frontend and backend changes
   - ✅ All checks should appear and run actual tests
