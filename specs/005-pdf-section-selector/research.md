# Research: PDF Section Selector

**Feature**: `005-pdf-section-selector`
**Date**: 2025-12-06
**Status**: Complete

## Research Tasks Completed

### 1. Frontend Architecture for Section Visibility

**Question**: How should section visibility state be managed in the Vue application?

**Decision**: Create a dedicated Pinia store (`section-visibility.store.ts`) with localStorage persistence

**Rationale**:
- Pinia provides reactive state management with excellent TypeScript support
- Separate store keeps concerns isolated from main resume store
- localStorage ensures 30-day persistence requirement (FR-006, FR-016)
- Follows existing pattern in codebase (see `resume.store.ts`)

**Alternatives Considered**:
1. **Extend existing resume.store.ts** - Rejected because it would conflate resume data with UI preferences
2. **Component-level state with props drilling** - Rejected due to prop drilling complexity and lack of persistence
3. **URL query parameters** - Rejected because state would be lost on navigation and URLs become unwieldy

### 2. Section Toggle UI Component Pattern

**Question**: What UI pattern best matches the Figma design for section toggles?

**Decision**: Custom `SectionTogglePill` component using Tailwind CSS, with Reka UI's Collapsible for item expansion

**Rationale**:
- Figma design shows pill-shaped toggles that don't match standard Toggle component
- Design uses specific active/inactive states (purple fill vs white outline)
- Collapsible provides accessible expand/collapse for item lists
- Custom component gives precise control over visual design

**Design Tokens (from Figma analysis)**:
- Active pill: `bg-primary text-primary-foreground` with shadow
- Inactive pill: `bg-background border border-input text-muted-foreground`
- Checkmark icon in semi-transparent circle for active state
- 34px height, rounded-full corners

**Alternatives Considered**:
1. **Use existing Toggle component** - Rejected because visual design differs significantly
2. **Badge with click handler** - Rejected because Badge lacks proper toggle semantics
3. **Checkbox with custom styling** - Rejected because pill shape doesn't match checkbox affordances

### 3. Resume Filtering Strategy

**Question**: Should filtering happen on frontend or backend?

**Decision**: Frontend-only filtering with filtered data sent to backend for PDF generation

**Rationale**:
- Instant visual feedback (no round-trip latency) - SC-001 requirement
- Backend API contract remains unchanged (backward compatible)
- Filtering logic is simple array filtering (no complex computation)
- Resume data is already fully loaded on client
- Existing `ResumePdfPage.vue` sends full resume to `/api/resume/generate`

**Implementation**:
```typescript
// Filter service creates a new resume object with only visible sections/items
function filterResume(resume: Resume, visibility: SectionVisibility): Resume {
  return {
    ...resume,
    work: visibility.work.enabled
      ? resume.work.filter((_, i) => visibility.work.items[i] !== false)
      : [],
    // ... similar for other sections
  };
}
```

**Alternatives Considered**:
1. **Backend filtering with visibility payload** - Rejected due to added latency and API contract changes
2. **GraphQL-style field selection** - Rejected as overkill; would require significant API rearchitecture

### 4. Personal Details Field-Level Control

**Question**: How to handle Personal Details section which has individual field toggles rather than array items?

**Decision**: Model Personal Details visibility as a record of field names to boolean values

**Rationale**:
- Personal Details has fixed fields (name, email, phone, location, image)
- Name is always required (FR-013)
- Different structure from array-based sections like Work Experience
- Matches UI design where Personal Details expands to show field-level toggles

**Data Model**:
```typescript
interface PersonalDetailsVisibility {
  enabled: true; // Always enabled (cannot be disabled per FR-007)
  fields: {
    image: boolean;
    email: boolean;
    phone: boolean;
    location: boolean;
    // name is always visible, not toggleable
  };
}
```

### 5. Persistence Strategy

**Question**: How to implement 30-day preference persistence (SC-005)?

**Decision**: localStorage with resume-specific key and timestamp-based expiration

**Rationale**:
- localStorage persists across browser sessions (unlike sessionStorage)
- Resume ID as key prefix allows per-resume preferences
- Timestamp enables 30-day TTL enforcement on load
- Simple implementation without backend changes

**Implementation**:
```typescript
const STORAGE_KEY_PREFIX = 'cvix-section-visibility-';
const TTL_MS = 30 * 24 * 60 * 60 * 1000; // 30 days

interface StoredPreference {
  visibility: SectionVisibility;
  timestamp: number;
}

// On save
localStorage.setItem(key, JSON.stringify({ visibility, timestamp: Date.now() }));

// On load
const data = JSON.parse(localStorage.getItem(key));
if (Date.now() - data.timestamp > TTL_MS) {
  localStorage.removeItem(key);
  return defaultVisibility;
}
```

### 6. Section Ordering

**Question**: How to maintain standard resume section order (FR-009)?

**Decision**: Define static section order constant; UI and PDF generation iterate in this order

**Rationale**:
- Standard resume order is well-established (Personal → Work → Education → Skills → Projects → Certs)
- Order should not change based on toggle sequence
- Simple array constant ensures consistency

**Implementation**:
```typescript
export const SECTION_ORDER = [
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
  'references'
] as const;
```

### 7. Empty Section Handling

**Question**: How to handle sections with no data?

**Decision**: Render pill in disabled state with tooltip "No data available" (FR-008)

**Rationale**:
- Users should understand why a section can't be toggled
- Consistent with edge case specification
- Tooltip provides context without cluttering UI

**Implementation**:
```vue
<SectionTogglePill
  :disabled="!hasData(section)"
  :title="hasData(section) ? undefined : t('resume.pdfPage.noDataAvailable')"
/>
```

### 8. Expand/Collapse Item List Interaction

**Question**: How should the expand/collapse of item lists within sections work?

**Decision**: Clicking section pill toggles expansion inline below the pill (per clarification from spec)

**Rationale**:
- Spec clarifies: "clicking on a section pill expands an item list directly below it"
- Inline expansion keeps context visible
- No modal/drawer complexity
- Natural UI pattern (similar to accordion)

**Implementation**: Use Reka UI's Collapsible component wrapped in each section pill

### 9. Auto-Disable Section When All Items Disabled

**Question**: What happens when user disables all items within an enabled section? (Edge case)

**Decision**: Automatically disable section and show visual warning indicator (FR-017)

**Rationale**:
- Spec states: "section should automatically become disabled, or show visual warning"
- Auto-disable prevents generating section with zero content
- Brief toast notification informs user of automatic action

**Implementation**:
```typescript
watch(visibility.work.items, (items) => {
  if (items.every(item => !item)) {
    visibility.work.enabled = false;
    toast.info(t('resume.pdfPage.sectionAutoDisabled', { section: 'Work Experience' }));
  }
});
```

### 10. Keyboard Accessibility

**Question**: How to ensure section pills meet accessibility requirements?

**Decision**: Use semantic button elements with proper ARIA attributes

**Rationale**:
- Constitution requires keyboard accessibility for all interactive elements
- Toggle pattern should support Enter/Space to activate
- Focus indicators must be visible
- Screen reader must announce state (expanded/collapsed, checked/unchecked)

**Implementation**:
```vue
<button
  role="switch"
  :aria-checked="isEnabled"
  :aria-expanded="isExpanded"
  @keydown.space.prevent="toggle"
  @keydown.enter="toggle"
  class="focus:ring-2 focus:ring-ring focus:ring-offset-2"
>
```

## Technology Stack Confirmation

| Layer              | Technology   | Version | Usage                      |
| ------------------ | ------------ | ------- | -------------------------- |
| Frontend Framework | Vue.js       | 3.5.17  | Component composition      |
| State Management   | Pinia        | 3.0.3   | Section visibility store   |
| Styling            | TailwindCSS  | 4.1.11  | Design token-based styling |
| UI Components      | Reka UI      | -       | Collapsible, primitives    |
| Storage            | localStorage | -       | Preference persistence     |
| Testing            | Vitest       | -       | Unit/integration tests     |
| E2E Testing        | Playwright   | -       | User flow verification     |
| i18n               | vue-i18n     | -       | Localized labels           |

## No Backend Changes Required

The section filtering feature is implemented entirely on the frontend:
- Resume data is already loaded on the client
- Filtered resume is constructed client-side before sending to PDF generation API
- Existing `/api/resume/generate` endpoint accepts partial resume data (all arrays are optional per `GenerateResumeRequest.kt`)
- No API contract changes required
- Backward compatibility maintained

## Resolved Clarifications

| Question                         | Resolution                                                                    |
| -------------------------------- | ----------------------------------------------------------------------------- |
| Item-level toggle interaction    | Section pills expand inline to show item list (spec clarification 2025-12-06) |
| Personal Details minimum content | Name always visible; email, phone, location, image toggleable (FR-013)        |
| Section order                    | Static order maintained regardless of toggle sequence (FR-009)                |
| Persistence duration             | 30 days via localStorage with timestamp (SC-005)                              |
