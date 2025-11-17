# Resume Data Entry Screen — Wireframe

This wireframe illustrates the Resume Data Entry Screen, featuring a two-column layout with a data entry form on the left and a live preview on the right.

```markdown
+--------------------------------------------------------------------------------------+
|                               TOP UTILITY BAR                                        |
|--------------------------------------------------------------------------------------|
| [ Upload JSON ]   [ Download JSON ]   [ Reset Form ]   [ Validate JSON ]             |
+--------------------------------------------------------------------------------------+

+----------------------------------+-----------------------------------------------+
|          DATA ENTRY              |                   LIVE PREVIEW                |
|----------------------------------|-----------------------------------------------|
|  [ TOC - Sticky ]                |   +----------------------------------------+  |
|  - Basics                        |   |                Preview Area             |  |
|  - Work                          |   |  (Generic template, independent scroll) |  |
|  - Education                     |   |                                        |  |
|  - Skills                        |   +----------------------------------------+  |
|  - Projects                      |                                               |
|  - Languages                     |                                               |
|  - Certificates                  |                                               |
|  - Publications                  |                                               |
|  - Awards                        |                                               |
|  - Volunteer                     |                                               |
|  - References                    |                                               |
|                                  |                                               |
| -------------------------------------------------------------------------------  |
| SECTION: BASICS (Accordion)                                                      |
|  Name: [...............................]                                         |
|  Label: [..............................]                                         |
|  Email: [..............................]                                         |
|  Phone: [..............................]                                         |
|  Summary: [ multiline textarea.........]                                         |
|  Location:                                                                    |
|     Address: [..............]  City: [..........] Region: [..........]          |
|     Country Code: [..]  Postal Code: [....]                                     |
|                                                                                 |
|  Profiles:                                                                      |
|     [ + Add Profile ]                                                           |
|     ┌ Network: [....] Username: [....] URL: [.............................] ┐   |
|     └ [Delete]                                                               ┘   |
|                                                                                 |
| ------------------------------------------------------------------------------- |
| SECTION: WORK EXPERIENCE (Accordion)                                             |
|  [ + Add Job ]                                                                   |
|                                                                                 |
|  Job #1                                                                          |
|     Company: [...................]                                               |
|     Position: [...................]                                              |
|     URL: [.........................]                                             |
|     Dates: Start [YYYY-MM-DD] End [YYYY-MM-DD]                                   |
|     Summary: [ textarea ................................................ ]       |
|                                                                                 |
|     Highlights:                                                                  |
|        - [ text input ......................... ] [Delete]                       |
|        - [ text input ......................... ] [Delete]                       |
|        [ + Add Highlight ]                                                       |
|                                                                                 |
| (Repeat similar structure for Education, Skills, Projects, etc.)                 |
+----------------------------------+-----------------------------------------------+
```
