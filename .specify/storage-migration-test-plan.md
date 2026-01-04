# Storage Migration Manual Test Plan

## Test Environment Setup

### Prerequisites
1. Backend running: `cd server/engine && ./gradlew bootRun`
2. PostgreSQL running: `cd infra/postgresql && docker compose up -d`
3. Frontend running: `cd client && pnpm dev-web`
4. Test user credentials:
   - Email: `john.doe@profiletailors.com`
   - Password: `S3cr3tP@ssw0rd*123`

### Browser DevTools Commands

```javascript
// Clear all storage
localStorage.clear();
sessionStorage.clear();

// Check localStorage
console.log("localStorage:", localStorage.getItem('cvix_resume'));

// Check sessionStorage
console.log("sessionStorage:", sessionStorage.getItem('cvix_resume'));

// Check current storage preference
console.log("Storage Preference:", localStorage.getItem('storage_preference'));
```

---

## Test Cases

### ✅ Test 1: Local → Cloud with Migration

**Objective:** Verify data migrates from Local Storage to Cloud Storage

**Steps:**
1. Open browser, clear all storage (F12 → Console → run clear commands above)
2. Go to Settings → Storage
3. Select "Local Storage" (if not already selected)
4. Click "Apply Changes" (should switch immediately, no dialog)
5. Navigate to Resume Editor
6. Fill in basic info (name, email)
7. Wait for auto-save
8. Go back to Settings → Storage
9. Select "Cloud Storage"
10. Click "Apply Changes"

**Expected Results:**
- ✅ Dialog appears with warning
- ✅ Dialog shows "You have resume data stored in **Local Storage**"
- ✅ Dialog shows "Switching to **Cloud Storage** will make your current data inaccessible unless you migrate it"
- ✅ Three buttons visible: "Cancel", "Switch Without Migrating", "Migrate & Switch"

**Action:** Click "Migrate & Switch"

**Expected Results:**
- ✅ Button shows "Migrating..." with spinner
- ✅ Migration completes
- ✅ Success message appears: "✓ Storage changed successfully!"
- ✅ Navigate to Resume Editor
- ✅ Resume data is still visible
- ✅ Verify in Network tab: API call to `/api/v1/resumes/{id}` with PUT request

**Browser DevTools Verification:**
```javascript
// localStorage should be empty now (data migrated out)
console.log("localStorage after migration:", localStorage.getItem('cvix_resume'));

// Preference should be 'remote'
console.log("Current preference:", localStorage.getItem('storage_preference'));
```

---

### ✅ Test 2: Cloud → Local with Migration

**Objective:** Verify data migrates from Cloud Storage to Local Storage

**Prerequisites:**
- Logged in as test user
- Resume exists in Cloud Storage (from Test 1)

**Steps:**
1. Ensure Cloud Storage is active (Settings → Storage shows "Currently using Cloud Storage")
2. Navigate to Resume Editor, verify data loads from backend
3. Go to Settings → Storage
4. Select "Local Storage"
5. Click "Apply Changes"

**Expected Results:**
- ✅ Dialog appears with warning
- ✅ Dialog shows "You have resume data stored in **Cloud Storage**"
- ✅ Three buttons visible

**Action:** Click "Migrate & Switch"

**Expected Results:**
- ✅ Button shows "Migrating..." with spinner
- ✅ API call made to fetch resume from backend
- ✅ Data saved to localStorage
- ✅ Success message appears
- ✅ Navigate to Resume Editor
- ✅ Resume data still visible
- ✅ Changes save to localStorage (not backend)

**Browser DevTools Verification:**
```javascript
// localStorage should now have data
const data = localStorage.getItem('cvix_resume');
console.log("localStorage after migration:", JSON.parse(data));

// Preference should be 'local'
console.log("Current preference:", localStorage.getItem('storage_preference'));
```

---

### ✅ Test 3: Switch Without Migration

**Objective:** Verify user can switch without migrating data

**Prerequisites:**
- Resume in Local Storage

**Steps:**
1. Settings → Storage
2. Current: "Local Storage"
3. Select "Session Storage"
4. Click "Apply Changes"
5. Dialog appears

**Action:** Click "Switch Without Migrating"

**Expected Results:**
- ✅ Switch completes immediately
- ✅ Success message appears
- ✅ Navigate to Resume Editor
- ✅ Empty state shown (no resume)
- ✅ Go back to Settings → Storage
- ✅ Select "Local Storage" again
- ✅ Click "Apply Changes" (should switch without dialog since Session Storage is empty)
- ✅ Navigate to Resume Editor
- ✅ Original resume data reappears (was never deleted)

**Browser DevTools Verification:**
```javascript
// localStorage data still exists
console.log("localStorage (never deleted):", localStorage.getItem('cvix_resume'));

// sessionStorage is empty
console.log("sessionStorage (empty):", sessionStorage.getItem('cvix_resume'));
```

---

### ✅ Test 4: Cancel During Dialog

**Objective:** Verify cancel preserves current state

**Prerequisites:**
- Resume in Local Storage

**Steps:**
1. Settings → Storage
2. Current: "Local Storage"
3. Select "Cloud Storage"
4. Click "Apply Changes"
5. Dialog appears

**Action:** Click "Cancel"

**Expected Results:**
- ✅ Dialog closes
- ✅ Storage selector shows "Local Storage" selected (reverted)
- ✅ "Currently using Local Storage" message shows
- ✅ No changes applied
- ✅ Navigate to Resume Editor
- ✅ Resume data still accessible

---

### ✅ Test 5: Empty Storage Switch (No Dialog)

**Objective:** Verify switching with no data is seamless

**Steps:**
1. Clear all storage: `localStorage.clear(); sessionStorage.clear();`
2. Reload page
3. Settings → Storage
4. Current: "Local Storage" (default)
5. Select "Session Storage"
6. Click "Apply Changes"

**Expected Results:**
- ✅ NO dialog appears
- ✅ Switch happens immediately
- ✅ Button briefly shows "Checking..." then "Applying..."
- ✅ Success message appears
- ✅ "Currently using Session Storage" message shows

---

### ✅ Test 6: Network Error - Cloud Migration

**Objective:** Verify error handling when network fails

**Prerequisites:**
- Backend running
- Resume in Local Storage

**Steps:**
1. Settings → Storage
2. Select "Cloud Storage"
3. Click "Apply Changes"
4. Dialog appears
5. Open DevTools → Network tab
6. Enable "Offline" mode
7. Click "Migrate & Switch"

**Expected Results:**
- ✅ Button shows "Migrating..." with spinner
- ✅ After timeout, error message appears
- ✅ Error message: "Failed to change storage. Please try again."
- ✅ Storage preference NOT changed (still using Local Storage)
- ✅ User can retry after re-enabling network

**Recovery Steps:**
1. Disable "Offline" mode
2. Click "Apply Changes" again
3. Dialog appears again
4. Click "Migrate & Switch"
5. ✅ Migration succeeds

---

### ✅ Test 7: Back-and-Forth Switching

**Objective:** Verify multiple switches work correctly

**Steps:**
1. Start with Local Storage with data
2. Switch to Session Storage without migration
3. Switch to Cloud Storage without migration
4. Switch back to Local Storage (should show data again)
5. Switch to Cloud Storage with migration
6. Switch to Local Storage with migration
7. Verify data exists in both Local and Cloud

**Expected Results:**
- ✅ Each switch with data shows dialog
- ✅ Each switch without data (or choosing not to migrate) shows no data in new storage
- ✅ Data never lost if not migrated (still in original storage)
- ✅ Data duplicated if migrated to multiple storages

---

### ✅ Test 8: Unauthenticated User - Cloud Storage

**Objective:** Verify Cloud Storage requires authentication

**Steps:**
1. Logout (if logged in)
2. Settings → Storage should NOT show Cloud Storage option
3. Login
4. Settings → Storage should now show Cloud Storage option

**Expected Results:**
- ✅ Cloud Storage not available when logged out
- ✅ Cloud Storage available when logged in
- ✅ Switching to Cloud Storage works

---

### ✅ Test 9: Mobile Responsive

**Objective:** Verify UI works on mobile

**Steps:**
1. Open DevTools → Device Toolbar (Responsive mode)
2. Set to iPhone 14 Pro (390x844)
3. Navigate to Settings → Storage
4. Verify UI is readable and buttons accessible
5. Trigger dialog
6. Verify dialog is readable and buttons accessible

**Expected Results:**
- ✅ Storage options stack vertically on mobile
- ✅ Labels wrap properly
- ✅ Buttons full-width on mobile
- ✅ Dialog readable and scrollable if needed
- ✅ Touch targets large enough (min 44x44px)

---

### ✅ Test 10: Keyboard Navigation (Accessibility)

**Objective:** Verify keyboard-only navigation works

**Steps:**
1. Settings → Storage
2. Use Tab key to navigate between options
3. Use Arrow keys to select storage type
4. Tab to "Apply Changes" button
5. Press Enter to trigger dialog
6. Tab through dialog buttons
7. Press Enter on "Migrate & Switch"

**Expected Results:**
- ✅ Focus visible on all interactive elements
- ✅ Tab order is logical
- ✅ Enter key activates buttons
- ✅ Escape key closes dialog (if implemented)
- ✅ Focus trap in dialog (can't Tab outside)

---

## Test Results Template

```
Date: _______________
Tester: _______________
Browser: _______________ (Version: _______)
OS: _______________

| Test # | Test Name | Pass/Fail | Notes |
|--------|-----------|-----------|-------|
| 1 | Local → Cloud with Migration | ⬜ PASS ⬜ FAIL | |
| 2 | Cloud → Local with Migration | ⬜ PASS ⬜ FAIL | |
| 3 | Switch Without Migration | ⬜ PASS ⬜ FAIL | |
| 4 | Cancel During Dialog | ⬜ PASS ⬜ FAIL | |
| 5 | Empty Storage Switch | ⬜ PASS ⬜ FAIL | |
| 6 | Network Error Handling | ⬜ PASS ⬜ FAIL | |
| 7 | Back-and-Forth Switching | ⬜ PASS ⬜ FAIL | |
| 8 | Unauthenticated User | ⬜ PASS ⬜ FAIL | |
| 9 | Mobile Responsive | ⬜ PASS ⬜ FAIL | |
| 10 | Keyboard Navigation | ⬜ PASS ⬜ FAIL | |

Overall Status: ⬜ PASSED ⬜ FAILED ⬜ NEEDS INVESTIGATION

Critical Issues Found:
1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

Non-Critical Issues Found:
1. _______________________________________________
2. _______________________________________________
```

---

## Acceptance Criteria

✅ All 10 test cases must pass
✅ No console errors during normal operation
✅ Network errors handled gracefully
✅ Data never silently lost
✅ Dialog messaging clear and accurate
✅ Mobile responsive layout works
✅ Keyboard navigation functional
✅ Loading states visible and accurate

---

## Known Limitations (Not Bugs)

1. **Single Resume Only:** Current implementation assumes one resume per storage. Multi-resume support planned for Phase 3.
2. **No Storage Stats:** Dialog doesn't show how much data (size, date modified). Planned for Phase 2.
3. **No Auto-Sync:** Switching storage doesn't keep them in sync. Manual migration required each time. Auto-sync planned for Phase 4.
4. **Remote Storage Requires Auth:** Cloud Storage not available when logged out. This is by design.

---

## Bug Report Template

If you find a bug during testing, use this template:

```markdown
## Bug Report

**Test Case:** Test #X - [Test Name]

**Severity:** 🔴 Critical | 🟡 Medium | 🟢 Low

**Summary:** [Brief description]

**Steps to Reproduce:**
1. 
2. 
3. 

**Expected Behavior:**
[What should happen]

**Actual Behavior:**
[What actually happened]

**Browser:** [Chrome/Firefox/Safari] Version X.X
**OS:** [macOS/Windows/Linux]
**Screenshot:** [Attach if applicable]

**Console Errors:**
```
[Paste console errors here]
```

**Network Tab:**
[Any failed requests or unusual responses]

**Additional Context:**
[Any other relevant information]
```
