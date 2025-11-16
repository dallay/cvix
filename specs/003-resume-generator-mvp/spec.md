# Feature Specification: Resume Generator MVP

**Feature Branch**: `003-resume-generator-mvp`
**Created**: October 31, 2025
**Status**: Draft
**Input**: User description: "Resume Generator MVP - Technical Specification v1.0"

## Clarifications

### Session 2025-11-15

- Q: The initial spec called for session-only storage, but the implementation supports persistent local storage. Should the spec be updated? → A: Yes, the storage strategy was intentionally evolved during implementation to improve user experience. The initial assumption of session-only storage proved too restrictive for users who wanted to save drafts. The current implementation, which offers user-selectable storage options (Session, Local, IndexedDB), is now the source of truth. The specification has been updated to reflect this flexible, client-side persistence model.

### Session 2025-11-01

- Q: Should we reuse existing shared value objects (Email, Name) from `com.loomify.common.domain.vo`? → A: Reuse Email only - The shared Email VO provides RFC-compliant validation (320 char limit). The resume-specific name field renamed to `FullName` to avoid confusion, as JSON Resume uses unstructured single string (e.g., "John Doe") while shared Name VO expects structured firstName + lastName for user profiles.
- Q: How should API versioning be implemented in the REST contract? → A: Header-based versioning - Use `API-Version: v1` header (not path-based `/v1`) to align with project standards. Added required `API-Version` header parameter and global `security: [bearerAuth]` requirement to OpenAPI spec to comply with CKV_OPENAPI_4 security policy.

### Session 2025-10-31

- Q: What is the resume output format, and should the product claim ATS optimization? → A: PDF only - Remove "ATS-optimized" claims and position as "professional print-ready resumes"
- Q: How should a single template handle diverse industries and roles? → A: Smart content layout - Single template that adapts section emphasis based on content (skills vs experience focus)
- Q: What are the specific field length limits and request size limits? → A: Industry standard - Names/titles: 100 chars, Descriptions: 500 chars, Skills: 50 chars, Payload: 100KB
- Q: What date format should be used for work experience? → A: Month/Year with "Present" option, localized (English: "Jan 2020 - Present", Spanish: "Ene 2020 - Presente")
- Q: What happens when a user exceeds the rate limit (10 requests/minute)? → A: HTTP 429 with retry timing - Clear error message with countdown/timestamp for when to retry

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Basic Resume Generation (Priority: P1)

A professional user needs to create a high-quality, professionally formatted resume without technical knowledge. They fill out a web form with their information and receive a print-ready PDF within seconds.

**Why this priority**: This is the core value proposition - democratizing access to professional resume generation. Without this, there is no product.

**Independent Test**: Can be fully tested by filling out the web form with sample data, clicking generate, and verifying a valid PDF is downloaded. Delivers immediate value: a professional resume without technical knowledge.

**Acceptance Scenarios**:

1. **Given** a user visits the resume generator landing page, **When** they click "Create Resume", **Then** they see a form with fields for personal information, experience, education, and skills
2. **Given** a user has filled all required form fields with valid data, **When** they click "Generate PDF", **Then** the system generates and returns a downloadable PDF within 8 seconds
3. **Given** a user has successfully generated a resume, **When** they open the PDF, **Then** the resume displays professional typography, proper formatting, and all entered information accurately
4. **Given** a user is filling out the form, **When** they navigate away and return, **Then** their progress is preserved for the current session

---

### User Story 2 - Form Validation and Error Handling (Priority: P1)

A user filling out the resume form makes mistakes or skips required fields. The system provides clear, immediate feedback to help them correct issues before submission.

**Why this priority**: Essential for user experience and preventing failed generations. A 75% completion rate depends on this.

**Independent Test**: Can be tested by intentionally submitting incomplete or invalid form data and verifying appropriate error messages appear. Delivers value by preventing wasted time on failed generations.

**Acceptance Scenarios**:

1. **Given** a user attempts to generate a resume, **When** they have left required fields empty, **Then** the system displays field-specific error messages indicating which fields need completion
2. **Given** a user enters invalid data (e.g., non-email format in email field), **When** they move to the next field, **Then** the system validates the field and shows an appropriate error message
3. **Given** a user has corrected validation errors, **When** they resubmit the form, **Then** error messages disappear and generation proceeds
4. **Given** the form is being validated, **When** validation occurs, **Then** it happens without blocking the user interface

---

### User Story 3 - Real-time Preview (Priority: P2)

A user wants to see how their resume will look before generating the final PDF. They can preview formatting and layout as they fill out the form to make adjustments.

**Why this priority**: Significantly improves user confidence and reduces iteration cycles, but the core value (PDF generation) works without it.

**Independent Test**: Can be tested by filling form fields and observing a live preview panel update. Delivers value by allowing users to iterate on content before committing to PDF generation.

**Acceptance Scenarios**:

1. **Given** a user is filling out the form, **When** they complete a section, **Then** the preview updates to show how that information will appear in the final resume
2. **Given** a user modifies existing form data, **When** they make changes, **Then** the preview updates within 500ms to reflect the changes
3. **Given** a user is viewing the preview, **When** they are on a mobile device, **Then** the preview is responsive and readable

---

### User Story 4 - Mobile-Responsive Experience (Priority: P2)

A user accessing the resume generator from a mobile device can complete the entire form and generate their resume using a phone or tablet interface.

**Why this priority**: Expands accessibility and user base, but desktop experience covers the core use case.

**Independent Test**: Can be tested by accessing the application on a 375px viewport and completing the full form submission. Delivers value by enabling resume creation from any device.

**Acceptance Scenarios**:

1. **Given** a user accesses the site from a mobile device, **When** they view the form, **Then** all form fields are properly sized and accessible
2. **Given** a user is filling the form on mobile, **When** they interact with form fields, **Then** the keyboard displays appropriate input types (email keyboard for email fields, etc.)
3. **Given** a user completes the form on mobile, **When** they generate the PDF, **Then** the download process works correctly on their mobile browser

---

### User Story 5 - Error Recovery and Retry (Priority: P3)

When PDF generation fails due to system issues, the user receives clear information about what went wrong and can easily retry without re-entering all their information.

**Why this priority**: Improves robustness but should rarely be needed if the system is stable. The <3% error rate target means this affects few users.

**Independent Test**: Can be tested by simulating system errors (timeout, resource limits) and verifying error messages and retry functionality work. Delivers value by preventing data loss on errors.

**Acceptance Scenarios**:

1. **Given** the PDF generation times out, **When** the error occurs, **Then** the user sees a clear message explaining the timeout and offering a "Retry" button
2. **Given** compilation fails due to template issues, **When** the error occurs, **Then** the user sees a helpful error message without technical details
3. **Given** a user clicks "Retry" after an error, **When** the retry is initiated, **Then** the form retains all previously entered data

---

### Edge Cases

- What happens when a user enters extremely long text that could break formatting (e.g., 500-character job description)?
- How does the system handle special characters or non-ASCII characters in names (e.g., accented characters, Chinese characters)?
- What happens if a user attempts to generate multiple resumes simultaneously from the same browser?
- How does the system respond when document generation resources are exhausted (all execution environments in use)?
- What happens if the document compilation produces warnings but still generates a valid PDF?
- How does the system handle users attempting to exploit document generation vulnerabilities through malicious input?

---

### User Story 6 - Storage Preference Selection (Priority: P3)

A user wants to save their resume draft and continue working on it later. They can choose a persistent storage option to save their data across browser sessions.

**Why this priority**: Improves user convenience for multi-session editing but is not essential for the core value of generating a resume in a single session.

**Independent Test**: Can be tested by selecting a persistent storage option (e.g., Local Storage), entering data, closing and reopening the browser, and verifying the data is restored. Delivers value by preventing data loss and allowing users to complete their resume over multiple sessions.

**Acceptance Scenarios**:

1. **Given** a user navigates to the application settings, **When** they view the storage options, **Then** they see a choice between Session Storage (default, temporary), Local Storage (persistent), and IndexedDB (persistent, for large data).
2. **Given** a user has entered data into the resume form, **When** they select a new persistent storage option and confirm, **Then** their existing data is automatically migrated to the new storage location.
3. **Given** a user has selected a persistent storage option, **When** they close and reopen their browser, **Then** their resume data is automatically loaded from the selected storage.
4. **Given** a user switches back to Session Storage, **When** they confirm the change, **Then** their data is migrated to the session, and a warning is displayed that it will be cleared when the session ends.

---

## Requirements *(mandatory)*

### Functional Requirements

#### Form Input & Validation

- **FR-001**: System MUST provide a web-based form with fields for personal information (name, email, phone, location), work experience (company, title, dates, description), education (institution, degree, dates), and skills
- **FR-002**: System MUST validate all form inputs in real-time to ensure data integrity and prevent malicious input
- **FR-003**: System MUST block potentially harmful user inputs that could compromise system security or document generation
- **FR-004**: System MUST enforce field length limits to ensure proper document formatting and system performance: names/titles (100 characters), descriptions (500 characters), skills (50 characters per item)
- **FR-005**: System MUST provide clear, field-specific error messages when validation fails
- **FR-006**: System MUST preserve form data according to the user's selected storage preference (Session, Local, or IndexedDB) to prevent data loss on navigation or session termination

#### PDF Generation

- **FR-007**: System MUST generate professionally formatted PDF resumes from validated user input within 8 seconds (95th percentile)
- **FR-008**: System MUST render resumes using a template-based approach that ensures consistent, professional formatting with intelligent content adaptation (section ordering and emphasis based on content presence)
- **FR-009**: System MUST compile documents in an isolated, secure environment to prevent security vulnerabilities
- **FR-010**: System MUST enforce resource limits on document generation to ensure system stability and fair resource allocation
- **FR-011**: System MUST automatically clean up temporary files after PDF generation or error to prevent storage issues
- **FR-012**: System MUST deliver the generated PDF directly to the user without long-term storage

#### Security & Rate Limiting

- **FR-013**: System MUST enforce rate limiting of 10 requests per minute per user to prevent abuse, returning HTTP 429 status with clear error message and retry-after timing when limit exceeded
- **FR-014**: System MUST execute all document compilation in an isolated environment with restricted file system access
- **FR-015**: System MUST reject requests exceeding 100KB payload size to prevent resource exhaustion
- **FR-016**: System MUST log all compilation attempts with anonymized identifiers for security auditing
- **FR-017**: System MUST enforce secure connections and modern encryption standards for all communications

#### Error Handling

- **FR-018**: System MUST return clear validation error messages when user input is invalid, indicating which fields need correction
- **FR-019**: System MUST return user-friendly error messages when document compilation fails, without exposing technical details
- **FR-020**: System MUST provide helpful guidance and retry options when generation times out
- **FR-021**: System MUST log unexpected errors for investigation while showing users a generic error message
- **FR-022**: System MUST never expose internal system details, file paths, or technical implementation in error messages
- **FR-023**: System MUST return HTTP 429 with retry-after timing (in seconds) when rate limit is exceeded, with localized error message

#### Performance & Reliability

- **FR-024**: System MUST handle 50 concurrent users without performance degradation during MVP phase
- **FR-025**: System MUST maintain 99.5% uptime as measured by external monitoring
- **FR-026**: System MUST respond to API requests within 200ms (95th percentile) excluding document generation time
- **FR-027**: System MUST maintain error rate below 3% of all generation attempts

#### User Interface

- **FR-028**: System MUST provide a responsive interface that works on mobile devices and small screens
- **FR-029**: System MUST meet web accessibility standards including proper form labels, keyboard navigation, and screen reader support
- **FR-030**: System MUST provide visual feedback during PDF generation to inform users of progress
- **FR-031**: System MUST support all modern web browsers released in the past 2 years

### Key Entities

- **Resume Data**: Represents the complete user-submitted information including personal details, work experiences (list), education entries (list), and skills (list). Each work experience contains company, job title, employment dates (Month/Year format with "Present" option, localized), and description. Each education entry contains institution, degree, and dates (Month/Year format, localized). Output format is PDF.
- **Generation Request**: Represents a single PDF generation attempt including validated resume data, timestamp, request source (IP hash), processing status, and user's language preference.
- **Generated Document**: Represents the output PDF with metadata including generation timestamp, file size, success status, and language used.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: System generates 500 successful PDF documents within the first 30 days of launch
- **SC-002**: Compilation error rate remains below 3% of all generation attempts
- **SC-003**: 75% of users who start the form complete it and download a PDF (completion rate)
- **SC-004**: Users complete the form and receive their PDF in under 60 seconds total (form fill + generation)
- **SC-005**: System successfully handles 50 concurrent users without timeouts or errors
- **SC-006**: API response latency (excluding PDF generation) stays below 500ms for 95% of requests
- **SC-007**: PDF generation completes within 8 seconds for 95% of requests
- **SC-008**: System maintains 99.5% uptime over any 30-day period
- **SC-009**: 70% or higher user satisfaction rating from post-download survey
- **SC-010**: Zero successful LaTeX injection attacks or security breaches detected in audit logs

## Assumptions *(mandatory)*

- Users have basic computer literacy and can fill out web forms
- Users will access the service during normal business hours with peak load of 50 concurrent users
- The application provides user-selectable storage options (session, local, IndexedDB) to balance privacy with the convenience of long-term draft saving. Session storage is the default.
- Users understand that generated resumes may need manual adjustments for specific job applications
- The service will initially target English-language resumes with support for international character sets
- The service will support Spanish language as a secondary language option for the MVP
- Infrastructure will be hosted on reliable cloud services with appropriate redundancy
- The default resume template will follow modern design principles and be optimized for professional appearance and readability
- Users will primarily access the service from desktop browsers, with mobile as a secondary use case
- PDF downloads will be initiated immediately upon generation
- The service will initially serve North American and European markets
- Users may need to convert PDFs to other formats if required by specific job application systems

## Dependencies *(include if applicable)*

### External Services

- **Content Delivery Network**: Required for fast delivery of web assets globally
- **Document Generation Environment**: Required for secure, isolated PDF compilation
- **External Monitoring Service**: Required for uptime monitoring and alerting

### Technical Capabilities Required

- Web application framework for building responsive user interfaces
- Backend service framework for handling API requests and business logic
- Document templating system for consistent resume formatting
- PDF generation capability from structured templates
- Containerization platform for isolated execution environments

### Development Requirements

- Automated testing infrastructure for unit, integration, and end-to-end tests
- Metrics collection and visualization for monitoring system health
- Continuous integration and deployment pipelines

### Constraints

- User data is stored client-side according to the user's selected preference (session, local, or IndexedDB storage), with no server-side data persistence.
- Must use lightweight, efficient containerization to minimize resource costs
- Must comply with existing organizational security practices and industry standards
- Must integrate with existing infrastructure and deployment processes

## Out of Scope *(include if helpful to set boundaries)*

- **User Accounts**: No authentication, user accounts, or saved resume storage (session-only processing)
- **Template Customization**: Users cannot select or customize templates; single adaptive template that intelligently adjusts based on content
- **Paid Features**: No payment processing, premium tiers, or monetization features
- **Email Delivery**: PDFs are not emailed; download-only in MVP
- **Resume Editing**: Cannot edit previously generated resumes (one-time generation only)
- **Multi-language Support Beyond English/Spanish**: Only English and Spanish interfaces and content in MVP
- **Advanced Formatting**: No custom fonts, colors, or layout modifications
- **Import from LinkedIn**: No integration with external profile sources
- **Version History**: No tracking of multiple generations or version comparison
- **Collaborative Editing**: No sharing or real-time collaboration features
- **Mobile Apps**: Web-only interface (responsive design, no native apps)
- **Analytics Dashboard**: No user-facing analytics or generation statistics
- **Content Suggestions**: No AI-powered writing assistance or content recommendations
- **Cover Letter Generation**: Resumes only; no cover letters or other document types
- **Batch Processing**: One resume at a time; no bulk generation
- **API Access**: No public API or programmatic access
