# Data Model: PDF Section Selector

**Feature**: `005-pdf-section-selector`
**Date**: 2025-12-06

## Overview

This document defines the data models for managing section and item visibility preferences in the
PDF Section Selector feature. All models are frontend-only (TypeScript) as the filtering logic is
implemented client-side.

## Entities

### 1. SectionVisibility

**Purpose**: Root model representing visibility preferences for all resume sections.

**Location**: `client/apps/webapp/src/core/resume/domain/SectionVisibility.ts`

```typescript
/**
 * Root visibility preferences for a resume.
 * Controls which sections and items appear in the PDF export.
 */
export interface SectionVisibility {
    /** Unique identifier for the resume these preferences apply to */
    resumeId: string;

    /** Personal details section (always enabled, individual fields toggleable) */
    personalDetails: PersonalDetailsVisibility;

    /** Work experience section visibility */
    work: ArraySectionVisibility;

    /** Education section visibility */
    education: ArraySectionVisibility;

    /** Skills section visibility */
    skills: ArraySectionVisibility;

    /** Projects section visibility */
    projects: ArraySectionVisibility;

    /** Certifications section visibility */
    certifications: ArraySectionVisibility;

    /** Volunteer experience section visibility */
    volunteer: ArraySectionVisibility;

    /** Awards section visibility */
    awards: ArraySectionVisibility;

    /** Publications section visibility */
    publications: ArraySectionVisibility;

    /** Languages section visibility */
    languages: ArraySectionVisibility;

    /** Interests section visibility */
    interests: ArraySectionVisibility;

    /** References section visibility */
    references: ArraySectionVisibility;
}
```

**Validation Rules**:

- `resumeId` must be a non-empty string
- `personalDetails.enabled` is always `true` (enforced at type level)
- At least one array section should have enabled items for PDF generation

**Relationships**:

- 1:1 with Resume (preferences are resume-specific)
- Contains 1 PersonalDetailsVisibility
- Contains 11 ArraySectionVisibility instances

---

### 2. PersonalDetailsVisibility

**Purpose**: Special visibility model for Personal Details section with field-level toggles.

**Location**: `client/apps/webapp/src/core/resume/domain/SectionVisibility.ts`

```typescript
/**
 * Visibility settings for the Personal Details section.
 * This section cannot be fully disabled (FR-007).
 * Individual fields can be toggled except for name (FR-013).
 */
export interface PersonalDetailsVisibility {
    /** Always true - Personal Details cannot be disabled */
    readonly enabled: true;

    /** Whether the section is expanded to show field toggles */
    expanded: boolean;

    /** Individual field visibility */
    fields: PersonalDetailsFieldVisibility;
}

/**
 * Individual field visibility within Personal Details.
 * Note: 'name' is always visible and not included here.
 */
export interface PersonalDetailsFieldVisibility {
    /** Profile image visibility */
    image: boolean;

    /** Email address visibility */
    email: boolean;

    /** Phone number visibility */
    phone: boolean;

    /** Location/address visibility */
    location: {
        address: boolean;
        postalCode: boolean;
        city: boolean;
        countryCode: boolean;
        region: boolean;
    };

    /** Professional summary visibility */
    summary: boolean;

    /** Website URL visibility */
    url: boolean;

    /** Social profiles visibility */
    profiles: { [profile: string]: boolean };
}
```

**Validation Rules**:

- `enabled` is always `true` (type-enforced)
- Name field is implicitly always visible
- All `fields` properties default to `true`

**State Transitions**:

- `expanded`: false → true (on pill click)
- `expanded`: true → false (on pill click again)
- Individual field: true → false → true (on field toggle)

---

### 3. ArraySectionVisibility

**Purpose**: Visibility model for sections containing arrays of items (work experience, education,
etc.).

**Location**: `client/apps/webapp/src/core/resume/domain/SectionVisibility.ts`

```typescript
/**
 * Visibility settings for a section containing multiple items.
 * Used for Work Experience, Education, Skills, Projects, etc.
 */
export interface ArraySectionVisibility {
    /** Whether the entire section is enabled */
    enabled: boolean;

    /** Whether the section is expanded to show item toggles */
    expanded: boolean;

    /**
     * Visibility state for each item in the section.
     * Index corresponds to item index in the resume array.
     * true = visible, false = hidden
     */
    items: boolean[];
}
```

**Validation Rules**:

- When `enabled` is `false`, section is hidden regardless of `items`
- When all `items` are `false`, `enabled` should auto-set to `false` (FR-017)
- `items` array length should match corresponding resume section length

**State Transitions**:

- `enabled`: true → false (on section pill click or all items disabled)
- `enabled`: false → true (on section pill click, enables all items)
- `expanded`: false → true → false (on section pill click when enabled)
- `items[n]`: true → false → true (on individual item toggle)

---

### 4. SectionType (Enum)

**Purpose**: Type-safe enumeration of all resume section types.

**Location**: `client/apps/webapp/src/core/resume/domain/SectionVisibility.ts`

```typescript
/**
 * Enumeration of all resume section types.
 * Order defines standard resume section ordering (FR-009).
 */
export const SECTION_TYPES = [
            'personalDetails',
            'work',
            'education',
            'skills',
            'projects',
            'certifications',
            'volunteer',
            'awards',
            'publications',
            'languages',
            'interests',
            'references',
        ] as const;

export type SectionType = typeof SECTION_TYPES[number];

/**
 * Sections that contain arrays of items (excludes personalDetails)
 */
export type ArraySectionType = Exclude<SectionType, 'personalDetails'>;
```

---

### 5. SectionMetadata

**Purpose**: UI metadata for rendering section pills (labels, icons, data availability).

**Location**: `client/apps/webapp/src/core/resume/domain/SectionVisibility.ts`

```typescript
/**
 * Metadata for rendering a section toggle pill.
 */
export interface SectionMetadata {
    /** Section type identifier */
    type: SectionType;

    /** Internationalized display label key */
    labelKey: string;

    /** Whether the section has data in the resume */
    hasData: boolean;

    /** Number of items in the section (0 for personalDetails) */
    itemCount: number;

    /** Number of currently visible items */
    visibleItemCount: number;
}
```

---

### 6. StoredSectionVisibility

**Purpose**: Wrapper for persisting visibility preferences with metadata.

**Location**:
`client/apps/webapp/src/core/resume/infrastructure/storage/SectionVisibilityStorage.ts`

```typescript
/**
 * Persisted visibility preference with metadata.
 * Stored in localStorage with TTL enforcement.
 */
export interface StoredSectionVisibility {
    /** The visibility preferences */
    visibility: SectionVisibility;

    /** Unix timestamp when preferences were last saved */
    savedAt: number;

    /** Schema version for migration support */
    version: number;
}

/**
 * Current schema version.
 * Increment when making breaking changes to SectionVisibility structure.
 */
export const VISIBILITY_SCHEMA_VERSION = 1;

/**
 * Time-to-live for stored preferences (30 days in milliseconds).
 */
export const VISIBILITY_TTL_MS = 30 * 24 * 60 * 60 * 1000;
```

---

## Factory Functions

### createDefaultVisibility

**Purpose**: Generate default visibility preferences for a resume.

```typescript
/**
 * Creates default visibility preferences with all sections/items enabled.
 *
 * @param resumeId - Unique identifier for the resume
 * @param resume - The resume to generate defaults for
 * @returns Default SectionVisibility with all content visible
 */
export function createDefaultVisibility(
        resumeId: string,
        resume: Resume
): SectionVisibility {
    return {
        resumeId,
        personalDetails: {
            enabled: true,
            expanded: false,
            fields: {
                image: true,
                email: true,
                phone: true,
                location: {
                    address: true,
                    postalCode: true,
                    city: true,
                    countryCode: true,
                    region: true,
                },
                summary: true,
                url: true,
                profiles: {}, // All profiles enabled by default
            },
        },
        work: createArrayVisibility(resume.work.length),
        education: createArrayVisibility(resume.education.length),
        skills: createArrayVisibility(resume.skills.length),
        projects: createArrayVisibility(resume.projects.length),
        certifications: createArrayVisibility(resume.certificates.length),
        volunteer: createArrayVisibility(resume.volunteer.length),
        awards: createArrayVisibility(resume.awards.length),
        publications: createArrayVisibility(resume.publications.length),
        languages: createArrayVisibility(resume.languages.length),
        interests: createArrayVisibility(resume.interests.length),
        references: createArrayVisibility(resume.references.length),
    };
}

/**
 * Creates default visibility for an array section.
 */
function createArrayVisibility(itemCount: number): ArraySectionVisibility {
    return {
        enabled: itemCount > 0,
        expanded: false,
        items: Array(itemCount).fill(true),
    };
}
```

---

## Computed/Derived Data

### FilteredResume

The filtering service produces a new Resume object with hidden sections/items removed:

```typescript
/**
 * Applies visibility preferences to filter a resume.
 * Returns a new Resume object containing only visible content.
 */
export function filterResume(
        resume: Resume,
        visibility: SectionVisibility
): Resume {
    return {
        basics: filterBasics(resume.basics, visibility.personalDetails),
        work: filterArray(resume.work, visibility.work),
        education: filterArray(resume.education, visibility.education),
        skills: filterArray(resume.skills, visibility.skills),
        projects: filterArray(resume.projects, visibility.projects),
        certificates: filterArray(resume.certificates, visibility.certifications),
        volunteer: filterArray(resume.volunteer, visibility.volunteer),
        awards: filterArray(resume.awards, visibility.awards),
        publications: filterArray(resume.publications, visibility.publications),
        languages: filterArray(resume.languages, visibility.languages),
        interests: filterArray(resume.interests, visibility.interests),
        references: filterArray(resume.references, visibility.references),
    };
}
```

---

## Entity Relationship Diagram

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                          SectionVisibility                              │
├─────────────────────────────────────────────────────────────────────────┤
│ resumeId: string                                                        │
│ personalDetails: PersonalDetailsVisibility ────────────────────┐        │
│ work: ArraySectionVisibility ──────────────────────────────────┤        │
│ education: ArraySectionVisibility ─────────────────────────────┤        │
│ skills: ArraySectionVisibility ────────────────────────────────┤        │
│ projects: ArraySectionVisibility ──────────────────────────────┤        │
│ certifications: ArraySectionVisibility ────────────────────────┤        │
│ volunteer: ArraySectionVisibility ─────────────────────────────┤        │
│ awards: ArraySectionVisibility ────────────────────────────────┤        │
│ publications: ArraySectionVisibility ──────────────────────────┤        │
│ languages: ArraySectionVisibility ─────────────────────────────┤        │
│ interests: ArraySectionVisibility ─────────────────────────────┤        │
│ references: ArraySectionVisibility ────────────────────────────┘        │
└─────────────────────────────────────────────────────────────────────────┘
                              │
                              │ 1:1 (stored in localStorage)
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      StoredSectionVisibility                            │
├─────────────────────────────────────────────────────────────────────────┤
│ visibility: SectionVisibility                                           │
│ savedAt: number (Unix timestamp)                                        │
│ version: number                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────┐    ┌─────────────────────────────────┐
│  PersonalDetailsVisibility      │    │    ArraySectionVisibility       │
├─────────────────────────────────┤    ├─────────────────────────────────┤
│ enabled: true (always)          │    │ enabled: boolean                │
│ expanded: boolean               │    │ expanded: boolean               │
│ fields:                         │    │ items: boolean[]                │
│   image: boolean                │    └─────────────────────────────────┘
│   email: boolean                │
│   phone: boolean                │
│   location: {                   │
│            address: true,       │
│            postalCode: true,    │
│            city: true,          │
│            countryCode: true,   │
│            region: true,        │
│               }                 │
│   summary: boolean              │
│   url: boolean                  │
│   profiles:  {}                 │
└─────────────────────────────────┘
```

---

## State Machine: Section Toggle

```text
                    ┌──────────────────────────────────────┐
                    │                                      │
    ┌───────────────▼───────────────┐    click pill        │
    │                               │    (when disabled)   │
    │    DISABLED                   │──────────────────────┘
    │    (enabled: false)           │
    │                               │
    └───────────────────────────────┘
              ▲
              │ all items disabled (auto-transition)
              │ OR click pill
              │
    ┌─────────┴─────────────────────┐
    │                               │
    │    ENABLED_COLLAPSED          │◄─────── click pill ────┐
    │    (enabled: true,            │                        │
    │     expanded: false)          │                        │
    │                               │                        │
    └───────────────────────────────┘                        │
              │                                              │
              │ click pill (when enabled)                    │
              ▼                                              │
    ┌───────────────────────────────┐                        │
    │                               │                        │
    │    ENABLED_EXPANDED           │────────────────────────┘
    │    (enabled: true,            │    click pill
    │     expanded: true)           │
    │                               │
    └───────────────────────────────┘
```

---

## Index/Key Strategy

**localStorage Key Format**:

```text
cvix-section-visibility-{resumeId}
```

**Example**:

```text
cvix-section-visibility-550e8400-e29b-41d4-a716-446655440000
```

This ensures:

- Unique preferences per resume
- Easy debugging/inspection in browser DevTools
- Namespace collision prevention with `cvix-` prefix
