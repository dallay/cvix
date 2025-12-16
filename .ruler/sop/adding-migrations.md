# SOP: Adding Database Migrations

> Step-by-step procedure for creating and applying Liquibase database migrations.

## Prerequisites

- Ensure you have access to the development database
- Understand the schema change you need to make
- Review existing migrations for naming conventions

---

## Procedure

### 1. Determine Migration Type

| Type               | Naming Pattern                   | Example                                  |
|--------------------|----------------------------------|------------------------------------------|
| New feature schema | `NNN-feature-name.yaml`          | `005-user-preferences.yaml`              |
| Triggers           | `NNNa-feature-triggers.yaml`     | `005a-user-preferences-triggers.yaml`    |
| RLS policies       | `NNNb-feature-rls.yaml`          | `005b-user-preferences-rls.yaml`         |
| Constraints        | `NNNc-feature-constraints.yaml`  | `005c-user-preferences-constraints.yaml` |
| Dev/Test data      | `999NNNNN-data-description.yaml` | `99900002-data-dev-preferences.yaml`     |

### 2. Create Migration File

Location: `server/engine/src/main/resources/db/changelog/migrations/`

```yaml
databaseChangeLog:
  - changeSet:
      id: "005-user-preferences-create-table"
      author: "your-name"
      changes:
        - createTable:
            tableName: user_preferences
            columns:
              - column:
                  name: id
                  type: uuid
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: user_id
                  type: uuid
                  constraints:
                    nullable: false
                    foreignKeyName: fk_user_preferences_user
                    references: users(id)
              - column:
                  name: theme
                  type: varchar(50)
                  defaultValue: 'light'
              - column:
                  name: created_at
                  type: timestamp with time zone
                  defaultValueComputed: CURRENT_TIMESTAMP
              - column:
                  name: updated_at
                  type: timestamp with time zone
                  defaultValueComputed: CURRENT_TIMESTAMP
```

### 3. Add RLS Policy (If Tenant-Scoped)

Create a separate file: `005b-user-preferences-rls.yaml`

```yaml
databaseChangeLog:
  - changeSet:
      id: "005b-user-preferences-enable-rls"
      author: "your-name"
      changes:
        - sql:
            sql: |
              ALTER TABLE user_preferences ENABLE ROW LEVEL SECURITY;
              ALTER TABLE user_preferences FORCE ROW LEVEL SECURITY;

              CREATE POLICY tenant_isolation ON user_preferences
                USING (user_id IN (
                  SELECT id FROM users
                  WHERE tenant_id = current_setting('app.current_tenant', true)::uuid
                ))
                WITH CHECK (user_id IN (
                  SELECT id FROM users
                  WHERE tenant_id = current_setting('app.current_tenant', true)::uuid
                ));
```

### 4. Add Index for RLS Performance

```yaml
  - changeSet:
      id: "005-user-preferences-index-user-id"
      author: "your-name"
      changes:
        - createIndex:
            tableName: user_preferences
            indexName: idx_user_preferences_user_id
            columns:
              - column:
                  name: user_id
```

### 5. Update Master Changelog

Edit `server/engine/src/main/resources/db/changelog/master.yaml`:

```yaml
databaseChangeLog:
  # ... existing includes ...
  - include:
      file: migrations/005-user-preferences.yaml
      relativeToChangelogFile: true
  - include:
      file: migrations/005b-user-preferences-rls.yaml
      relativeToChangelogFile: true
```

### 6. Test Migration

```bash
# Run migrations locally
./gradlew :server:engine:bootRun

# Or run Liquibase directly
./gradlew :server:engine:liquibaseUpdate

# Verify in database
psql -h localhost -U postgres -d cvix -c "\d user_preferences"
```

### 7. Create Rollback (If Complex)

For complex changes, include rollback instructions:

```yaml
  - changeSet:
      id: "005-user-preferences-create-table"
      author: "your-name"
      changes:
        - createTable:
            # ... table definition ...
      rollback:
        - dropTable:
            tableName: user_preferences
```

---

## Checklist

- [ ] Migration file follows naming convention
- [ ] Changeset has unique `id` and `author`
- [ ] UUID used for primary keys
- [ ] Appropriate indexes created
- [ ] RLS policy added (if tenant-scoped)
- [ ] Index on RLS policy columns
- [ ] Added to `master.yaml` in correct order
- [ ] Tested on clean database
- [ ] Tested on database with existing data
- [ ] Rollback defined (if applicable)
- [ ] Documented in `changelog/README.md` (if significant)

---

## Common Pitfalls

| Issue                     | Solution                                           |
|---------------------------|----------------------------------------------------|
| Migration already applied | Never modify applied migrations; create a new one  |
| Missing RLS               | Always add RLS for tenant-scoped tables            |
| Poor index coverage       | Index columns used in WHERE clauses and JOINs      |
| Large data migrations     | Split into smaller changesets with proper batching |
