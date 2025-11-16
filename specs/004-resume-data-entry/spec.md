# Feature Specification: Resume Data Entry Screen

**Feature Branch**: `004-resume-data-entry`
**Created**: 2025-11-16
**Status**: Draft
**Input**: User description: "Resume Data Entry Screen with JSON-Resume integration, live preview, and PDF generation"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Resume Data Entry with Live Preview (Priority: P1)

As a job seeker, I want to input my resume information through a structured form while seeing a live preview of how it looks, so that I can confidently create a professional resume without worrying about formatting.

**Why this priority**: This is the core value proposition - enabling users to create resume content. Without this, the feature provides no value. The two-column layout with form and preview is the minimum viable product.

**Independent Test**: Can be fully tested by opening the data entry screen, filling in basic information (name, contact, work experience), and verifying the preview updates in real-time. Delivers immediate value by showing users their resume as they build it.

**Acceptance Scenarios**:

1. **Given** I am on the resume data entry screen, **When** I enter my name in the Basics section, **Then** the preview pane updates immediately to display my name
2. **Given** I have entered information in multiple sections, **When** I scroll through the form, **Then** the preview pane maintains its independent scroll position
3. **Given** I am editing the Work Experience section, **When** I add a new job entry with company name, position, dates, and highlights, **Then** the preview displays the new job in the appropriate format within 150ms
4. **Given** I have the form open, **When** I collapse/expand accordion sections in the Table of Contents, **Then** only the selected section's fields are visible while the preview shows all completed sections
5. **Given** I am filling out location information, **When** I enter address, city, region, country code, and postal code, **Then** the preview formats and displays the complete location appropriately

---

### User Story 2 - JSON Resume Import/Export (Priority: P1)

As a user with existing resume data, I want to upload my JSON Resume file and have all my information automatically populated in the form, so that I don't have to manually re-enter everything.

**Why this priority**: This is essential for users migrating from other tools or wanting to maintain their resume in a portable format. It also provides the foundation for data persistence and sharing. Without this, users can't easily save or transfer their work.

**Independent Test**: Can be fully tested by uploading a valid JSON Resume file and verifying all form fields populate correctly, then downloading the JSON and confirming it matches the schema. Delivers value by enabling data portability and backup.

**Acceptance Scenarios**:

1. **Given** I have a valid JSON Resume file, **When** I click "Upload JSON Resume" and select the file, **Then** all form sections populate with the corresponding data from the file
2. **Given** I have completed filling out the resume form, **When** I click "Download JSON Resume", **Then** a resume.json file downloads containing all my entered data in valid JSON Resume schema format
3. **Given** I upload a JSON file with validation errors, **When** the system processes the file, **Then** I see a grouped error report showing which sections have issues and what needs to be corrected
4. **Given** I have partially completed the form, **When** I upload a JSON Resume file, **Then** I am warned that current data will be replaced and can choose to cancel or proceed
5. **Given** I upload a JSON Resume with optional sections (publications, awards, volunteer), **When** the form loads, **Then** those optional sections appear populated with the imported data

---

### User Story 3 - Form Validation and Error Handling (Priority: P2)

As a user creating my resume, I want to receive clear feedback when I make mistakes or miss required information, so that I can ensure my resume data is complete and valid before generating the final document.

**Why this priority**: While users can technically enter data without validation, they need feedback to ensure quality and completeness. This prevents frustration when generating PDFs or sharing resumes with incomplete/invalid data.

**Independent Test**: Can be tested by intentionally entering invalid data (malformed email, invalid dates, missing required fields) and verifying inline validation messages appear. Delivers value by improving data quality and user confidence.

**Acceptance Scenarios**:

1. **Given** I am entering my email address, **When** I enter an invalid format (missing @, incorrect domain), **Then** an inline error message appears below the field indicating the correct format
2. **Given** I am entering work experience dates, **When** the end date is before the start date, **Then** a validation error appears and highlights both date fields
3. **Given** I have filled out multiple sections with errors, **When** I click "Validate JSON", **Then** a panel opens showing all validation errors grouped by section with jump-to links
4. **Given** I am filling out a required field, **When** I leave it blank and move to another field, **Then** the field displays a visual indicator marking it as required
5. **Given** the form has validation errors, **When** I fix all errors, **Then** the error indicators clear automatically and the "Validate JSON" panel shows success

---

### User Story 4 - Autosave and Data Persistence (Priority: P2)

As a user working on my resume, I want my progress to be automatically saved as I work, so that I don't lose my data if my browser crashes or I accidentally close the tab.

**Why this priority**: Essential for user trust and preventing data loss frustration. While not needed for initial data entry, it significantly improves the user experience for real-world usage where interruptions happen.

**Independent Test**: Can be tested by entering data, closing the browser without saving, reopening the application, and verifying all entered data is still present. Delivers value by providing peace of mind and resilience.

**Acceptance Scenarios**:

1. **Given** I am actively editing the resume form, **When** I make changes to any field, **Then** the data is automatically saved to browser storage (the user selected storage) within 2 seconds
2. **Given** I have unsaved changes, **When** I attempt to navigate away from the page, **Then** a warning dialog appears asking me to confirm before leaving
3. **Given** I return to the application after closing my browser, **When** the page loads, **Then** all my previously entered data is automatically restored from storage
4. **Given** autosave is active, **When** I successfully save, **Then** a subtle indicator shows "Last saved at [timestamp]"
5. **Given** I have autosaved data, **When** I click "Reset Form", **Then** I receive a confirmation warning before all data is cleared from storage

---

### User Story 5 - PDF Generation with Template Selection (Priority: P3)

As a user who has completed my resume data entry, I want to navigate to a dedicated PDF generation screen where I can select from different professional templates and generate a downloadable PDF, so that I can produce a polished resume document for job applications.

**Why this priority**: This is the final output that users need, but it's a separate concern from data entry. Users must first have their data entered (P1) before they can generate PDFs. Template selection adds value but is lower priority than getting basic PDF generation working.

**Independent Test**: Can be tested by loading pre-existing resume data, navigating to the PDF generation screen, selecting a template, and downloading the resulting PDF. Delivers value by providing the final resume document output.

**Acceptance Scenarios**:

1. **Given** I have completed my resume data entry, **When** I navigate to the PDF Generation screen, **Then** I see a preview of my resume using the default template
2. **Given** I am on the PDF Generation screen, **When** I select a different template from the dropdown, **Then** the preview updates to show my resume in the new template style within 500ms
3. **Given** I have selected my preferred template, **When** I click "Generate PDF", **Then** the system processes my resume data and creates a high-quality PDF
4. **Given** the PDF has been generated, **When** I click "Download PDF", **Then** a file named "resume.pdf" downloads to my device
5. **Given** I am viewing a template, **When** I adjust template parameters (color, font, spacing), **Then** the preview updates to reflect my customizations
6. **Given** I am on the PDF Generation screen, **When** I click "Back to Data Entry", **Then** I return to the data entry form with all my data intact

---

### User Story 6 - Preview Interaction and Navigation (Priority: P3)

As a user reviewing my resume, I want to click on a section in the preview pane and have the form automatically scroll to and highlight the corresponding input fields, so that I can quickly jump to the section I want to edit.

**Why this priority**: This is a quality-of-life enhancement that improves navigation efficiency but is not essential for the core data entry workflow. Users can still navigate via the Table of Contents.

**Independent Test**: Can be tested by clicking on different sections in the preview and verifying the form scrolls to the corresponding section and highlights the relevant fields. Delivers value by reducing scrolling and search time.

**Acceptance Scenarios**:

1. **Given** I am viewing the resume preview, **When** I click on my work experience section in the preview, **Then** the form scrolls to the Work Experience accordion and expands it
2. **Given** I click on my education in the preview, **When** the form scrolls, **Then** the corresponding education entry is highlighted with a subtle visual indicator
3. **Given** I have multiple work experiences, **When** I click on a specific job in the preview, **Then** the form scrolls to and highlights that specific job entry
4. **Given** I click on my contact information in the preview, **When** the form responds, **Then** the Basics section expands and the focus moves to the contact fields

---

### Edge Cases

- What happens when a JSON Resume file exceeds a reasonable size limit (e.g., >10MB due to large embedded images)?
- How does the system handle malformed JSON files that are almost valid but have syntax errors?
- What happens when a user has hundreds of work experiences or publications (performance of preview rendering)?
- How does the preview handle extremely long text entries (e.g., a 10,000-character summary)?
- What happens when a user uploads a JSON Resume file that uses an older version of the schema?
- How does the system handle concurrent editing across multiple browser tabs?
- What happens when browser storage is full and autosave fails?
- How does the preview render when mandatory fields are missing or empty?
- What happens when a user tries to generate a PDF with completely empty resume data?
- How does the system handle special characters, emojis, or non-Latin scripts in resume fields?
- What happens when image URLs in the JSON Resume file are broken or inaccessible?
- How does the form handle array fields with maximum length constraints (e.g., 100+ skills or highlights)?

## Requirements *(mandatory)*

### Functional Requirements

#### Data Entry Interface

- **FR-001**: System MUST provide a two-column split-view layout with independent scrolling for the form (left) and preview (right)
- **FR-002**: System MUST display a sticky Table of Contents in the left column listing all resume sections: Basics, Work, Education, Skills, Projects, Languages, Certificates, Publications, Awards, Volunteer, References
- **FR-003**: System MUST implement accordion-style collapsible sections for each resume category in the Table of Contents
- **FR-004**: System MUST render the live preview with a debounce of 100-150ms after any form field change
- **FR-005**: System MUST maintain independent scroll positions between the form column and preview column

#### Top Utility Bar

- **FR-006**: System MUST provide an "Upload JSON Resume" button that accepts .json file uploads
- **FR-007**: System MUST provide a "Download JSON Resume" button that exports the current form state as a JSON file
- **FR-008**: System MUST provide a "Reset Form" button that clears all form data after user confirmation
- **FR-009**: System MUST provide a "Validate JSON" button that opens a validation panel showing structured errors grouped by section

#### JSON Resume Schema Compliance

- **FR-010**: System MUST support all fields defined in the JSON Resume schema specification (<https://jsonresume.org/schema>)
- **FR-011**: System MUST validate uploaded JSON files against the official JSON Resume schema
- **FR-012**: System MUST auto-hydrate all form sections when a valid JSON Resume file is uploaded
- **FR-013**: System MUST display validation errors grouped by section when an invalid JSON file is uploaded
- **FR-014**: System MUST export resume data in valid JSON Resume format when "Download JSON Resume" is clicked

#### Form Sections - Basics

- **FR-015**: System MUST provide input fields for: name, label, image URL, email, phone, personal URL, and summary (multiline)
- **FR-016**: System MUST provide nested fields for location: address, city, region, countryCode, postalCode
- **FR-017**: System MUST support dynamic addition and removal of profile entries with fields: network, username, URL
- **FR-018**: System MUST validate email format in real-time
- **FR-019**: System MUST validate phone number format based on common international standards

#### Form Sections - Work Experience

- **FR-020**: System MUST allow users to add multiple work experience entries
- **FR-021**: System MUST provide fields for each work entry: company name, position, URL, start date, end date, summary (multiline)
- **FR-022**: System MUST support dynamic addition and removal of highlights (bullet points) for each work entry
- **FR-023**: System MUST validate that end date is not before start date
- **FR-024**: System MUST support date format YYYY-MM-DD for work experience dates

#### Form Sections - Education

- **FR-025**: System MUST allow users to add multiple education entries
- **FR-026**: System MUST provide fields for each education entry: institution, URL, area of study, study type, start date, end date, GPA
- **FR-027**: System MUST support dynamic addition and removal of course names for each education entry
- **FR-028**: System MUST validate that end date is not before start date

#### Form Sections - Skills

- **FR-029**: System MUST allow users to add multiple skill entries
- **FR-030**: System MUST provide fields for each skill: name, proficiency level, keywords (multiple)
- **FR-031**: System MUST support dynamic addition and removal of keywords for each skill

#### Form Sections - Projects

- **FR-032**: System MUST allow users to add multiple project entries
- **FR-033**: System MUST provide fields for each project: name, description, URL, start date, end date
- **FR-034**: System MUST support dynamic addition and removal of keywords and roles for each project

#### Form Sections - Languages

- **FR-035**: System MUST allow users to add multiple language entries
- **FR-036**: System MUST provide fields for each language: language name, fluency level

#### Form Sections - Certificates

- **FR-037**: System MUST allow users to add multiple certificate entries
- **FR-038**: System MUST provide fields for each certificate: name, issuer, date, URL

#### Form Sections - Optional Extended Schema

- **FR-039**: System MUST support optional sections for: publications, awards, volunteer work, references
- **FR-040**: System MUST only display optional sections when data is present or when explicitly added by the user
- **FR-041**: System MUST allow users to manually add optional sections via an "Add Section" interface

#### Validation

- **FR-042**: System MUST perform inline validation showing errors as users complete fields
- **FR-043**: System MUST perform global validation when "Validate JSON" is clicked
- **FR-044**: System MUST display validation errors in a structured panel with section grouping
- **FR-045**: System MUST provide jump-to-field links in the validation error panel
- **FR-046**: System MUST clear validation errors automatically when corrected

#### Data Persistence

- **FR-047**: System MUST automatically save form data to browser local storage or IndexedDB within 2 seconds of changes
- **FR-048**: System MUST display a "Last saved at [timestamp]" indicator after successful autosave
- **FR-049**: System MUST restore autosaved data when the user returns to the application
- **FR-050**: System MUST warn users before navigating away if there are unsaved changes
- **FR-051**: System MUST clear autosaved data when "Reset Form" is confirmed
- **FR-075**: System MUST perform background debounced server persistence of the full resume document for authenticated users: trigger after 2s of inactivity OR at least once every 10s during continuous editing, batching rapid changes into a single save
- **FR-076**: System MUST retry failed server persistence with exponential backoff (initial 1s, max 30s) and surface a non-blocking warning after 3 consecutive failures
- **FR-077**: System MUST record a server-synced timestamp and display it distinct from local autosave when the last remote save succeeds

#### PDF Generation Screen

- **FR-052**: System MUST provide a separate PDF Generation screen accessible from the data entry screen
- **FR-053**: System MUST load current resume data from the form or storage when entering the PDF Generation screen
- **FR-054**: System MUST display a template selector (dropdown or thumbnail grid) showing available resume templates
- **FR-055**: System MUST render a real-time preview of the resume in the selected template
- **FR-056**: System MUST update the PDF preview within 500ms when a different template is selected
- **FR-057**: System MUST provide a "Generate PDF" button that processes the resume data and creates a PDF
- **FR-058**: System MUST provide a "Download PDF" button that triggers download of the generated PDF file
- **FR-059**: System MUST provide a "Back to Data Entry" button that returns to the form without losing data
- **FR-060**: System MUST support optional template parameters including: color palette, font family, spacing/density
- **FR-061**: System MUST update the preview when template parameters are modified

#### Template Metadata & Validation

- **FR-078**: System MUST fetch template metadata from server endpoint `/api/templates` providing `{ id, name, version, paramsSchema }` on entering the PDF Generation screen (or first template interaction)
- **FR-079**: System MUST validate user-specified template parameter values against `paramsSchema` before applying them to preview or sending them for PDF generation
- **FR-080**: System MUST cache fetched template metadata for the active session and refresh it only if `ETag` or `version` changes

#### Preview Interaction

- **FR-062**: System MUST support clicking on preview sections to scroll and highlight corresponding form fields
- **FR-063**: System MUST expand accordion sections when clicked from the preview
- **FR-064**: System MUST provide visual highlighting of the active form section when navigated from preview

#### Keyboard Shortcuts

- **FR-065**: System MUST support Cmd/Ctrl+S to trigger "Download JSON Resume"
- **FR-066**: System MUST support Cmd/Ctrl+Shift+I to trigger "Upload JSON Resume" file picker

#### Performance

- **FR-067**: System MUST render preview updates within 150ms of form changes for optimal user experience
- **FR-068**: System MUST handle resume data with up to 50 work experiences without performance degradation
- **FR-069**: System MUST handle text fields with up to 10,000 characters without lag

#### Error Handling

- **FR-070**: System MUST display user-friendly error messages when JSON file upload fails
- **FR-071**: System MUST handle oversized JSON files (>10MB) with appropriate error messaging
- **FR-072**: System MUST gracefully handle malformed JSON with syntax error indicators
- **FR-073**: System MUST handle browser storage quota exceeded errors with user notification
- **FR-074**: System MUST handle broken image URLs in the resume data without breaking the preview

### Key Entities

- **Resume**: The complete data structure representing a user's resume, conforming to JSON Resume schema with sections for basics, work, education, skills, projects, languages, certificates, and optional extended data
- **Basics**: Personal information including name, contact details, location, professional summary, and social media profiles
- **Work Experience**: Individual job entries with company, role, dates, responsibilities summary, and achievement highlights
- **Education**: Academic credentials including institution, degree/study type, field of study, dates, GPA, and relevant courses
- **Skill**: Individual competency with name, proficiency level, and related keywords/technologies
- **Project**: Portfolio item with name, description, URL, dates, roles, and technology keywords
- **Language**: Language proficiency entry with language name and fluency level
- **Certificate**: Professional certification with name, issuing organization, date, and verification URL
- **Publication**: Published work entry including title, publisher, release date, URL, and summary
- **Award**: Recognition or achievement with title, awarding organization, date, and description
- **Volunteer**: Volunteer experience with organization, role, dates, summary, and highlights
- **Reference**: Professional reference with name, reference type, and contact information
- **Template**: PDF layout design with configurable parameters like color scheme, typography, and spacing
- **ValidationError**: Error entry indicating invalid or missing data with section reference, field name, and error description

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can complete basic resume data entry (name, contact, one work experience, one education entry) and see live preview updates in under 3 minutes
- **SC-002**: Users can upload a valid JSON Resume file and have all form fields populated within 2 seconds
- **SC-003**: Users can export their resume data as a valid JSON Resume file that passes schema validation 100% of the time
- **SC-004**: Preview updates render within 150ms of form field changes for entries up to 5000 characters
- **SC-005**: System successfully handles resume data with up to 50 work experiences without preview lag exceeding 300ms
- **SC-006**: 95% of form validation errors provide clear, actionable guidance that users can resolve without external help
- **SC-007**: Autosave successfully preserves user data through browser closure and restoration in 99% of cases
- **SC-008**: Users can navigate between data entry and PDF generation screens without data loss in 100% of attempts
- **SC-009**: Users can generate and download a PDF from their resume data in under 5 seconds
- **SC-010**: Template switching in PDF generation updates preview within 500ms
- **SC-011**: System correctly imports and exports JSON Resume files with all optional sections (publications, awards, volunteer, references) with 100% accuracy
- **SC-012**: Users successfully complete their first resume using the tool with a task completion rate of 90% or higher
- **SC-013**: Click-to-edit from preview to form successfully navigates and highlights the correct section 95% of the time
- **SC-014**: System handles malformed JSON uploads gracefully and provides clear error messages identifying the issues in 100% of cases
- **SC-015**: Server persistence round-trip (request to success) p95 < 400ms for documents up to 100KB JSON
- **SC-016**: 99% of editing sessions persist the latest resume state to the server within 10s of the user's last keystroke
- **SC-017**: Template metadata fetch completes within 800ms (p95) and includes version + schema for all templates

## Clarifications

### Session 2025-11-16

- Q: What server persistence strategy should be used (background debounced vs manual save etc.)? → A: Background debounced server sync after 2s idle or every ≤10s while typing
- Q: How should template metadata be provided to the client? → A: Server endpoint `/api/templates` returning array of `{id,name,version,paramsSchema}`
