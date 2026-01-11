# Feature Specification: Checkbox State Synchronization

**Feature Branch**: `006-checkbox-state-sync`
**Created**: January 10, 2026
**Updated**: January 11, 2026
**Status**: In Progress (Phase 6 - Follow-up Fixes)
**Input**: UX Gap Analysis - Checkbox state inconsistency in Content Selection panel

## Problem Statement

The Content Selection tree (SectionTogglePanel) checkboxes were completely unresponsive to user clicks. Clicking a checkbox did nothing - the checkbox state remained unchanged and sections stayed permanently selected.

### Expected Behavior

1. **Parent -> Children (Downward)**: Checking a parent MUST check all children automatically
2. **Children -> Parent (Upward)**: Parent state MUST be derived from children:
   - All checked -> Parent checked
   - None checked -> Parent unchecked
   - Some checked -> Parent indeterminate
3. **Click Response**: Clicking a checkbox MUST toggle its state immediately

### Actual Failure Mode

**Checkboxes did not respond to clicks at all**. The store logic was correct, but the UI event binding was broken.

---

## Root Cause Analysis

### Initial Analysis (Incorrect)

Initial code analysis concluded that all functionality was correctly implemented in the store layer. This was a **false positive** because:

1. The unit tests passed (they test store methods directly)
2. The store logic IS correct
3. The bug was in the **UI binding layer**, not the business logic

### Actual Root Cause

**File**: `client/packages/ui/src/components/ui/checkbox/Checkbox.vue`

**Problem**: API mismatch between the wrapper component and Reka UI's CheckboxRoot.

| Layer | Expected Event | Actual Event | Result |
|-------|----------------|--------------|--------|
| Consumer (SectionAccordionItem.vue) | `@update:checked` | - | Event handler attached |
| Wrapper (Checkbox.vue) | Emit `update:checked` | Only emitted `update:modelValue` | **EVENT NEVER FIRED** |
| Reka UI (CheckboxRoot) | - | Emits `update:modelValue` | Working correctly |

**Technical Details**:

1. The wrapper defined a custom `checked` prop (line 12) but Reka UI uses `modelValue`
2. `useForwardPropsEmits()` only forwarded `CheckboxRootEmits` which emits `update:modelValue`
3. Consumers were listening for `@update:checked` which was never emitted
4. The `:checked="props.checked"` binding was useless (Reka UI doesn't have a `checked` prop)

### Affected Components

All components using `@update:checked`:
- `SectionAccordionItem.vue` - section toggle checkboxes
- `ItemToggleList.vue` - individual item checkboxes
- `LoginForm.vue` - remember me checkbox
- `VolunteerSection.vue` - current checkbox
- `ProjectSection.vue` - current checkbox

---

## Solution Implemented

### Fix Applied

**File**: `client/packages/ui/src/components/ui/checkbox/Checkbox.vue`

**Approach**: Adapter pattern - intercept Reka UI's `update:modelValue` and re-emit as both `update:modelValue` AND `update:checked` for backward compatibility.

```typescript
// Extended emits to support both patterns
const emits = defineEmits<
  CheckboxRootEmits & {
    "update:checked": [value: CheckedValue];
  }
>();

// Handle value changes and emit both events
const handleUpdate = (value: CheckedValue) => {
  emits("update:modelValue", value);
  emits("update:checked", value);
};
```

```vue
<CheckboxRoot
  v-bind="delegatedProps"
  :model-value="modelValue"
  @update:model-value="handleUpdate"
>
```

### Why This Approach

1. **No breaking changes**: Existing consumers using `@update:checked` continue to work
2. **Forward compatible**: New consumers can use `@update:modelValue` (Vue 3 convention)
3. **Single point of fix**: Only one file modified instead of updating all consumers
4. **Aligns with Reka UI v2**: Migration guide recommends `v-model` over `v-model:checked`

---

## Follow-up Bug Fixes (Phase 6)

### Bug 1: localStorage Persistence Not Working

**Symptom**: Checkbox state was not restored on page reload.

**Root Cause**: `section-visibility.store.ts` line 103:
```typescript
const id = newResumeId || crypto.randomUUID();
```

The `ResumePdfPage.vue` calls `initialize(resumeStore.resume)` WITHOUT a `resumeId`, generating a new random UUID each time. This meant localStorage lookups always failed (different key each time).

**Fix**: Use a stable default ID for single-resume mode:
```typescript
const DEFAULT_RESUME_ID = "default";
const id = newResumeId || DEFAULT_RESUME_ID;
```

### Bug 2: PDF Re-renders When Expanding Sections

**Symptom**: Clicking to expand a section accordion caused the PDF to re-render (visual flash, network request).

**Root Cause**: The `expanded` state was stored inside the `visibility` object. The `filteredResume` computed watches `visibility` with `{ deep: true }`, so ANY change to visibility (including expand/collapse) triggered PDF regeneration.

**Fix**: Architectural separation of concerns:
- `visibility` - domain state (what's visible in PDF) - triggers PDF regeneration
- `expandedSections` - UI-only state (accordion open/closed) - does NOT trigger PDF regeneration

```typescript
// NEW: Separate UI state for accordion expansion
const expandedSections = ref<Record<SectionType, boolean>>({
  personalDetails: false,
  work: false,
  // ...etc
});
```

**Components Updated**:
- `section-visibility.store.ts` - Added `expandedSections` ref and `isSectionExpanded()` helper
- `SectionTogglePanel.vue` - New `expandedSections` prop
- `ResumePdfPage.vue` - Passes `expandedSections` from store

---

## Test Coverage

### Existing Unit Tests (Pass)

The store logic was already correctly tested with 21+ unit tests in:
`client/apps/webapp/src/core/resume/infrastructure/store/section-visibility.store.spec.ts`

These tests passed because they test `toggleSection()` and `toggleItem()` directly, not through the UI.

**Updated tests for Phase 6**:
- Changed UUID test to verify stable "default" ID
- Updated `toggleSectionExpanded` tests to check `expandedSections` ref instead of `visibility.*.expanded`

### E2E Tests (Added)

New Playwright tests added to verify the UI binding works:
`client/apps/webapp/e2e/checkbox-sync/checkbox-sync.spec.ts`

| Test | Description |
|------|-------------|
| US1-001 | Section checkbox toggles from checked to unchecked |
| US1-002 | Section checkbox toggles from unchecked to checked |
| US1-003 | Section checkbox enables all children when clicked from indeterminate |
| US2-001 | Unchecking one item shows indeterminate state on parent |
| US2-002 | Unchecking all items shows unchecked state on parent |
| US2-003 | Checking all items shows checked state on parent |

---

## Lessons Learned

1. **Unit tests don't catch UI binding bugs**: Store tests passing doesn't mean the UI works
2. **Integration/E2E tests are essential**: Need tests that click actual checkboxes
3. **API wrappers need careful review**: When wrapping third-party components, verify event forwarding
4. **False positives are dangerous**: Code analysis can miss runtime binding issues
5. **Manual QA catches integration bugs**: Two new bugs found only through manual testing
6. **Separation of concerns matters for performance**: UI state (expanded) vs domain state (visibility)

---

## Success Criteria

- [x] Checkboxes respond to clicks
- [x] Parent checkbox state propagates to children
- [x] Child checkbox state updates parent (indeterminate/checked/unchecked)
- [x] Unit tests continue to pass (21/21)
- [x] E2E tests verify UI binding
- [x] localStorage persistence works across page reloads
- [x] Expanding sections does NOT trigger PDF re-render
- [ ] Manual QA confirms expected behavior (second round pending)

---

## User Scenarios

### User Story 1 - Section Toggle Synchronization (Priority: P1)

A user toggles a resume section checkbox to control all items within that section.

**Acceptance Scenarios**:

1. **Given** a section with 3 items all checked, **When** user unchecks the section, **Then** all 3 items become unchecked AND section appears unchecked
2. **Given** a section with 3 items all unchecked, **When** user checks the section, **Then** all 3 items become checked AND section appears checked
3. **Given** a section with 2 of 3 items checked (indeterminate), **When** user clicks the section, **Then** all 3 items become checked (select all behavior)

---

### User Story 2 - Item Toggle Updates Parent (Priority: P1)

A user toggles individual items and the parent checkbox reflects the aggregate state.

**Acceptance Scenarios**:

1. **Given** a section with all items checked, **When** user unchecks one item, **Then** section shows indeterminate state (minus icon)
2. **Given** a section with some items checked (indeterminate), **When** user unchecks remaining items, **Then** section shows unchecked state
3. **Given** a section with some items checked (indeterminate), **When** user checks all remaining items, **Then** section shows checked state

---

### User Story 3 - Visual Consistency with PDF (Priority: P1)

Items that are checked in the UI MUST appear in the PDF, and items unchecked MUST NOT appear.

**Acceptance Scenarios**:

1. **Given** 2 work experiences with first checked and second unchecked, **When** user generates PDF, **Then** only first experience appears in PDF
2. **Given** all items in a section unchecked, **When** user generates PDF, **Then** section does not appear in PDF
3. **Given** all items in a section checked, **When** user generates PDF, **Then** all items appear in PDF

---

### User Story 4 - State Persistence (Priority: P1)

User's checkbox selections persist across page reloads.

**Acceptance Scenarios**:

1. **Given** user has unchecked some sections, **When** user reloads the page, **Then** unchecked sections remain unchecked
2. **Given** user has toggled items to indeterminate state, **When** user reloads the page, **Then** indeterminate state is restored

---

### User Story 5 - Expand Without Re-render (Priority: P2)

Expanding/collapsing sections should not trigger PDF regeneration.

**Acceptance Scenarios**:

1. **Given** user is viewing PDF preview, **When** user expands a section to see items, **Then** PDF does NOT re-render (no flash, no network request)
