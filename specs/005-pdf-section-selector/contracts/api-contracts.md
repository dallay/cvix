# API Contracts: PDF Section Selector

**Feature**: `005-pdf-section-selector`
**Date**: 2025-12-06

## Overview

This feature **does not require backend API changes**. The section filtering is implemented entirely
on the frontend, and the existing `/api/resume/generate` endpoint already accepts partial resume
data (all section arrays are optional).

## Existing API (No Changes Required)

### POST /api/resume/generate

The existing PDF generation endpoint continues to work unchanged:

```text
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
    visibility: Ref<SectionVisibility>;
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
}

interface SectionTogglePanelEmits {
    /** Emitted when section is toggled */
    (e: 'toggle-section', section: SectionType): void;

    /** Emitted when section is expanded/collapsed */
    (e: 'expand-section', section: SectionType): void;

    /** Emitted when item within section is toggled */
    (e: 'toggle-item', section: ArraySectionType, index: number): void;

    /** Emitted when Personal Details field is toggled */
    (e: 'toggle-field', field: keyof PersonalDetailsFieldVisibility): void;
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
interface SectionVisibilityStorageData {
  visibility: {
    work: { enabled: boolean; items: boolean[] };
    education: { enabled: boolean; items: boolean[] };
    skills: { enabled: boolean; items: boolean[] };
    projects: { enabled: boolean; items: boolean[] };
    certificates: { enabled: boolean; items: boolean[] };
    volunteer: { enabled: boolean; items: boolean[] };
    awards: { enabled: boolean; items: boolean[] };
    publications: { enabled: boolean; items: boolean[] };
    languages: { enabled: boolean; items: boolean[] };
    interests: { enabled: boolean; items: boolean[] };
    references: { enabled: boolean; items: boolean[] };
  };
  savedAt: number; // Unix timestamp in milliseconds
  version: 1; // Schema version (increment on breaking changes; stale versions cleared on load)
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
     * TTL is 30 days from savedAt timestamp; expired preferences are auto-cleared on load.
     * Schema version mismatches result in cleared stale data and null return.
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
            if (!newVisibility) return; // Early return guard for falsy visibility

            try {
                // Re-filter resume and update preview
                const filtered = filterResume(resume, newVisibility);
                previewStore.setFilteredResume(filtered);
            } catch (error) {
                console.error('Error updating preview:', error);
                // Recovery action: clear preview or surface user-facing error
                previewStore.setFilteredResume(null);
                toast.error('Failed to update preview. Please try again.');
            }
        },
        {deep: true}
);

// Watch for all items toggled off to auto-disable section (FR-017)
watch(
        () => visibilityStore.visibility,
        (newVisibility, oldVisibility) => {
            if (!newVisibility) return;
            for (const section of Object.keys(newVisibility)) {
                const vis = newVisibility[section];
                if (vis && Array.isArray(vis.items) && vis.items.length > 0) {
                    const allOff = vis.items.every((v) => v === false);
                    if (allOff && vis.enabled) {
                        visibilityStore.disableSection(section); // Use action instead
                        // Optionally emit event or show warning
                    }
                }
            }
        },
        {deep: true}
);
```

## i18n Keys Contract

```json5
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
  "resume.sections.certificates": "Certificates",
  "resume.sections.volunteer": "Volunteer",
  "resume.sections.awards": "Awards",
  "resume.sections.publications": "Publications",
  "resume.fields.name": "Name", // Added for PersonalDetailsFieldList

  // Personal Details fields
  "resume.fields.email": "Email",
  "resume.fields.phone": "Phone",
  "resume.fields.location": "Location",
  "resume.fields.image": "Profile Image",
  "resume.fields.summary": "Summary",
  "resume.fields.url": "Website",
  "resume.fields.profiles": "Profiles",

  // PDF Page UI labels
  "resume.pdfPage.alwaysVisible": "Always visible" // Added for always-visible label
}
```
