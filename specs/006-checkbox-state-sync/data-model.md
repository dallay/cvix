# Data Model: Checkbox State Synchronization

**Feature**: 006-checkbox-state-sync
**Date**: 2026-01-11
**Status**: Documentation of existing model (no changes)

## Overview

This document describes the **existing** data model for section visibility state in the resume generator. No changes are required - this serves as reference documentation.

## Core Types

### SectionVisibility (Root State)

```typescript
// Location: client/apps/webapp/src/core/resume/domain/SectionVisibility.ts

interface SectionVisibility {
  resumeId: string;
  personalDetails: PersonalDetailsVisibility;
  work: ArraySectionVisibility;
  education: ArraySectionVisibility;
  skills: ArraySectionVisibility;
  projects: ArraySectionVisibility;
  certificates: ArraySectionVisibility;
  volunteer: ArraySectionVisibility;
  awards: ArraySectionVisibility;
  publications: ArraySectionVisibility;
  languages: ArraySectionVisibility;
  interests: ArraySectionVisibility;
  references: ArraySectionVisibility;
}
```

### ArraySectionVisibility (Section with Items)

```typescript
interface ArraySectionVisibility {
  enabled: boolean;      // Parent checkbox state
  expanded: boolean;     // Accordion expanded state
  items: boolean[];      // Per-item visibility (children)
}
```

### PersonalDetailsVisibility (Special Section)

```typescript
interface PersonalDetailsVisibility {
  enabled: true;         // Always enabled (FR-007)
  expanded: boolean;
  fields: {
    image: boolean;
    email: boolean;
    phone: boolean;
    location: LocationVisibility;
    summary: boolean;
    url: boolean;
    profiles: Record<string, boolean>;
  };
}
```

### SectionMetadata (Computed for UI)

```typescript
interface SectionMetadata {
  type: SectionType;
  labelKey: string;           // i18n key
  hasData: boolean;           // Section has resume data
  itemCount: number;          // Total items in section
  visibleItemCount: number;   // Items with visibility=true
}
```

## State Relationships

```text
┌─────────────────────────────────────────────────────────────┐
│                    SectionVisibility                         │
├─────────────────────────────────────────────────────────────┤
│  resumeId: "abc-123"                                        │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ work: ArraySectionVisibility                        │    │
│  │   enabled: true  ←───────────────────────┐          │    │
│  │   expanded: false                        │          │    │
│  │   items: [true, false, true]  ───────────┘          │    │
│  │            ↑                                        │    │
│  │   Parent derived from children:                     │    │
│  │   - all true → enabled: true                        │    │
│  │   - all false → enabled: false                      │    │
│  │   - mixed → enabled: true (indeterminate in UI)     │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## State Transitions

### Parent → Children (toggleSection)

```text
Before: enabled=true, items=[true, true, true] (all selected)
Action: toggleSection("work")
After:  enabled=false, items=[false, false, false] (none selected)

Before: enabled=false, items=[false, false, false] (none selected)
Action: toggleSection("work")
After:  enabled=true, items=[true, true, true] (all selected)

Before: enabled=true, items=[true, false, true] (indeterminate)
Action: toggleSection("work")
After:  enabled=true, items=[true, true, true] (all selected)
```

### Children → Parent (toggleItem)

```text
Before: enabled=true, items=[true, true] (all selected)
Action: toggleItem("work", 1)
After:  enabled=true, items=[true, false] (indeterminate)

Before: enabled=true, items=[true, false] (indeterminate)
Action: toggleItem("work", 0)
After:  enabled=false, items=[false, false] (none - auto-disabled)
```

## UI State Derivation

The `getSectionState()` function derives checkbox visual state:

```typescript
function getSectionState(section: SectionMetadata): boolean | "indeterminate" {
  // No data → unchecked
  if (section.itemCount === 0) return false;

  // No visible items → unchecked
  if (section.visibleItemCount === 0) return false;

  // All items visible → checked
  if (section.visibleItemCount === section.itemCount) return true;

  // Some items visible → indeterminate
  return "indeterminate";
}
```

## Persistence

State is persisted to localStorage with 300ms debounce:

```typescript
// Key format: section-visibility-{resumeId}
// Triggered by: watch(visibility, debouncedSave, { deep: true })
```

## Invariants

1. `personalDetails.enabled` is always `true` (cannot be disabled)
2. `name` field within personalDetails cannot be toggled
3. Section `enabled` state must reflect children: `enabled = items.some(Boolean)`
4. Empty sections (`itemCount === 0`) cannot be expanded
