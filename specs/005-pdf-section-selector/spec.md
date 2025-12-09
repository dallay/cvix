# Feature Specification: PDF Section Selector

**Feature Branch**: `005-pdf-section-selector`
**Created**: December 6, 2025
**Status**: ✅ Implemented
**Implementation Date**: December 8, 2025
**Input**: User description: "Redesign PDF Generator Screen to allow users to select which sections to include in the final PDF resume"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Toggle Resume Sections for PDF Export (Priority: P1)

As a user preparing my resume for a specific job application, I want to select which sections appear in my final PDF so that I can tailor my resume content to match the job requirements without editing my source data.

**Why this priority**: This is the core feature request. Users need the ability to customize which sections appear in their exported PDF to create targeted resumes for different opportunities. This delivers immediate value by enabling resume customization without data loss.

**Independent Test**: Can be fully tested by toggling section visibility and verifying the PDF output includes only selected sections. Delivers the core value of customizable resume exports.

**Acceptance Scenarios**:

1. **Given** I am on the Resume Preview screen with my complete resume data, **When** I view the "Visible Sections" area, **Then** I see toggleable pills for each available section (Personal Details, Work Experience, Education, Skills, Projects, Certificates, Volunteer, Awards, Publications, Languages, Interests, References)

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

### User Story 3 - Select Individual Items Within Sections (Priority: P1)

As a user, I want to select which specific items within each section to include in my PDF so that I can curate the most relevant experiences for each job application.

**Why this priority**: This is essential for creating targeted resumes. Users often have multiple work experiences, education entries, projects, and skills, but only want to showcase the most relevant ones for a specific job application. This granular control enables true resume customization.

**Independent Test**: Can be fully tested by expanding a section, toggling individual items on/off, and verifying the PDF output includes only the selected items within each section.

**Acceptance Scenarios**:

1. **Given** I have enabled a section (e.g., Work Experience) that contains multiple entries, **When** I click on the section pill, **Then** an item list expands directly below the pill in the "Visible Sections" area showing all items within that section with individual toggle controls

2. **Given** a section is expanded showing its items, **When** I click on the section pill again, **Then** the item list collapses and only the section pill is visible

3. **Given** I am viewing items within an expanded section, **When** I disable a specific item (e.g., one particular job), **Then** that item is excluded from the live preview and PDF export while other items in the section remain visible

4. **Given** the Personal Details section is expanded, **When** I view the selectable fields, **Then** I can individually toggle: profile image, email address, phone number, and location

5. **Given** I have multiple work experiences, **When** I disable one specific job entry, **Then** only that job disappears from the preview, and the remaining jobs stay visible

6. **Given** I have disabled all items within a section, **When** I view the section pill, **Then** it automatically becomes inactive (or shows a warning that no items are selected)

7. **Given** I have customized item visibility within sections, **When** I refresh the page, **Then** my item-level selections are preserved

---

### User Story 4 - Section Order Preservation (Priority: P2)

As a user, I want the sections in my PDF to maintain a logical order so that my resume reads professionally and follows standard resume conventions.

**Why this priority**: Section ordering affects resume quality but has a sensible default (standard resume order). This can be enhanced later with drag-and-drop reordering.

**Independent Test**: Can be tested by toggling various section combinations and verifying the PDF output follows the expected order.

**Acceptance Scenarios**:

1. **Given** I have enabled multiple sections, **When** I generate the PDF, **Then** sections appear in standard resume order: Personal Details → Work Experience → Education → Skills → Projects → Certificates

2. **Given** I have only enabled Work Experience and Education, **When** I generate the PDF, **Then** they appear in the correct order without gaps where disabled sections would have been

3. **Given** I have selected specific items within each section, **When** I generate the PDF, **Then** items within each section appear in their original chronological/defined order

---

### Edge Cases

- What happens when the user deselects all sections? → The system should require at least Personal Details to be visible (it cannot be disabled)
- What happens when a section has no data? → Empty sections should appear grayed out/disabled in the toggle pills with a tooltip indicating "No data available"
- What happens on mobile/narrow screens? → Section pills should wrap to multiple rows while maintaining usability
- What happens when the user deselects all items within an enabled section? → The section should automatically become disabled, or show a visual warning indicating no content will appear
- What happens when Personal Details has all optional fields disabled? → At minimum, the user's name must always be visible; other fields (email, phone, location, image) can all be individually disabled
- What happens when a section has only one item? → The item toggle should still be available but cannot result in an empty enabled section

## Requirements *(mandatory)*

### Functional Requirements

#### Section-Level Controls

- **FR-001**: System MUST display a "Visible Sections" control panel above the resume preview showing all available resume sections as toggleable pills
- **FR-002**: System MUST allow users to enable/disable resume sections by clicking on section pills
- **FR-003**: System MUST visually distinguish between enabled sections (purple/primary filled pill with checkmark) and disabled sections (white/outlined pill without checkmark)
- **FR-004**: System MUST update the live resume preview in real-time when section visibility is toggled
- **FR-005**: System MUST generate PDFs containing only the sections that are currently enabled
- **FR-006**: System MUST persist section visibility preferences across page refreshes within the same resume session
- **FR-007**: System MUST prevent users from disabling all sections (Personal Details must always remain enabled)
- **FR-008**: System MUST show sections with no data as disabled/grayed out with appropriate feedback
- **FR-009**: System MUST maintain standard resume section ordering regardless of toggle order

#### Item-Level Controls Within Sections

- **FR-010**: System MUST allow users to expand sections by clicking on section pills, displaying an item list inline directly below the pill in the "Visible Sections" area
- **FR-011**: System MUST provide individual toggle controls for each item within a section (e.g., each job in Work Experience, each degree in Education)
- **FR-012**: System MUST allow users to individually toggle Personal Details fields: profile image, email address, phone number, and location
- **FR-013**: System MUST ensure the user's name in Personal Details cannot be disabled (always required)
- **FR-014**: System MUST update the live preview in real-time when individual items are toggled
- **FR-015**: System MUST generate PDFs containing only the items that are currently enabled within each enabled section
- **FR-016**: System MUST persist item-level visibility preferences across page refreshes within the same resume session
- **FR-017**: System MUST automatically disable a section if all items within it are disabled, or show a visual warning

### Key Entities

- **Resume Section**: Represents a distinct content area of the resume (e.g., Work Experience, Education). Key attributes: name, visibility state, content availability. Relationship: contains multiple items/entries
- **Section Item**: Represents an individual entry within a section (e.g., a specific job, a specific degree, a specific skill). Key attributes: item reference, visibility state, display order within section
- **Personal Details Field**: Represents an individual field in the Personal Details section (name, email, phone, location, image). Key attributes: field type, visibility state (name always visible)
- **Section Preference**: Represents user's visibility preference for each section within a resume. Key attributes: section reference, is_visible flag, display order
- **Item Preference**: Represents user's visibility preference for each item within a section. Key attributes: item reference, is_visible flag
- **Resume**: The parent entity containing all sections, items, and user preferences. Relationship: has many sections, each section has many items

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can toggle section visibility in under 1 second per action (immediate visual feedback)
- **SC-002**: Users can toggle item visibility within sections in under 1 second per action (immediate visual feedback)
- **SC-003**: 95% of users can successfully customize their resume sections and items on first attempt without assistance
- **SC-004**: Generated PDFs accurately reflect both section and item selections with 100% consistency
- **SC-005**: Section and item preferences persist correctly across browser sessions for at least 30 days
- **SC-006**: The section and item toggle interface is fully usable on screen widths from 768px to 2560px
- **SC-007**: Users can create a tailored resume for a specific job in under 3 minutes using section and item toggles

## Clarifications

### Session 2025-12-06

- Q: What is the interaction mechanism for accessing item-level toggles within sections? → A: Section pills are clickable to expand inline - clicking on a section pill expands an item list directly below it in the "Visible Sections" area
