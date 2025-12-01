# cvix – Database Schema

This document defines the conventions, practices, and structure of the cvix project's database.

---

## 🧱 General Architecture

- **PostgreSQL** as the relational database engine.
- **Liquibase** for schema versioning (YAML format).
- **Keycloak** as the external authentication provider.
- Internal `users` table partially synced with Keycloak.
- Multi-tenancy managed through `workspaces`.

---

## 📁 Migration Structure

Migrations are organized in the `/migrations` directory and automatically included via
`master.yaml`:

```yaml
databaseChangeLog:
    -   includeAll:
            path: migrations
            relativeToChangelogFile: true
```

**File naming conventions:**

- Numeric prefixes for ordering (`001`, `002`, ...).
- Clear domain-based names: `001-initial-schema.yaml`, `002-workspaces.yaml`,
  `003-subscribers.yaml`, etc.
- Optional subversions: `002a-workspaces-rls.yaml`.

---

## 📐 Base Schema

### `users`

Local table representing users authenticated via Keycloak.

| Field      | Type         | Constraints             |
|------------|--------------|-------------------------|
| id         | uuid         | PK, NOT NULL            |
| email      | varchar(255) | UNIQUE, NOT NULL        |
| full_name  | varchar(255) |                         |
| created_at | timestamptz  | DEFAULT now(), NOT NULL |
| updated_at | timestamptz  |                         |

---

### `workspaces`

Unit of multi-tenant isolation.

| Field       | Type         | Constraints                |
|-------------|--------------|----------------------------|
| id          | uuid         | PK, NOT NULL               |
| name        | varchar(100) | NOT NULL                   |
| description | varchar(500) |                            |
| owner_id    | uuid         | FK → `users(id)`, NOT NULL |
| created_by  | varchar(50)  | DEFAULT 'system', NOT NULL |
| created_at  | timestamptz  | DEFAULT now(), NOT NULL    |
| updated_by  | varchar(50)  |                            |
| updated_at  | timestamptz  |                            |

---

### `workspace_members`

User–workspace relationship with role.

| Field        | Type        | Constraints                |
|--------------|-------------|----------------------------|
| workspace_id | uuid        | PK, FK → `workspaces(id)`  |
| user_id      | uuid        | PK, FK → `users(id)`       |
| role         | role_type   | DEFAULT 'EDITOR', NOT NULL |
| created_at   | timestamptz | DEFAULT now()              |
| updated_at   | timestamptz | DEFAULT now()              |

---

### `authority`

Global roles mapped from Keycloak or used for cross-workspace permissions.

| Field | Type        | Constraints  |
|-------|-------------|--------------|
| name  | varchar(50) | PK, NOT NULL |

---

### `user_authority`

Many-to-many relationship between users and global authorities.

| Field          | Type        | Constraints                |
|----------------|-------------|----------------------------|
| user_id        | uuid        | PK, FK → `users(id)`       |
| authority_name | varchar(50) | PK, FK → `authority(name)` |

---

### `subscribers`

Contacts managed per workspace.

| Field        | Type              | Constraints                       |
|--------------|-------------------|-----------------------------------|
| id           | uuid              | PK, NOT NULL                      |
| email        | varchar(320)      | UNIQUE (per workspace), NOT NULL  |
| firstname    | text              | NOT NULL                          |
| lastname     | text              |                                   |
| status       | subscriber_status | ENUM, NOT NULL (default: ENABLED) |
| attributes   | jsonb             |                                   |
| workspace_id | uuid              | FK → `workspaces(id)`, NOT NULL   |
| created_at   | timestamptz       | DEFAULT now()                     |
| updated_at   | timestamptz       | DEFAULT now()                     |

---

### `tags`

Custom labels to segment subscribers.

| Field        | Type        | Constraints                                                                         |
|--------------|-------------|-------------------------------------------------------------------------------------|
| id           | uuid        | PK, NOT NULL                                                                        |
| name         | text        | NOT NULL                                                                            |
| color        | text        | CHECK (`color IN ('default', 'purple', 'pink', 'red', 'blue', 'yellow')`), NOT NULL |
| workspace_id | uuid        | FK → `workspaces(id)`, NOT NULL                                                     |
| created_at   | timestamptz | DEFAULT now()                                                                       |
| updated_at   | timestamptz | DEFAULT now()                                                                       |
| deleted_at   | timestamptz |                                                                                     |

---

### `subscriber_tags`

M:N relation between `subscribers` and `tags`.

| Field         | Type        | Constraints                        |
|---------------|-------------|------------------------------------|
| subscriber_id | uuid        | FK → `subscribers(id)`, part of PK |
| tag_id        | uuid        | FK → `tags(id)`, part of PK        |
| created_at    | timestamptz | DEFAULT now()                      |
| updated_at    | timestamptz | DEFAULT now()                      |

---

### `forms`

Embeddable forms used to collect subscribers. Scoped per workspace.

| Field             | Type         | Constraints                     |
|-------------------|--------------|---------------------------------|
| id                | uuid         | PK, NOT NULL                    |
| name              | varchar(150) | NOT NULL                        |
| header            | text         |                                 |
| description       | text         |                                 |
| input_placeholder | varchar(100) |                                 |
| button_text       | varchar(50)  |                                 |
| button_color      | varchar(30)  |                                 |
| background_color  | varchar(30)  |                                 |
| text_color        | varchar(30)  |                                 |
| button_text_color | varchar(30)  |                                 |
| workspace_id      | uuid         | FK → `workspaces(id)`, NOT NULL |
| created_by        | varchar(50)  | DEFAULT 'system', NOT NULL      |
| created_at        | timestamptz  | DEFAULT now(), NOT NULL         |
| updated_by        | varchar(50)  |                                 |
| updated_at        | timestamptz  |                                 |

## 🔁 Enums

```sql
CREATE TYPE role_type AS ENUM ('OWNER', 'ADMIN', 'EDITOR', 'VIEWER');
CREATE TYPE subscriber_status AS ENUM ('ENABLED', 'DISABLED', 'BLOCKLISTED');
```

---

## ⚙️ Best Practices

- **UUIDs** as primary keys.
- **Triggers** to update `updated_at`.
- **Liquibase in YAML** for clear and auditable schema changes.
- **Decoupling from Keycloak** via internal `users` table.
- **Strict naming conventions** for files and constraints.
- **Multi-tenancy via `workspace_id`**, reinforced with RLS.
- **Row-level security policies (RLS)** controlled by
  `current_setting('cvix.current_workspace')`.
- **Hybrid permission model**: combines workspace-local roles (`workspace_members.role`) and global
  authorities (`user_authority`).
- **Global roles from Keycloak groups** are stored in `authority` and joined via `user_authority`.

---

## ✅ Implemented Migrations

1. `001-initial-schema.yaml`: enums and users table
2. `002-workspaces.yaml`: workspaces
3. `002a-workspaces-triggers.yaml`: triggers for workspaces
4. `002b-workspaces-rls.yaml`: RLS for workspaces
5. `003-subscribers.yaml`: subscriber entity
6. `003a-subscribers-triggers.yaml`: triggers for subscribers
7. `003b-subscribers-rls.yaml`: RLS for subscribers
8. `004-tags.yaml`: tag entity
9. `004a-subscriber-tags.yaml`: subscriber-tag relation
10. `004b-tags-triggers.yaml`: triggers for tags and subscriber_tags
11. `004c-tags-rls.yaml`: RLS for tags
12. `99900001-data-dev-test-users.yaml`: dev-only test users and roles
13. `005-forms.yaml`: forms table
14. `005a-forms-triggers.yaml`: trigger for `updated_at`
15. `005b-forms-rls.yaml`: RLS policy for forms

---

## 🧪 Useful Commands

### Apply all migrations

```bash
liquibase --changelog-file=master.yaml update
```

### Rollback (example)

```bash
liquibase --changelog-file=master.yaml rollbackCount 1
```

---

## 🧭 Versioning

- Semantic versioning: `MAJOR.MINOR.PATCH`.
- Each file defines a `changeSet` with an incremental `id`.
- Versioning is tracked by Liquibase's internal table.

---
