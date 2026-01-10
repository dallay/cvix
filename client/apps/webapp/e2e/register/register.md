# E2E Tests: User Registration Flow

**Suite ID:** `REGISTER-E2E`
**Feature:** User Account Creation (US1)

---

## Test Case: `REGISTER-E2E-001` - User can register with valid data

**Priority:** `critical`

**Tags:**

- type → @e2e
- feature → @register

**Description/Objective:** Verify users can successfully create a new account

**Preconditions:**

- Email is not already registered
- User is on the registration page

### Flow Steps:

1. Navigate to /register
2. Enter valid email
3. Enter first name and last name
4. Enter valid password (meets complexity requirements)
5. Confirm password
6. Accept terms and conditions
7. Click "Create account" button

### Expected Result:

- User is redirected to /dashboard, /workspace, or /login
- Account is created

### Key verification points:

- URL changes to expected post-registration route
- No error messages displayed

---

## Test Case: `REGISTER-E2E-002` - User sees error for existing email

**Priority:** `high`

**Tags:**

- type → @e2e
- feature → @register

**Description/Objective:** Verify proper error when email is already registered

**Preconditions:**

- Email is already associated with an account

### Flow Steps:

1. Navigate to /register
2. Enter existing email
3. Complete form and submit

### Expected Result:

- Error message "An account with this email already exists" is displayed
- User remains on registration page

---

## Test Case: `REGISTER-E2E-003` - User sees error for weak password

**Priority:** `medium`

**Tags:**

- type → @e2e
- feature → @register

**Description/Objective:** Verify client-side password strength validation

**Preconditions:**

- User is on registration page

### Flow Steps:

1. Enter weak password (e.g., "weak")
2. Tab out of field (blur)

### Expected Result:

- Validation error about password requirements is displayed
- Minimum length, uppercase, numbers, special chars as configured

---

## Test Case: `REGISTER-E2E-004` - User sees error for mismatched passwords

**Priority:** `medium`

**Tags:**

- type → @e2e
- feature → @register

**Description/Objective:** Verify password confirmation validation

**Preconditions:**

- User is on registration page

### Flow Steps:

1. Enter password in password field
2. Enter different password in confirm field
3. Tab out of confirm field (blur)

### Expected Result:

- Error message "Passwords don't match" is displayed

---

## Test Case: `REGISTER-E2E-005` - User sees required field errors

**Priority:** `medium`

**Tags:**

- type → @e2e
- feature → @register

**Description/Objective:** Verify required field validation

**Preconditions:**

- User is on registration page

### Flow Steps:

1. Click "Create account" without filling any fields

### Expected Result:

- Required field errors are displayed
- Form is not submitted

---

## Test Case: `REGISTER-E2E-006` - User can navigate to login

**Priority:** `low`

**Tags:**

- type → @e2e
- feature → @register

**Description/Objective:** Verify navigation link to login works

**Preconditions:**

- User is on registration page

### Flow Steps:

1. Click "Sign in" or "Log in" link

### Expected Result:

- User is redirected to /login
