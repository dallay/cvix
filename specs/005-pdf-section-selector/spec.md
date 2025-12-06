# Feature Specification: PDF Section Selector

**Feature Branch**: `005-pdf-section-selector`
**Created**: December 6, 2025
**Status**: Draft
**Input**: User description: "Redesign PDF Generator Screen to allow users to select which sections to include in the final PDF resume"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Toggle Resume Sections for PDF Export (Priority: P1)

As a user preparing my resume for a specific job application, I want to select which sections appear in my final PDF so that I can tailor my resume content to match the job requirements without editing my source data.

**Why this priority**: This is the core feature request. Users need the ability to customize which sections appear in their exported PDF to create targeted resumes for different opportunities. This delivers immediate value by enabling resume customization without data loss.

**Independent Test**: Can be fully tested by toggling section visibility and verifying the PDF output includes only selected sections. Delivers the core value of customizable resume exports.

**Acceptance Scenarios**:

1. **Given** I am on the Resume Preview screen with my complete resume data, **When** I view the "Visible Sections" area, **Then** I see toggleable pills for each available section (Personal Details, Work Experience, Education, Skills, Projects, Certifications)

2. **Given** all sections are visible by default, **When** I click on an active (purple) section pill, **Then** the section becomes inactive (white/gray) and immediately disappears from the live preview

3. **Given** a section is inactive, **When** I click on the inactive section pill, **Then** the section becomes active (purple) and immediately appears in the live preview

4. **Given** I have selected specific sections to be visible, **When** I click "Download PDF", **Then** the generated PDF contains only the sections I selected

5. **Given** I have customized section visibility, **When** I refresh the page, **Then** my section selections are preserved

---

### User Story 2 - Visual Feedback for Section State (Priority: P1)

As a user, I want clear visual distinction between active and inactive sections so that I can easily understand which sections will appear in my final PDF.

**Why this priority**: Without clear visual feedback, users cannot effectively use the section toggle feature. This is essential for usability of the core feature.

**Independent Test**: Can be tested by observing the visual states of section pills and verifying they match the expected design.

**Acceptance Scenarios**:

1. **Given** a section is enabled, **When** I view the section pill, **Then** it displays with a purple/primary background, white text, and a checkmark icon

2. **Given** a section is disabled, **When** I view the section pill, **Then** it displays with a white background, gray border, gray text, and no checkmark icon

3. **Given** I hover over a section pill, **When** hovering, **Then** the pill shows a hover state indicating it is interactive

---

### User Story 3 - Add Custom Section (Priority: P2)

As a user, I want to add custom sections to my resume so that I can include additional information not covered by the default sections.

**Why this priority**: While valuable for power users who need specialized sections (e.g., Publications, Languages, Volunteer Work), this extends beyond the core toggle functionality and can be delivered after the primary feature.

**Independent Test**: Can be tested by clicking "Add Custom Section", entering a section name, and verifying the new section appears in both the section pills and the resume preview.

**Acceptance Scenarios**:

1. **Given** I am on the Resume Preview screen, **When** I click "+ Add Custom Section", **Then** I am prompted to enter a section name

2. **Given** I have entered a valid section name, **When** I confirm the creation, **Then** a new toggleable section pill appears in the Visible Sections area

3. **Given** I have added a custom section, **When** I toggle it on, **Then** an empty section with that name appears in the resume preview where I can add content

---

### User Story 4 - Section Order Preservation (Priority: P3)

As a user, I want the sections in my PDF to maintain a logical order so that my resume reads professionally and follows standard resume conventions.

**Why this priority**: Section ordering affects resume quality but has a sensible default (standard resume order). This can be enhanced later with drag-and-drop reordering.

**Independent Test**: Can be tested by toggling various section combinations and verifying the PDF output follows the expected order.

**Acceptance Scenarios**:

1. **Given** I have enabled multiple sections, **When** I generate the PDF, **Then** sections appear in standard resume order: Personal Details → Work Experience → Education → Skills → Projects → Certifications → Custom Sections

2. **Given** I have only enabled Work Experience and Education, **When** I generate the PDF, **Then** they appear in the correct order without gaps where disabled sections would have been

---

### Edge Cases

- What happens when the user deselects all sections? → The system should require at least Personal Details to be visible (it cannot be disabled)
- What happens when a section has no data? → Empty sections should appear grayed out/disabled in the toggle pills with a tooltip indicating "No data available"
- What happens on mobile/narrow screens? → Section pills should wrap to multiple rows while maintaining usability
- What happens when the user has many custom sections? → The visible sections area should handle overflow gracefully (scrolling or expanding)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display a "Visible Sections" control panel above the resume preview showing all available resume sections as toggleable pills
- **FR-002**: System MUST allow users to enable/disable resume sections by clicking on section pills
- **FR-003**: System MUST visually distinguish between enabled sections (purple/primary filled pill with checkmark) and disabled sections (white/outlined pill without checkmark)
- **FR-004**: System MUST update the live resume preview in real-time when section visibility is toggled
- **FR-005**: System MUST generate PDFs containing only the sections that are currently enabled
- **FR-006**: System MUST persist section visibility preferences across page refreshes within the same resume session
- **FR-007**: System MUST prevent users from disabling all sections (Personal Details must always remain enabled)
- **FR-008**: System MUST display an "+ Add Custom Section" button that allows users to create new resume sections
- **FR-009**: System MUST show sections with no data as disabled/grayed out with appropriate feedback
- **FR-010**: System MUST maintain standard resume section ordering regardless of toggle order

### Key Entities

- **Resume Section**: Represents a distinct content area of the resume (e.g., Work Experience, Education). Key attributes: name, type (standard/custom), visibility state, content availability
- **Section Preference**: Represents user's visibility preference for each section within a resume. Key attributes: section reference, is_visible flag, display order
- **Resume**: The parent entity containing all sections and user preferences. Relationship: has many sections, has many section preferences

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can toggle section visibility in under 1 second per action (immediate visual feedback)
- **SC-002**: 95% of users can successfully customize their resume sections on first attempt without assistance
- **SC-003**: Generated PDFs accurately reflect section selections with 100% consistency
- **SC-004**: Section preferences persist correctly across browser sessions for at least 30 days
- **SC-005**: The section toggle interface is fully usable on screen widths from 768px to 2560px
- **SC-006**: Users can create a tailored resume for a specific job in under 2 minutes using section toggles
