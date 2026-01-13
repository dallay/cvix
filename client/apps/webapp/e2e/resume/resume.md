# Resume Generator E2E Tests

E2E tests for the Resume Generator feature.

## Test Coverage

### @RESUME-E2E-001 - Navigate to resume editor and display empty form
**Priority**: Critical  
**Tags**: `@critical` `@e2e` `@resume`

**Description**:  
Verifies that the resume editor page loads correctly with an empty form.

**Steps**:
1. Navigate to `/resume`
2. Verify page title is visible
3. Verify save button is visible
4. Verify name and email inputs are empty

**Expected Result**:  
Page loads successfully with empty form fields.

---

### @RESUME-E2E-002 - Fill basic information and save resume
**Priority**: Critical  
**Tags**: `@critical` `@e2e` `@resume`

**Description**:  
Tests filling basic resume information and saving to storage.

**Steps**:
1. Navigate to resume editor
2. Fill name, label, email, phone, URL, and summary
3. Verify unsaved changes indicator appears
4. Click save button
5. Verify success notification
6. Verify unsaved changes indicator disappears
7. Verify "last saved" text appears

**Expected Result**:  
Resume data is saved successfully and UI updates accordingly.

---

### @RESUME-E2E-003 - Persist data after page refresh
**Priority**: High  
**Tags**: `@high` `@e2e` `@resume`

**Description**:  
Verifies that saved resume data persists across page refreshes.

**Steps**:
1. Navigate to resume editor
2. Fill name and email
3. Click save
4. Refresh the page
5. Verify name and email fields still contain saved values

**Expected Result**:  
Resume data is loaded from storage after page refresh.

---

### @RESUME-E2E-004 - Toggle preview panel visibility
**Priority**: Medium  
**Tags**: `@medium` `@e2e` `@resume`

**Description**:  
Tests the preview panel toggle functionality.

**Steps**:
1. Navigate to resume editor
2. Fill name field
3. Check initial preview visibility state
4. Click toggle preview button
5. Verify preview visibility changed

**Expected Result**:  
Preview panel visibility toggles correctly.

---

### @RESUME-E2E-005 - Export resume as JSON
**Priority**: High  
**Tags**: `@high` `@e2e` `@resume`

**Description**:  
Tests JSON export functionality.

**Steps**:
1. Navigate to resume editor
2. Fill name and email
3. Click download JSON button
4. Verify download initiated
5. Verify downloaded filename matches pattern `resume*.json`

**Expected Result**:  
Resume is exported as a valid JSON file.

---

### @RESUME-E2E-006 - Validate JSON and show validation panel
**Priority**: Medium  
**Tags**: `@medium` `@e2e` `@resume`

**Description**:  
Tests JSON validation functionality.

**Steps**:
1. Navigate to resume editor
2. Fill name field
3. Click validate JSON button
4. Verify validation panel appears

**Expected Result**:  
Validation panel shows validation results.

---

### @RESUME-E2E-007 - Reset form with confirmation
**Priority**: Medium  
**Tags**: `@medium` `@e2e` `@resume`

**Description**:  
Tests form reset with user confirmation.

**Steps**:
1. Navigate to resume editor
2. Fill name and email
3. Save resume
4. Click reset form button
5. Confirm reset in dialog
6. Verify success notification
7. Verify form fields are empty

**Expected Result**:  
Form is cleared and storage is reset after confirmation.

---

### @RESUME-E2E-008 - Cancel reset and keep data
**Priority**: Low  
**Tags**: `@low` `@e2e` `@resume`

**Description**:  
Tests canceling the reset operation.

**Steps**:
1. Navigate to resume editor
2. Fill name field
3. Click reset form button
4. Cancel reset in dialog
5. Verify name field still contains value

**Expected Result**:  
Data is preserved when reset is canceled.

---

### @RESUME-E2E-009 - Navigate to PDF generation page
**Priority**: Critical  
**Tags**: `@critical` `@e2e` `@resume`

**Description**:  
Tests navigation to the PDF generation page.

**Steps**:
1. Navigate to resume editor
2. Fill name field
3. Click "Generate PDF" button
4. Verify URL changes to `/resume/pdf`

**Expected Result**:  
User is navigated to the PDF generation page.

---

### @RESUME-E2E-010 - Detect unsaved changes on form modification
**Priority**: Medium  
**Tags**: `@medium` `@e2e` `@resume`

**Description**:  
Tests unsaved changes detection.

**Steps**:
1. Navigate to resume editor
2. Verify no unsaved changes indicator initially
3. Modify name field
4. Wait for reactive update
5. Verify unsaved changes indicator appears

**Expected Result**:  
Unsaved changes indicator appears when form is modified.

---

## Test Data

### Sample Resume
```json
{
  "basics": {
    "name": "John Doe",
    "label": "Senior Software Engineer",
    "email": "john.doe@example.com",
    "phone": "+1-555-0100",
    "url": "https://johndoe.dev",
    "summary": "Experienced software engineer with 10+ years building scalable web applications.",
    "location": {
      "address": "123 Tech Street",
      "city": "San Francisco",
      "region": "CA",
      "postalCode": "94105",
      "countryCode": "US"
    }
  }
}
```

## Page Objects

### ResumeEditorPage
Located at: `e2e/resume/resume-editor-page.ts`

**Key Methods**:
- `gotoResumeEditor()`: Navigate to resume editor
- `fillBasicInfo(data)`: Fill basic information section
- `fillLocation(data)`: Fill location section
- `saveResume()`: Save resume and wait for success
- `uploadJson(filePath, confirmReplace)`: Upload JSON file
- `downloadJson()`: Export resume as JSON
- `validateJson()`: Validate resume schema
- `resetForm()`: Reset form with confirmation
- `togglePreview()`: Toggle preview panel
- `navigateToPdfPage()`: Navigate to PDF page

## Running Tests

```bash
# All resume tests
pnpm test:e2e --grep "@resume"

# Critical resume tests only
pnpm test:e2e --grep "@resume.*@critical"

# Specific test by ID
pnpm test:e2e --grep "@RESUME-E2E-001"

# Run with UI
pnpm test:e2e:headed --grep "@resume"
```

## Notes

- Tests require authenticated session (mocked via helpers)
- Storage persistence is tested via localStorage
- JSON import tests require sample JSON files
- Preview toggle behavior may differ based on viewport size
- Unsaved changes detection relies on Vue reactivity (300ms wait)
