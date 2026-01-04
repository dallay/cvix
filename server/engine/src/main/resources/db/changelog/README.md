# Database Changelog

This directory contains all Liquibase database migration files for the cvix application.

## Structure

- `master.yaml` - Master changelog file that includes all migrations via `includeAll`
- `migrations/` - Individual migration files, executed in alphabetical order
- `data/` - CSV files containing seed/test data for development environments

## Migration Naming Convention

| Pattern                 | Purpose                           | Example                             |
|-------------------------|-----------------------------------|-------------------------------------|
| `NNN-feature-name.yaml` | Main schema changes               | `004-resumes.yaml`                  |
| `NNNa-feature-*.yaml`   | Triggers for the feature          | `004a-resumes-triggers.yaml`        |
| `NNNb-feature-*.yaml`   | Row-Level Security (RLS) policies | `004b-resumes-rls.yaml`             |
| `NNNc-feature-*.yaml`   | Additional constraints            | `004c-resumes-constraints.yaml`     |
| `999NNNNN-data-*.yaml`  | Development/test data             | `99900001-data-dev-test-users.yaml` |

## Migration History

### Core Schema Migrations

| Migration | Description                                    | Date       |
|-----------|------------------------------------------------|------------|
| 001       | Initial schema (users, authorities)            | 2024-10-15 |
| 002       | Workspaces and multi-tenancy                   | 2024-10-20 |
| 002a-f    | Workspace triggers, RLS, sessions, auth events | 2024-10-21 |
| 003       | Session optimization                           | 2024-10-25 |
| 004       | Resume documents (JSONB storage)               | 2024-11-01 |
| 004b      | Fix project date arrays → ISO strings          | 2026-01-03 |

### Data Migrations

| Migration | Description            | Environment |
|-----------|------------------------|-------------|
| 99900001  | Development test users | dev/test    |

## Special Notes

### Migration 004b: Project Date Format Fix

**Problem**: Legacy resume data stored project dates as JSON arrays `[YYYY, M, D]` instead of ISO
8601 strings `"YYYY-MM-DD"`.

**Solution**: Migration converts all array-format dates in `resumes.data->'projects'` to ISO 8601
string format.

**Affected Fields**:

- `projects[].startDate`
- `projects[].endDate`

**Format Conversion**:

```text
Before: [2018, 9, 5]
After:  "2018-09-05"
```

This ensures compliance with the JSON Resume Schema specification, which requires ISO 8601 date
strings.

## Adding New Migrations

1. Create a new YAML file in `migrations/` following the naming convention
2. Ensure the file has a unique changeset `id` and `author`
3. Use `includeAll` in `master.yaml` - new files will be auto-discovered
4. Test on a clean database before committing
5. Document significant migrations in this README

## Rollback Strategy

- Simple migrations: Include rollback SQL in the changeset
- Data transformations: Document manual rollback procedures
- Breaking changes: Coordinate with deployment team

## Best Practices

- Keep migrations small and focused
- Use UUIDs for all primary keys
- Add indexes for RLS policy columns
- Test rollback scenarios when possible
- Never modify applied migrations in production
