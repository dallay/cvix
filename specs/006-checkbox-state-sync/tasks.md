# Tasks: Checkbox State Synchronization

**Status**: IN PROGRESS (Phase 6 added)
**Input**: Design documents from `/specs/006-checkbox-state-sync/`

## Completed Tasks

### Phase 1: Bug Investigation

- [x] T001 Analyze initial spec (concluded false positive - INCORRECT)
- [x] T002 Review user video evidence showing checkboxes not responding
- [x] T003 Identify root cause: API mismatch in Checkbox.vue wrapper
- [x] T004 Document affected components (SectionAccordionItem, ItemToggleList, etc.)

### Phase 2: Fix Implementation

- [x] T005 Modify Checkbox.vue to emit both `update:modelValue` and `update:checked`
- [x] T006 Run TypeScript check - no errors in Checkbox.vue
- [x] T007 Run unit tests - 21/21 passing
- [x] T008 Run build - successful
- [x] T009 Run lint check - no errors

### Phase 3: E2E Test Creation

- [x] T010 Create Page Object for Content Selection panel
- [x] T011 Create E2E test file for checkbox sync
- [x] T012 [US1] E2E test: section checkbox toggles checked -> unchecked
- [x] T013 [US1] E2E test: section checkbox toggles unchecked -> checked
- [x] T014 [US1] E2E test: indeterminate section click enables all children
- [x] T015 [US2] E2E test: unchecking one item shows indeterminate parent
- [x] T016 [US2] E2E test: unchecking all items shows unchecked parent
- [x] T017 [US2] E2E test: checking all items shows checked parent

### Phase 4: Documentation

- [x] T018 Update spec.md with root cause and solution
- [x] T019 Update plan.md with implementation details
- [x] T020 Update tasks.md (this file)

### Phase 5: Verification (First Round)

- [x] T021 Manual QA verification (user testing)
- [ ] T022 Run full E2E suite to ensure no regressions
- [ ] T023 Commit changes with proper message

### Phase 6: Follow-up Bug Fixes (From Manual QA)

- [x] T024 Fix localStorage persistence - Use stable "default" ID instead of random UUID
- [x] T025 Fix PDF re-render on expand - Separate `expandedSections` from `visibility` state
- [x] T026 Update unit tests for new architecture (21/21 passing)
- [x] T027 Update SectionTogglePanel to receive `expandedSections` prop
- [x] T028 Update ResumePdfPage to pass `expandedSections` prop
- [x] T029 Build verification - successful
- [ ] T030 Manual QA verification (second round)

## Summary

| Phase | Tasks | Completed |
|-------|-------|-----------|
| Investigation | 4 | 4 |
| Fix | 5 | 5 |
| E2E Tests | 8 | 8 |
| Documentation | 3 | 3 |
| Verification | 3 | 1 |
| Follow-up Fixes | 7 | 6 |
| **Total** | **30** | **27** |

## Key Learnings

1. **Initial analysis was wrong** - concluded "false positive" when there was a real bug
2. **Video evidence is invaluable** - user-provided video revealed the actual problem
3. **UI binding bugs are invisible to unit tests** - store tests passed but clicks didn't work
4. **E2E tests are essential** - added tests that actually click checkboxes
5. **Manual QA reveals integration issues** - two new bugs found during QA:
   - Persistence broken due to random UUID on each page load
   - PDF re-rendering when expanding sections (state coupling)
6. **Architecture matters for performance** - separating UI state (expanded) from domain state (visibility) prevents unnecessary PDF regenerations
