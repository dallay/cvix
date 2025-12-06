# API Contracts: PDF Section Selector

**Feature**: `005-pdf-section-selector`
**Date**: 2025-12-06

## Overview

This feature **does not require backend API changes**. The section filtering is implemented entirely on the frontend, and the existing `/api/resume/generate` endpoint already accepts partial resume data (all section arrays are optional).

## Existing API (No Changes Required)

### POST /api/resume/generate

The existing PDF generation endpoint continues to work unchanged:

```
POST /api/resume/generate
Content-Type: application/vnd.api.v1+json
Accept-Language: en

{
  "basics": { ... },          // Required
  "work": [...],              // Optional - can be empty or omitted
  "education": [...],         // Optional
  "skills": [...],            // Optional
  "projects": [...],          // Optional
  "certificates": [...],      // Optional
  "volunteer": [...],         // Optional
  "awards": [...],            // Optional
  "publications": [...],      // Optional
  "languages": [...],         // Optional
  "interests": [...],         // Optional
  "references": [...]         // Optional
}
```

**Response**: PDF binary with `Content-Type: application/pdf`

### Frontend Filtering Contract

The frontend applies visibility preferences and sends only the visible content to the existing API:

```typescript
// Before API call
const filteredResume = filterResume(resume, visibility);

// API call uses filtered data
await generatePdf(filteredResume, templateId, params);
```

## Frontend Contracts

### SectionVisibility Store Interface

```typescript
interface UseSectionVisibilityStore {
  // State
  visibility: Ref<SectionVisibility | null>;
  isLoading: Ref<boolean>;
  error: Ref<Error | null>;

  // Getters
  getSectionMetadata: ComputedRef<SectionMetadata[]>;
  getVisibleItemCount: (section: ArraySectionType) => number;

  // Actions
  initialize(resumeId: string, resume: Resume): void;
  toggleSection(section: SectionType): void;
  toggleItem(section: ArraySectionType, index: number): void;
  togglePersonalDetailsField(field: keyof PersonalDetailsFieldVisibility): void;
  expandSection(section: SectionType): void;
  collapseSection(section: SectionType): void;
  reset(): void;
}
```

### Component Props Contracts

#### SectionTogglePanel

```typescript
interface SectionTogglePanelProps {
  /** The resume to derive section metadata from */
  resume: Resume;

  /** Current visibility preferences */
  visibility: SectionVisibility;

  /** Callback when section is toggled */
  onToggleSection: (section: SectionType) => void;

  /** Callback when item within section is toggled */
  onToggleItem: (section: ArraySectionType, index: number) => void;

  /** Callback when Personal Details field is toggled */
  onToggleField: (field: keyof PersonalDetailsFieldVisibility) => void;
}
```

#### SectionTogglePill

```typescript
interface SectionTogglePillProps {
  /** Section display label */
  label: string;

  /** Whether section is currently enabled/visible */
  enabled: boolean;

  /** Whether section has data available */
  hasData: boolean;

  /** Whether pill is expanded to show items */
  expanded: boolean;

  /** Number of visible items (for display) */
  visibleCount?: number;

  /** Total number of items (for display) */
  totalCount?: number;

  /** Tooltip text when disabled due to no data */
  disabledTooltip?: string;
}

interface SectionTogglePillEmits {
  /** Emitted when pill is clicked to toggle enabled state */
  toggle: [];

  /** Emitted when expand/collapse is triggered */
  expand: [];
}
```

#### ItemToggleList

```typescript
interface ItemToggleListProps {
  /** Section type for labeling */
  sectionType: ArraySectionType;

  /** Items to display with toggle controls */
  items: Array<{
    label: string;
    sublabel?: string;
    enabled: boolean;
  }>;
}

interface ItemToggleListEmits {
  /** Emitted when an item is toggled */
  toggleItem: [index: number];
}
```

## Storage Contract

### localStorage Schema

**Key Pattern**: `cvix-section-visibility-{resumeId}`

**Value Schema**:
```typescript
{
  "visibility": SectionVisibility,  // See data-model.md
  "savedAt": number,                // Unix timestamp
  "version": 1                      // Schema version for migrations
}
```

### SectionVisibilityStorage Interface

```typescript
interface SectionVisibilityStorage {
  /**
   * Save visibility preferences for a resume.
   * @param resumeId - Unique resume identifier
   * @param visibility - Preferences to save
   */
  save(resumeId: string, visibility: SectionVisibility): void;

  /**
   * Load visibility preferences for a resume.
   * Returns null if no preferences exist or TTL expired.
   * @param resumeId - Unique resume identifier
   */
  load(resumeId: string): SectionVisibility | null;

  /**
   * Clear stored preferences for a resume.
   * @param resumeId - Unique resume identifier
   */
  clear(resumeId: string): void;

  /**
   * Check if valid preferences exist for a resume.
   * @param resumeId - Unique resume identifier
   */
  exists(resumeId: string): boolean;
}
```

## Event Contracts

### Vue Events (Component to Parent)

| Component         | Event        | Payload         | Description                               |
| ----------------- | ------------ | --------------- | ----------------------------------------- |
| SectionTogglePill | `toggle`     | none            | User clicked to toggle section visibility |
| SectionTogglePill | `expand`     | none            | User clicked to expand/collapse item list |
| ItemToggleList    | `toggleItem` | `index: number` | User toggled specific item                |

### Pinia Store Events (for watchers)

```typescript
// Watch for visibility changes to trigger preview update
watch(
  () => visibilityStore.visibility,
  (newVisibility) => {
    // Re-filter resume and update preview
    const filtered = filterResume(resume, newVisibility);
    previewStore.setFilteredResume(filtered);
  },
  { deep: true }
);
```

## i18n Keys Contract

```typescript
// New i18n keys required
{
  "resume.pdfPage.visibleSections": "Visible Sections",
  "resume.pdfPage.addCustomSection": "Add Custom Section",
  "resume.pdfPage.noDataAvailable": "No data available",
  "resume.pdfPage.sectionAutoDisabled": "{section} disabled (no items selected)",
  "resume.pdfPage.itemCount": "{visible} of {total}",

  // Section labels
  "resume.sections.personalDetails": "Personal Details",
  "resume.sections.work": "Work Experience",
  "resume.sections.education": "Education",
  "resume.sections.skills": "Skills",
  "resume.sections.projects": "Projects",
  "resume.sections.certifications": "Certifications",
  "resume.sections.volunteer": "Volunteer",
  "resume.sections.awards": "Awards",
  "resume.sections.publications": "Publications",
  "resume.sections.languages": "Languages",
  "resume.sections.interests": "Interests",
  "resume.sections.references": "References",

  // Personal Details fields
  "resume.fields.image": "Profile Image",
  "resume.fields.email": "Email",
  "resume.fields.phone": "Phone",
  "resume.fields.location": "Location",
  "resume.fields.summary": "Summary",
  "resume.fields.url": "Website",
  "resume.fields.profiles": "Social Profiles"
}
```
