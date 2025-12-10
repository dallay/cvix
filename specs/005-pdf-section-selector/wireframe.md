# Cvix PDF Generator Screen - Pure ASCII Wireframe

This file documents the main wireframe for the PDF Generator screen in feature `005-pdf-section-selector`.

- **Purpose**: Serves as a quick visual reference for layout, structure, and key UI components, aligning elements with feature requirements and component contracts.
- **Relation**: All interactive elements (section pills, toggles, panels, preview, settings) are mapped to the components and tasks described in the feature files (`spec.md`, `tasks.md`, `component-contracts.md`).
- **Figma Reference**: [View main Figma design](https://www.figma.com/design/RdLso6u4iuoulszrHaaraY/cvix?node-id=1-2&t=9XloDsg906QBkIaS-4)

## Usage in the Feature

- The ASCII wireframe should be used as a reference for:
  - The arrangement of components on the screen (`ResumePdfPage.vue`, `SectionTogglePanel.vue`, etc.)
  - The design and behavior of section pills and visibility controls
  - The structure of side panels and customization options
  - The integration of preview elements and main actions (Download PDF, Share)
- E2E and UI tests should validate that the implementation respects the structure and hierarchy shown here.
- Component contracts (`component-contracts.md`) and tasks (`tasks.md`) should reference this wireframe to ensure visual and functional consistency.

## Main Wireframe

```text
+----------------------------------------------------------------------------------------------+
| [cv] Profile Tailors| Resume Preview   | Last saved just now     [Download PDF] [Share Link] |
+---------------------+------------------------------------------------------------------------+
|                     |                                                                        |
| [=] Templates       |  Visible Sections:                                                     |
|                     |  [v] Personal  [v] Work Exp  [v] Education  [v] Skills  ( ) Projects   |
| +-------+ +-------+ |  ( ) Certificates     + Add Custom Section                             |
| |       | |       | |                                                                        |
| |ACTIVE | |       | |                                                                        |
| +-------+ +-------+ |      +----------------------------------------------------------+      |
|                     |      |                                                          |      |
| (@) Appearance      |      |                   Alex Morgan                            |      |
| Accent Color        |      |                                                          |      |
| (•) ( ) ( ) ( )     |      |  [e] alex@example.com   [t] (555) 123-4567               |      |
| Purple Blue Grn Blk |      |  [L] San Francisco, CA                                   |      |
|                     |      |                                                          |      |
| Font Family         |      |  Product-focused Senior Software Engineer with 6+ years  |      |
| [ Inter         v ] |      |  of experience building scalable web applications...     |      |
|                     |      |                                                          |      |
|                     |      |  ______________________________________________________  |      |
| [*] Page Settings   |      |                                                          |      |
|                     |      |  Work Experience                                         |      |
| Page Numbers        |      |                                                          |      |
| (  O )  [ON]        |      |  Senior Frontend Engineer                 2021 - Present |      |
|                     |      |  TechFlow Inc.                                           |      |
| Date Format         |      |  * Led the migration of dashboard to React...            |      |
| [ MM/YYYY       v ] |      |  * Mentored 3 junior developers...                       |      |
|                     |      |                                                          |      |
|                     |      |  Software Developer                          2018 - 2021 |      |
|                     |      |  Creative Solutions                                      |      |
|                     |      |  * Developed and maintained client-facing...             |      |
|                     |      |                                                          |      |
|                     |      |  Education                                               |      |
|                     |      |                                                          |      |
|                     |      +----------------------------------------------------------+      |
|                     |                                                                        |
+---------------------+------------------------------------------------------------------------+
```

## ASCII Symbols Legend

- `+`, `-`, `|` : Borders and window structure
- `[ ]` : Buttons or input fields
- `(-)` : Selected radio option (Radio button)
- `( )` : Inactive radio option
- `[v]` : Active/selected pill button
- `v` : Dropdown menu arrow
- `( O )` : Toggle switch
- `____` : Horizontal separator line (`<hr>`)

---

**For visual details and variants, see the [main Figma](https://www.figma.com/design/RdLso6u4iuoulszrHaaraY/cvix?node-id=1-2&t=9XloDsg906QBkIaS-4).**
