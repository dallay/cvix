# Phase 1 — Data Model

Date: 2025-11-16

## Domain Entities (JSON Resume aligned)

- Resume
  - id: UUID (client-generated or server-generated)
  - basics: Basics
  - work: Work[]
  - education: Education[]
  - skills: Skill[]
  - projects: Project[]
  - languages: Language[]
  - certificates: Certificate[]
  - publications?: Publication[]
  - awards?: Award[]
  - volunteer?: Volunteer[]
  - references?: Reference[]

- Basics
  - name: string
  - label?: string
  - image?: string (URL)
  - email?: string
  - phone?: string (E.164 normalized)
  - url?: string (personal site)
  - summary?: string
  - location?: Location
  - profiles?: Profile[]

- Location
  - address?: string
  - city?: string
  - region?: string
  - countryCode?: string (ISO 3166-1 alpha-2)
  - postalCode?: string

- Profile
  - network?: string
  - username?: string
  - url?: string

- Work
  - name?: string (company)
  - position?: string
  - url?: string
  - startDate?: string (YYYY-MM-DD)
  - endDate?: string (YYYY-MM-DD)
  - summary?: string
  - highlights?: string[]

- Education
  - institution?: string
  - url?: string
  - area?: string
  - studyType?: string
  - startDate?: string (YYYY-MM-DD)
  - endDate?: string (YYYY-MM-DD)
  - gpa?: string
  - courses?: string[]

- Skill
  - name: string
  - level?: string
  - keywords?: string[]

- Project
  - name: string
  - description?: string
  - url?: string
  - keywords?: string[]
  - roles?: string[]
  - startDate?: string (YYYY-MM-DD)
  - endDate?: string (YYYY-MM-DD)

- Language
  - language: string
  - fluency?: string

- Certificate
  - name: string
  - issuer?: string
  - date?: string (YYYY-MM-DD)
  - url?: string

- Publication
  - name: string
  - publisher?: string
  - releaseDate?: string (YYYY-MM-DD)
  - url?: string
  - summary?: string

- Award
  - title: string
  - date?: string (YYYY-MM-DD)
  - awarder?: string
  - summary?: string

- Volunteer
  - organization?: string
  - position?: string
  - url?: string
  - startDate?: string (YYYY-MM-DD)
  - endDate?: string (YYYY-MM-DD)
  - summary?: string
  - highlights?: string[]

- Reference
  - name: string
  - reference?: string
  - contact?: string

## Validation Rules (selected)

- Email must be RFC 5322 compatible (Ajv `format: email`).
- URL fields use Ajv `format: uri`.
- Phone normalized to E.164 using `libphonenumber-js`; store original input separately if desired.
- Date fields: ISO 8601 `YYYY-MM-DD`; endDate must be >= startDate in same section.
- Arrays (highlights, courses, keywords, roles) are max length 100.
- Text fields trimmed; max length 10,000 chars.

## Backend Persistence (optional)

- Table `resumes` (JSONB-first approach):

```sql
CREATE TABLE IF NOT EXISTS resumes (
  id UUID PRIMARY KEY,
  owner_id UUID NOT NULL,
  data JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_resumes_owner ON resumes (owner_id);
CREATE INDEX IF NOT EXISTS idx_resumes_data_gin ON resumes USING GIN (data);
```

- Notes:
  - Application sets `app.current_tenant`/owner context via security layer (Keycloak), not in DB logic.
  - Use R2DBC repositories with parameterized queries; map domain to JSONB payload via adapter.

## Identifiers & Generation

- Client-only mode: local `id` as UUID v4 for draft; server will upsert by id.
- Server mode: `id` generated on create; returned to client.

## State Transitions (simplified)

- Draft → Validated → Exported (JSON) or Rendered (PDF)
- Actions:
  - Import JSON → Hydrate form, validate
  - Edit fields → Debounced preview
  - Validate JSON → global error panel
  - Export JSON → download
  - Generate PDF → preview, then download
  - Save to server (optional) → create/update resume
