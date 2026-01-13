# E2E Tests: User Login Flow

**Suite ID:** `LOGIN-E2E`
**Feature:** User Authentication - Login (US2)

---

## Test Case: `LOGIN-E2E-001` - User can login with valid credentials

**Priority:** `critical`

**Tags:**

- type → @e2e
- feature → @login

**Description/Objective:** Verify users can successfully authenticate with valid email and password

**Preconditions:**

- User account exists in the system
- User is on the login page

### Flow Steps:

1. Navigate to /login
2. Enter valid email in the email field
3. Enter valid password in the password field
4. Click "Sign in" button

### Expected Result:

- User is redirected to /dashboard or /workspace
- Session is established

### Key verification points:

- URL changes to dashboard/workspace pattern
- No error messages displayed

---

## Test Case: `LOGIN-E2E-002` - User sees error for invalid credentials

**Priority:** `high`

**Tags:**

- type → @e2e
- feature → @login

**Description/Objective:** Verify proper error handling when credentials are incorrect

**Preconditions:**

- User is on the login page

### Flow Steps:

1. Navigate to /login
2. Enter incorrect email/password
3. Click "Sign in" button

### Expected Result:

- Error message "Invalid email or password" is displayed
- User remains on login page

### Key verification points:

- Error message is visible
- Form is not cleared (UX decision)

---

## Test Case: `LOGIN-E2E-003` - User sees rate limit message

**Priority:** `medium`

**Tags:**

- type → @e2e
- feature → @login

**Description/Objective:** Verify rate limiting protection displays appropriate message

**Preconditions:**

- API returns 429 (Too Many Requests)

### Flow Steps:

1. Navigate to /login
2. Attempt login (API returns 429)

### Expected Result:

- Message "Too many requests. Please try again later" is displayed

---

## Test Case: `LOGIN-E2E-004` - User sees error on network failure

**Priority:** `medium`

**Tags:**

- type → @e2e
- feature → @login

**Description/Objective:** Verify graceful handling of network errors

**Preconditions:**

- Network request to /api/auth/login fails

### Flow Steps:

1. Navigate to /login
2. Attempt login (network error occurs)

### Expected Result:

- Generic network error message is displayed
- Application does not crash

---

## Test Case: `LOGIN-E2E-005` - User sees validation error for invalid email

**Priority:** `medium`

**Tags:**

- type → @e2e
- feature → @login

**Description/Objective:** Verify client-side email validation

**Preconditions:**

- User is on login page

### Flow Steps:

1. Enter invalid email format (e.g., "invalid-email")
2. Tab out of field (blur)

### Expected Result:

- Validation error message about email format is displayed

---

## Test Case: `LOGIN-E2E-006` - User can navigate to registration

**Priority:** `low`

**Tags:**

- type → @e2e
- feature → @login

**Description/Objective:** Verify navigation link to registration works

**Preconditions:**

- User is on login page

### Flow Steps:

1. Click "Sign up" or "Register" link

### Expected Result:

- User is redirected to /register

---

## Test Case: `LOGIN-E2E-007` - XSS attack prevention

**Priority:** `critical`

**Tags:**

- type → @e2e
- feature → @login, @security

**Description/Objective:** Verify form inputs are sanitized against XSS

**Preconditions:**

- User is on login page

### Flow Steps:

1. Enter XSS payload in email field
2. Submit form

### Expected Result:

- No JavaScript executes (no alert dialogs)
- Form handles input safely

---

## Test Case: `LOGIN-E2E-008` - HTTPS enforcement

**Priority:** `critical`

**Tags:**

- type → @e2e
- feature → @login, @security

**Description/Objective:** Verify login page uses secure connection

**Preconditions:**

- SSL certificates configured
- Not running in CI/HTTP-only mode

### Flow Steps:

1. Navigate to login page
2. Check URL protocol

### Expected Result:

- URL uses https:// protocol

### Notes:

- Skipped in CI (runs on HTTP)
- Skipped when FORCE_HTTP=true
