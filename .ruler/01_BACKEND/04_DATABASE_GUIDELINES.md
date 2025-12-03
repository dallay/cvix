# Database Guidelines

> This document provides guidelines for database design, including Row-Level Security (RLS) and UUID strategy.

## UUID Strategy

- **Version**: Use UUID version 4 (randomly generated) for all primary keys.
- **Generation**: Prefer client-side UUID generation (offline first, e.g., using `crypto.randomUUID()` in browsers or equivalent in mobile/desktop). The application layer may generate UUIDs (e.g., `java.util.UUID.randomUUID()`) only in specific cases where client-side generation is not possible or not desirable (e.g., system jobs, migrations, legacy integrations).
- **Storage**: Store UUIDs in the database as a native `UUID` type in PostgreSQL. This is efficient for storage and indexing.
- **Usage**: Use UUIDs as the primary keys for all database tables and as the external identifiers in APIs.

## Row-Level Security (RLS)

RLS is a PostgreSQL feature used to enforce that queries automatically filter rows the current user is not allowed to see. This is a critical component of our multi-tenant security model.

### RLS Pattern

1. **Tenant Column**: Add a `tenant_id UUID NOT NULL` column to all tenant-scoped tables.
2. **Enable RLS**: Enable RLS on the table:

    ```sql
    ALTER TABLE my_table ENABLE ROW LEVEL SECURITY;
    ALTER TABLE my_table FORCE ROW LEVEL SECURITY;
    ```

3. **Create Policy**: Create a policy that checks the `tenant_id` against a session variable.

    ```sql
    CREATE POLICY tenant_isolation ON my_table
      USING (tenant_id = current_setting('app.current_tenant', true)::uuid)
      WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::uuid);
    ```

### Application Integration

- The application must set the `app.current_tenant` session variable immediately after acquiring a database connection and before executing any queries.
- Use `SET LOCAL` to ensure the setting only lasts for the duration of a transaction.

```sql
BEGIN;
SET LOCAL app.current_tenant = '<the-authenticated-tenant-uuid>';
-- Application queries go here
COMMIT;
```

### Performance

- **Indexing**: Always create an index on the columns used in RLS policies (e.g., `tenant_id`).

  ```sql
  CREATE INDEX IF NOT EXISTS idx_my_table_tenant_id ON my_table (tenant_id);
  ```

### Security Notes

- **`BYPASSRLS`**: Only grant this privilege to superuser/maintenance roles. Application roles must not have it.
- **Connection Pooling**: Ensure your connection pooler is configured to clean up session variables to prevent state leakage between different tenants.
