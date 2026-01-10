# E2E Tests: User Logout Flow

**Suite ID:** `LOGOUT-E2E`
**Feature:** User Session Termination (US6)

---

## Test Case: `LOGOUT-E2E-001` - User can logout and session is cleared

**Priority:** `critical`

**Tags:**

- type → @e2e
- feature → @logout

**Description/Objective:** Verify users can successfully terminate their session

**Preconditions:**

- User is authenticated
- User is on dashboard or workspace page

### Flow Steps:

1. Locate logout button (in user menu or sidebar)
2. Click logout button
3. Verify redirect occurs
4. Check session storage is cleared

### Expected Result:

- User is redirected to /login or /
- Session storage is empty (length = 0)
- User cannot access authenticated routes

### Key verification points:

- URL changes to unauthenticated route
- sessionStorage.length === 0
- No authentication tokens remain

### Notes:

- Test requires login first (done in beforeEach)
- Logout button may be in user menu dropdown or sidebar
- Uses `.first()` selector since multiple logout buttons may exist
