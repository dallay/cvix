# Quickstart: Checkbox State Sync Verification

**Feature**: 006-checkbox-state-sync
**Date**: 2026-01-11
**Purpose**: Manual QA checklist to verify checkbox synchronization behavior

## Prerequisites

1. Local development server running (`make dev-web`)
2. Browser DevTools open (to clear localStorage if needed)
3. Sample resume with multiple work experiences loaded

## Test Setup

1. Navigate to the Resume Generator PDF page
2. Locate the **Content Selection** panel on the right side
3. Ensure the "Work" section has at least 2 items

## Test Cases

### TC-001: Parent → Children (Select All)

**Steps**:
1. Expand the "Work" section to see child items
2. Uncheck all child items manually (one by one)
3. Click the parent "Work" checkbox

**Expected**:
- [ ] All child items become checked
- [ ] Parent checkbox shows checkmark (✓)
- [ ] Counter badge shows "X/X" (all visible)

**Pass/Fail**: ____

---

### TC-002: Parent → Children (Clear All)

**Steps**:
1. Ensure all "Work" items are checked
2. Click the parent "Work" checkbox

**Expected**:
- [ ] All child items become unchecked
- [ ] Parent checkbox shows empty (□)
- [ ] Counter badge shows "0/X"

**Pass/Fail**: ____

---

### TC-003: Children → Parent (Partial Selection)

**Steps**:
1. Start with all "Work" items checked
2. Uncheck ONE item (not all)

**Expected**:
- [ ] Parent checkbox shows indeterminate (−)
- [ ] Counter badge shows "Y/X" where Y < X
- [ ] Remaining checked items stay checked

**Pass/Fail**: ____

---

### TC-004: Children → Parent (Auto-Disable)

**Steps**:
1. Start with some "Work" items checked (indeterminate state)
2. Uncheck ALL remaining items

**Expected**:
- [ ] Parent checkbox shows unchecked (□)
- [ ] Section collapses automatically
- [ ] Counter badge shows "0/X"

**Pass/Fail**: ____

---

### TC-005: Indeterminate → Select All

**Steps**:
1. Create indeterminate state (some items checked)
2. Click the parent "Work" checkbox

**Expected**:
- [ ] ALL items become checked (not unchecked)
- [ ] Parent checkbox shows checkmark (✓)
- [ ] Counter badge shows "X/X"

**Pass/Fail**: ____

---

### TC-006: PDF Sync Verification

**Steps**:
1. Check only 1 of 2 "Work" experiences
2. Generate/preview the PDF

**Expected**:
- [ ] ONLY the checked experience appears in PDF
- [ ] The unchecked experience does NOT appear
- [ ] Section header appears (since 1 item visible)

**Pass/Fail**: ____

---

### TC-007: Page Reload Persistence

**Steps**:
1. Set specific checkbox states (some checked, some not)
2. Reload the page (F5)

**Expected**:
- [ ] All checkbox states preserved
- [ ] Indeterminate states preserved
- [ ] Expanded/collapsed states preserved

**Pass/Fail**: ____

---

## Edge Cases

### TC-008: Empty Section

**Steps**:
1. Find a section with no data (e.g., "Awards" if empty)

**Expected**:
- [ ] Checkbox is disabled (grayed out)
- [ ] Tooltip shows "No data available"
- [ ] Cannot be checked or expanded

**Pass/Fail**: ____

---

### TC-009: Personal Details (Cannot Disable)

**Steps**:
1. Try to uncheck the "Personal Details" section checkbox

**Expected**:
- [ ] Checkbox remains checked (cannot be disabled)
- [ ] Individual fields within can still be toggled

**Pass/Fail**: ____

---

## Summary

| Test Case | Status |
|-----------|--------|
| TC-001 | |
| TC-002 | |
| TC-003 | |
| TC-004 | |
| TC-005 | |
| TC-006 | |
| TC-007 | |
| TC-008 | |
| TC-009 | |

**Overall Result**: ____

**Tester**: ____
**Date**: ____

## Notes

If any test fails, document:
1. Exact steps to reproduce
2. Expected vs actual behavior
3. Browser and version
4. Screenshot if possible

Create a bug report with tag `006-checkbox-state-sync` for tracking.
