# Database Guidelines

> Guidelines for database design, including UUID strategy, Row-Level Security (RLS), and Liquibase
> migrations.

## UUID Strategy

| Aspect         | Guideline                                                                                                                                                                 |
|----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Version**    | Use UUID version 4 (randomly generated) for all primary keys                                                                                                              |
| **Generation** | Prefer client-side UUID generation (offline first, e.g., `crypto.randomUUID()` in browsers). Server-side generation only for system jobs, migrations, legacy integrations |
| **Storage**    | Store as native `UUID` type in PostgreSQL (efficient for storage and indexing)                                                                                            |
| **Usage**      | Use UUIDs as primary keys for all tables and as external identifiers in APIs                                                                                              |

---

## Row-Level Security (RLS)

RLS is a PostgreSQL feature used to enforce that queries automatically filter rows the current user
is not allowed to see. This is a **critical component** of our multi-tenant security model.

### RLS Pattern

1. **Tenant Column**: Add a `tenant_id UUID NOT NULL` column to all tenant-scoped tables

2. **Enable RLS**: Enable RLS on the table:

    ```sql
    ALTER TABLE my_table ENABLE ROW LEVEL SECURITY;
    ALTER TABLE my_table FORCE ROW LEVEL SECURITY;
    ```

3. **Create Policy**: Create a policy that checks the `tenant_id` against a session variable:

```sql
CREATE POLICY tenant_isolation ON my_table
    USING (tenant_id = current_setting('app.current_tenant', true)::uuid)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::uuid);
```

### Application Integration

- Set `app.current_tenant` session variable **immediately after acquiring a database connection**
  and **before executing any queries**
- Use `SET LOCAL` to ensure the setting only lasts for the duration of a transaction:

```sql
BEGIN;
SET LOCAL app.current_tenant = '<the-authenticated-tenant-uuid>';
-- Application queries go here
COMMIT;
```

### Performance

- **Always create an index** on columns used in RLS policies (e.g., `tenant_id`):

```sql
CREATE INDEX IF NOT EXISTS idx_my_table_tenant_id ON my_table (tenant_id);
```

### Security Notes

- **`BYPASSRLS`**: Only grant this privilege to superuser/maintenance roles. Application roles must
  **not** have it.
- **Connection Pooling**: Ensure your connection pooler is configured to clean up session variables
  to prevent state leakage between different tenants.

---

## Database Migrations (Liquibase)

### Directory Structure

```text
📁db
 └── 📁changelog
   ├── 📁data
   │   ├── authority.csv
   │   ├── federated_identities_dev.csv
   │   ├── user_authority_dev.csv
   │   ├── users_dev.csv
   │   ├── workspace_members_dev.csv
   │   └── workspaces_dev.csv
   ├── 📁migrations
   │   ├── 001-initial-schema.yaml
   │   ├── 002-workspaces.yaml
   │   ├── 002a-workspaces-triggers.yaml
   │   ├── 002b-workspaces-rls.yaml
   │   ├── 002c-workspaces-default-constraint.yaml
   │   ├── 002d-sessions-table.yaml
   │   ├── 002e-authentication-events-table.yaml
   │   ├── 002f-federated-identities-table.yaml
   │   ├── 003-session-optimization.yaml
   │   ├── 004-resumes.yaml
   │   └── 99900001-data-dev-test-users.yaml
   ├── master.yaml
   └── README.md
```

### Migration Guidelines

| Rule              | Description                                                                          |
|-------------------|--------------------------------------------------------------------------------------|
| **Master file**   | `master.yaml` includes all changes and data in execution order                       |
| **Naming**        | Organize by number and topic, use suffixes for variants (triggers, rls, constraints) |
| **Dev/Test data** | Located in `changelog/data/`, loaded only in non-production environments             |
| **Immutability**  | **Never modify** a migration file that has already been applied in production        |
| **Format**        | Use YAML files for migrations, CSV files for bulk data                               |
| **Documentation** | Document each relevant migration in the `README.md` inside `changelog`               |
| **Testing**       | Test migrations on a clean database and in staging before production                 |
| **Atomicity**     | Keep changes small, atomic, and use descriptive names                                |

### Best Practices

- Each migration should be self-contained and reversible when possible
- Use meaningful changeset IDs that describe the change
- Include `author` and `id` attributes in every changeset
- Add `preconditions` to prevent duplicate executions
- Use `rollback` blocks for critical changes
