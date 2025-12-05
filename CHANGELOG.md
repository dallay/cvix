# 1.0.0 (2025-12-05)

### ⚠ BREAKING CHANGES

* Tests need to be updated to work with new template loading

* fix(resume): 🐛 change ST4 delimiters from $ to @ to avoid LaTeX conflicts

* Change delimiters in StringTemplateConfiguration from '$' to '@'
* Update resume-template-en.st to use @ delimiters
* Update resume-template-es.st to use @ delimiters
* Update README.md with explanation of @ delimiter choice

This fixes ST4 parsing errors caused by LaTeX syntax conflicts:

* $ is heavily used in LaTeX for math mode
* ST4 was unable to parse templates due to ambiguity between LaTeX $ and ST4 $
* Using @ as delimiter avoids this conflict completely

Error fixed: 'mismatched input' at line 43:100 in template

The @ delimiter is rarely used in LaTeX, making it ideal for ST4 templates
that contain LaTeX code.

* feat(resume): update summary length limit and refactor interface names for PDF and template rendering

* test(latex): 🧪 enhance test coverage for LaTeX template rendering

Add new tests for long content handling, null/empty fields, LaTeX structure validation, and security against LaTeX injection.

* feat(collection): ✨ integrate locale variable and enhance CSRF token handling

Add locale support in headers and improve pre-request and post-response scripts for XSRF-TOKEN management.

* feat(template): enhance caching for template groups and i18n translations

* refactor(renderer): 🔧 simplify LaTeX escaping in UrlRenderer

Replace manual escaping with LatexEscaper utility for cleaner code

* feat(template): ✨ enhance TemplateValidator with comprehensive LaTeX injection detection

* Expanded DANGEROUS_PATTERN to include additional LaTeX commands.
* Improved validation coverage by adding tests for all user-controlled fields in ResumeData.

* feat(docker): ✨ update TeX Live Docker image to TL2024-historic

* feat(template): enhance templates with conditional rendering for contact information

* refactor(resume): 🔧 rename 'company' to 'name' in WorkExperience model

Updated the WorkExperience model to use 'name' instead of 'company' for better clarity and consistency across the codebase. Adjusted related templates and tests accordingly.

* feat(template): enhance education entry formatting and toggle document persistence

* test(template): 🔒 add tests for LaTeX injection prevention in template rendering

* test(template): 🧪 add tests for LaTeX escaping of special characters

* test(template): 🧪 add tests for LaTeX injection detection in resume data

* feat(workExperience): ✨ integrate ResourceBundle for localized period formatting

* fix(pdfGenerator): update timeoutSeconds to 30 for improved performance

* fix(template): 🐛 handle missing i18n resource bundles gracefully

Fallback to English if the specified locale's resource bundle is not found

* fix(template): 🐛 escape skill level to prevent LaTeX injection

* fix(urlRenderer): 🐛 make URL protocol removal case-insensitive

* fix(engineering.stg): 🐛 update pdfkeywords to conditionally include location

* fix(template): 🐛 replace deprecated onecolentry with begin/end syntax in multiple files

* fix(monitoring): 🐛 update PDF generator configuration and increase timeout

### ✨ Features

* 001 user auth system ([#30](https://github.com/dallay/cvix/issues/30)) ([67ee871](https://github.com/dallay/cvix/commit/67ee871c2e69ebc601468815c8691206a524199d))
* add @astrojs/vue dependency and include ui package.json in Dockerfile ([6f0a94a](https://github.com/dallay/cvix/commit/6f0a94af9649b646bbe94f5da880965b5f116824))
* add @cvix/assets package for centralized asset management and u… ([#323](https://github.com/dallay/cvix/issues/323)) ([1067b44](https://github.com/dallay/cvix/commit/1067b44f9550a7153503af08f2b8cc8128e6aac8))
* add API endpoints for user authentication and workspace management ([#50](https://github.com/dallay/cvix/issues/50)) ([85c5fc2](https://github.com/dallay/cvix/commit/85c5fc29761be6c1a5ea7bb4a736114780c77862))
* add centralized configuration for base URLs and create .env.example for environment variables ([#329](https://github.com/dallay/cvix/issues/329)) ([8d07f63](https://github.com/dallay/cvix/commit/8d07f6358cb2050f1140a8c9ff61191996c42091))
* add constraint violation handler and constants for improved validation error responses ([#302](https://github.com/dallay/cvix/issues/302)) ([b5af27f](https://github.com/dallay/cvix/commit/b5af27f86d41a2abcba015842478a94382267b1d))
* Add Docker build and push for all applications ([#86](https://github.com/dallay/cvix/issues/86)) ([6f26c3c](https://github.com/dallay/cvix/commit/6f26c3c765f478dcdeddf3c3b793d1d3fdd0dd28))
* add existsSync to update-gradle-version script for improved file handling ([cbab6f4](https://github.com/dallay/cvix/commit/cbab6f4efe3174dbb6f7d491d4481f8ad270492e))
* add fade-in animation to landing page sections and refine dark mode color palette ([e9bc2d5](https://github.com/dallay/cvix/commit/e9bc2d56bd76d63eefdae5796a990a4abb1866b3))
* Add JSON Resume Schema support to backend API ([#107](https://github.com/dallay/cvix/issues/107)) ([a454ee4](https://github.com/dallay/cvix/commit/a454ee48d0bc3f3bd24b49e86176dafc9c47799f))
* add Makefile for streamlined development commands ([#80](https://github.com/dallay/cvix/issues/80)) ([754583a](https://github.com/dallay/cvix/commit/754583a08e38a1f4fd00d514c12cf1c4bb946cc1))
* Add modern landing page for Civix app ([#52](https://github.com/dallay/cvix/issues/52)) ([cb3c24d](https://github.com/dallay/cvix/commit/cb3c24da55ea80ac4424f1cea5d64090e2d38d41))
* add multiple UI components and update imports for @cvix/ui ([bcf7ce7](https://github.com/dallay/cvix/commit/bcf7ce7ffdb423da5814e0bbdc162c443b3fcdd0))
* Add ruler CLI as devDependency ([#31](https://github.com/dallay/cvix/issues/31)) ([c9b047f](https://github.com/dallay/cvix/commit/c9b047f4d409f17fb5970038b28fbc09ac828708))
* add specification quality checklist and feature specification for resume data entry screen ([c9e01c2](https://github.com/dallay/cvix/commit/c9e01c2894aa499fb2147bf2643fe2ee937bec54))
* add specification quality checklist and feature specification for resume data entry screen ([#188](https://github.com/dallay/cvix/issues/188)) ([cb73f38](https://github.com/dallay/cvix/commit/cb73f38d4032e6c730f50134242c8a47fabd1dc9))
* add webapp url constant ([aabdedf](https://github.com/dallay/cvix/commit/aabdedfb1be0ed0ccd8f3edd71c0e136fa9118b1))
* **auth:** change form validation to trigger on blur ([#47](https://github.com/dallay/cvix/issues/47)) ([b3916a2](https://github.com/dallay/cvix/commit/b3916a2a0fa4fbd06cac293d27c6a3bac8a4af1e))
* Autosave and Data Persistence ([#283](https://github.com/dallay/cvix/issues/283)) ([117f219](https://github.com/dallay/cvix/commit/117f219ca4e63c0a21fcfa082b9ecd9e3aa48540))
* backend package restructuring ([#309](https://github.com/dallay/cvix/issues/309)) ([1abf96f](https://github.com/dallay/cvix/commit/1abf96f69025d80066d14577b7d6c6a0b52fc429))
* change landing page ui ([0877d14](https://github.com/dallay/cvix/commit/0877d14c7e216d96131d0337e2f07887e383f029))
* **ci:** add slug to codecov action ([#242](https://github.com/dallay/cvix/issues/242)) ([5014ef1](https://github.com/dallay/cvix/commit/5014ef1eb3926ecfe1c31cf23e7973b2d32c1b2d))
* **ci:** merge workflows to fix cancellation issues ([#251](https://github.com/dallay/cvix/issues/251)) ([aca7a4c](https://github.com/dallay/cvix/commit/aca7a4c732fafddd0601f89d0cf3bbc3515a29c0))
* **ci:** optimize GitHub Actions workflows ([#246](https://github.com/dallay/cvix/issues/246)) ([2fae517](https://github.com/dallay/cvix/commit/2fae51732291b2a8ebb47e54240fde6e6a7c2a0e))
* **cod 21:**  validation engine for json schema ([#111](https://github.com/dallay/cvix/issues/111)) ([2b4a775](https://github.com/dallay/cvix/commit/2b4a775a4aa855a2eb11150e7580ff3c27834407))
* cod 22 stringtemplate engine integration ([#135](https://github.com/dallay/cvix/issues/135)) ([c3ddfa4](https://github.com/dallay/cvix/commit/c3ddfa4ecc098d2a88dfbec6910a3914a7433f12)), closes [#144](https://github.com/dallay/cvix/issues/144)
* cod 24 pdf delivery via api ([#167](https://github.com/dallay/cvix/issues/167)) ([a4752f1](https://github.com/dallay/cvix/commit/a4752f1402c8a8780ae68f79720950720d9ed37c))
* **cv:** resume form ([#94](https://github.com/dallay/cvix/issues/94)) ([1e924f4](https://github.com/dallay/cvix/commit/1e924f4f9de63c21294fb587ec13102142499d2b)), closes [#95](https://github.com/dallay/cvix/issues/95)
* enhance API error responses with localized messages and trace IDs ([#118](https://github.com/dallay/cvix/issues/118)) ([574dfb3](https://github.com/dallay/cvix/commit/574dfb3b06803dc07ea9eb16cb446172c3cbc77a)), closes [#122](https://github.com/dallay/cvix/issues/122)
* enhance Codecov integration with verbose output and upload status tracking ([#332](https://github.com/dallay/cvix/issues/332)) ([0fef7e5](https://github.com/dallay/cvix/commit/0fef7e5c67edea9ee74233fe0a66c8745667b6a2))
* enhance site URL resolution with SITE_URL env var and improved fallback logic ([e2cf13f](https://github.com/dallay/cvix/commit/e2cf13f9bfc77c0ab96391d8d9284b56d9aa9c6f))
* Enhance test coverage for backend and frontend resume functionality ([#177](https://github.com/dallay/cvix/issues/177)) ([22ae527](https://github.com/dallay/cvix/commit/22ae527eb8042bda48c060839f9da8789ad3c6d8))
* implement centralized Vitest configuration for monorepo with project-specific overrides ([#363](https://github.com/dallay/cvix/issues/363)) ([d9f98f7](https://github.com/dallay/cvix/commit/d9f98f74f20a788b464ce7ce423b384383ee7f18))
* implement workspace selection feature with data model, plan, quickstart guide, and research documentation ([#36](https://github.com/dallay/cvix/issues/36)) ([6c6abf8](https://github.com/dallay/cvix/commit/6c6abf82db3a954223d3a115f7399a1a5ce24582))
* improving code quality and maintainability by removing unnecessary console logging and refactoring error handling ([#108](https://github.com/dallay/cvix/issues/108)) ([1b3f98a](https://github.com/dallay/cvix/commit/1b3f98a44e1170c12f1321ceb4351f582936dd5a))
* localize template showcase section and add translations for template names ([af78ebc](https://github.com/dallay/cvix/commit/af78ebc36c993a44c3baf1ffd79515a1fa34a67f))
* pdf view ([#300](https://github.com/dallay/cvix/issues/300)) ([4e02cfd](https://github.com/dallay/cvix/commit/4e02cfdef0cd3587c2bfa2f288e5da309d1e82d4))
* Phase 2: Foundational (Blocking Prerequisites) ([#231](https://github.com/dallay/cvix/issues/231)) ([5ae3cee](https://github.com/dallay/cvix/commit/5ae3cee9f186d41ecd5a6b0009b959ef780d9303))
* Phase 6: User Story 4 - Autosave and Data Persistence (Priority: P2) ([#256](https://github.com/dallay/cvix/issues/256)) ([dfd956c](https://github.com/dallay/cvix/commit/dfd956c3f6eb3f3eb177b0c6008a4760dd5ccee6)), closes [#258](https://github.com/dallay/cvix/issues/258)
* Preview Interaction and Navigation  ([#305](https://github.com/dallay/cvix/issues/305)) ([036b94a](https://github.com/dallay/cvix/commit/036b94aca5d86987f3c70e4b50e3775424fd1eb7))
* refactor footer component structure and add FooterLinkList for better code organization ([d430461](https://github.com/dallay/cvix/commit/d430461441bb3813a2fd19cfc417fd66ae07ba21))
* refactor landing page features section for improved maintainability ([80e188c](https://github.com/dallay/cvix/commit/80e188c538970c105528586856869dea93bd2984))
* refactor UI translation files to use UIMultilingual type and update TrustMetrics for full localization ([004d753](https://github.com/dallay/cvix/commit/004d7531c4e261ae752bb23859019ef2277511aa))
* refine fade-in animation delays for landing page sections and fix BASE_WEBAPP_URL import in Hero ([b2b8094](https://github.com/dallay/cvix/commit/b2b80944bfe9f9293c0102b660029deac5dff5ac))
* remove unused dependencies from package.json and pnpm-lock.yaml ([fd674a6](https://github.com/dallay/cvix/commit/fd674a684036680f0133acf6d5d048ba8c5ca8a0))
* resume form ui ([#98](https://github.com/dallay/cvix/issues/98)) ([bc25b46](https://github.com/dallay/cvix/commit/bc25b46fffa852c1b4fbe1ef5ed1a0467f9cb198))
* resume generator mvp plan ([#55](https://github.com/dallay/cvix/issues/55)) ([ccc0c8e](https://github.com/dallay/cvix/commit/ccc0c8e798b138f964fac17dba41148386e799b9))
* resume generator mvp tasks ([#56](https://github.com/dallay/cvix/issues/56)) ([9c0ecdd](https://github.com/dallay/cvix/commit/9c0ecdd6d9dab0321f2ce20bd25a590a9dd71867))
* resume generator v1 ([#83](https://github.com/dallay/cvix/issues/83)) ([871c7ab](https://github.com/dallay/cvix/commit/871c7ab53cf50144ac2da0e9f3603f5bff99ae05))
* Setup (Shared Infrastructure) [#219](https://github.com/dallay/cvix/issues/219) ([#228](https://github.com/dallay/cvix/issues/228)) ([c219438](https://github.com/dallay/cvix/commit/c2194388d0d558dbc938e282a90587c3e3848896))
* update acceptance scenarios and success criteria for resume data entry ([42249f1](https://github.com/dallay/cvix/commit/42249f17048e0bee46fb61f4ed987bec0d33e1dd))
* update CSS variables and styles for improved theming and scrollbar customization ([791efdc](https://github.com/dallay/cvix/commit/791efdca49a25dc702c4d61158d789a505d66702))
* update dependencies in package.json for improved stability and performance ([a7ec102](https://github.com/dallay/cvix/commit/a7ec102bcd5ec8319ad9bfb6f003f569d8b385a1))
* update documentation title and description for ProFileTailors ([1631e98](https://github.com/dallay/cvix/commit/1631e98992dba0309ae19d4de3e9039ff5b7a207))
* update favicon links and add manifest.json for improved PWA support ([9bd1f0d](https://github.com/dallay/cvix/commit/9bd1f0d64909f6976dc68ce4cdf9d513d0c8be9b))
* update footer localization, improve FAQ styling, and refine template showcase aria labels ([7992551](https://github.com/dallay/cvix/commit/79925511a338d573b8911a70c315a94427a1f4ea))
* update frontend API integration to use BACKEND_URL env var and improve proxy/env handling ([bf2da27](https://github.com/dallay/cvix/commit/bf2da27d08bc7253d85db5404629910c363168c9))
* update Hero component to use BASE_WEBAPP_URL for dynamic links ([5fd9cdc](https://github.com/dallay/cvix/commit/5fd9cdc7aad5b603180029d99fbddc1d5155e3c7))
* update resume rendering to handle empty endDate as ongoing employment and improve date formatting ([#376](https://github.com/dallay/cvix/issues/376)) ([99db68b](https://github.com/dallay/cvix/commit/99db68b3e70de6fb923c25bbbbff64f7f1531e11)), closes [#377](https://github.com/dallay/cvix/issues/377)
* update schema version in biome.json to 2.3.8 ([dcec889](https://github.com/dallay/cvix/commit/dcec889bfc3dfbe857f777c2d0327283dfcf51fe))
* update template showcase to use BASE_WEBAPP_URL for template links and add TemplateShowcase interface ([2fc4d8a](https://github.com/dallay/cvix/commit/2fc4d8a9357266c3f481e4daabfc68e57247e57c))
* update vitest config with improved plugin setup and env handling; clean up components and docs ([89d07bd](https://github.com/dallay/cvix/commit/89d07bde473bb97a79a8db2b1d947141ac9eed4b))
* update vitest config with improved plugin setup and env handling; clean up components and docs ([aed7bfd](https://github.com/dallay/cvix/commit/aed7bfdd191e7314cd8b8a76b844125d285a5abe))
* User Story 1 - Resume Data Entry with Live Preview  ([#244](https://github.com/dallay/cvix/issues/244)) ([b575f9f](https://github.com/dallay/cvix/commit/b575f9f1a8acb38a60b1477febb2ac46c8e00e44))

### 🐛 Bug Fixes

* **ci:** adjust concurrency group for PR title linting ([#184](https://github.com/dallay/cvix/issues/184)) ([8e55842](https://github.com/dallay/cvix/commit/8e55842860d409eab11d484aa352f985c24ddede))
* **ci:** install git in frontend Dockerfiles ([#91](https://github.com/dallay/cvix/issues/91)) ([8983c26](https://github.com/dallay/cvix/commit/8983c262784b62c7959a7c35d2d32a42bc95e13e))
* **ci:** update Dockerfile to use build.gradle.kts ([#294](https://github.com/dallay/cvix/issues/294)) ([b56a5d1](https://github.com/dallay/cvix/commit/b56a5d1884453ed31925828e19e75e757bc0da0c))
* codecov ([#237](https://github.com/dallay/cvix/issues/237)) ([44ec39e](https://github.com/dallay/cvix/commit/44ec39efdfc2743440f8b3a9cc08c4965eba5c25))
* Dependency Review Action Trigger ([#282](https://github.com/dallay/cvix/issues/282)) ([b47cf25](https://github.com/dallay/cvix/commit/b47cf25ee3229aa84c5e9f4b7227ef33bff6678b))
* **deps:** update dependency com.github.ben-manes:gradle-versions-plugin to v0.53.0 ([#185](https://github.com/dallay/cvix/issues/185)) ([4d4eeb1](https://github.com/dallay/cvix/commit/4d4eeb166b677189bc02e1419ca8f57225439b55))
* **deps:** update dependency io.nlopez.compose.rules:detekt to v0.4.27 ([#136](https://github.com/dallay/cvix/issues/136)) ([7646aac](https://github.com/dallay/cvix/commit/7646aac9cdeb289276c12c7c3bab7beadd7ca20b))
* **deps:** update dependency io.nlopez.compose.rules:detekt to v0.4.28 ([#299](https://github.com/dallay/cvix/issues/299)) ([a86b437](https://github.com/dallay/cvix/commit/a86b437fde4403cd554cb9ffb7a91fc842e0a56e))
* **deps:** update dependency org.jetbrains.dokka:dokka-gradle-plugin to v2 ([#347](https://github.com/dallay/cvix/issues/347)) ([0402109](https://github.com/dallay/cvix/commit/0402109f7557539bc35dbf9665860a1642e6ad75))
* **deps:** update dependency org.jetbrains.kotlinx:kover-gradle-plugin to v0.9.3 ([#186](https://github.com/dallay/cvix/issues/186)) ([2adbbc0](https://github.com/dallay/cvix/commit/2adbbc004133e944959a671efb75070a18cf3f32))
* **deps:** update dependency org.jlleitschuh.gradle.ktlint:org.jlleitschuh.gradle.ktlint.gradle.plugin to v14 ([#348](https://github.com/dallay/cvix/issues/348)) ([01811bc](https://github.com/dallay/cvix/commit/01811bcd05446e521279b927c1e0aaa4adad4c76))
* **deps:** update dependency org.siouan:frontend-jdk17 to v10 ([#350](https://github.com/dallay/cvix/issues/350)) ([00addca](https://github.com/dallay/cvix/commit/00addca435b7227e45a7641adcb9a9fdb1fa20ff))
* **deps:** update dependency org.springframework.cloud:spring-cloud-dependencies to v2025 ([#351](https://github.com/dallay/cvix/issues/351)) ([4770e4b](https://github.com/dallay/cvix/commit/4770e4b329bf23db89e0e43a7b1c0a0ee611b679))
* **deps:** update deprecated Bucket4j API usage ([#273](https://github.com/dallay/cvix/issues/273)) ([1daaeb3](https://github.com/dallay/cvix/commit/1daaeb371157a220e9e82c41f221218c7345c00b))
* **deps:** update jsonwebtoken to v0.13.0 ([#189](https://github.com/dallay/cvix/issues/189)) ([5a40c2f](https://github.com/dallay/cvix/commit/5a40c2f96c5619e7adae8948889dea9497e640b3))
* Docker image build and publish pipeline to GHCR ([#176](https://github.com/dallay/cvix/issues/176)) ([10bd39f](https://github.com/dallay/cvix/commit/10bd39fa335964a3addae1f1215090599c53933c)), closes [#181](https://github.com/dallay/cvix/issues/181)
* exposes raw passwords [#268](https://github.com/dallay/cvix/issues/268) ([#270](https://github.com/dallay/cvix/issues/270)) ([1e55348](https://github.com/dallay/cvix/commit/1e5534840427155e49f2d5911b0354068b48ede1)), closes [#258](https://github.com/dallay/cvix/issues/258)
* improve wording for required field validation message in spec ([b7025e1](https://github.com/dallay/cvix/commit/b7025e1b18a222a33d6402599608105b5280376b))
* package rename ([#311](https://github.com/dallay/cvix/issues/311)) ([56b7a18](https://github.com/dallay/cvix/commit/56b7a18a065c8c5c24ddf17a00a58e5748e830b7))
* performance issue in resume form input ([#88](https://github.com/dallay/cvix/issues/88)) ([7ac4e40](https://github.com/dallay/cvix/commit/7ac4e40b7f2cd8a70c5f7bffcb60e7f7f77188ae))
* resolve dependency vulnerabilities ([#274](https://github.com/dallay/cvix/issues/274)) ([115430b](https://github.com/dallay/cvix/commit/115430b48ba1195a01c8ecc017eedd46a1a5cbf7))
* skip lefthook git operations in Docker and CI builds ([#92](https://github.com/dallay/cvix/issues/92)) ([828d9fa](https://github.com/dallay/cvix/commit/828d9fa91d6f4ca6539f60779528051289269210))
* Update OWASP Dependency Check to 12.1.8 for CVE-2025-48924 ([#63](https://github.com/dallay/cvix/issues/63)) ([6fa5e2e](https://github.com/dallay/cvix/commit/6fa5e2ee86798e100fafead23ccc71432a15976d))
* Use docker-container driver for Buildx ([#90](https://github.com/dallay/cvix/issues/90)) ([7fe3f71](https://github.com/dallay/cvix/commit/7fe3f71b57488d2f5892b77271c15a9b1a19d4f0))
* workflow parsing error: secrets access in reusable workflows ([#74](https://github.com/dallay/cvix/issues/74)) ([1220e95](https://github.com/dallay/cvix/commit/1220e959eb190441188343679d2bfc179b6da551))

### ♻️ Refactors

* date input fields again ([#362](https://github.com/dallay/cvix/issues/362)) ([c0b9e68](https://github.com/dallay/cvix/commit/c0b9e6890bd48b0fc7dedf47b973fc1bb18c9e71))
* date input fields to use vue-shadcn date-picker component ([#330](https://github.com/dallay/cvix/issues/330)) ([3160e8a](https://github.com/dallay/cvix/commit/3160e8a1ad576a9c89aee87313e5fecaa23a580d)), closes [#360](https://github.com/dallay/cvix/issues/360) [#331](https://github.com/dallay/cvix/issues/331) [#332](https://github.com/dallay/cvix/issues/332) [#351](https://github.com/dallay/cvix/issues/351) [#358](https://github.com/dallay/cvix/issues/358)
* enhance chunk handling and improve Dockerfile validation in CI ([#370](https://github.com/dallay/cvix/issues/370)) ([d44aae9](https://github.com/dallay/cvix/commit/d44aae95accd44be1b59a50d3f3ee51541c85b9f))
* rename package from 'web.request' to 'http.request' for consistency ([ac4dc07](https://github.com/dallay/cvix/commit/ac4dc07fd2baa130bcb2a5ba8d7f4a95a3ede162))
* rename package from 'web.request' to 'http.request' for consistency ([c6fa5f3](https://github.com/dallay/cvix/commit/c6fa5f38da75d656e30ac6dbe8eb6877d20c81c3))
* rename project from Loomify to ProFileTailors and update all branding, package names, and references to cvix ([0220c02](https://github.com/dallay/cvix/commit/0220c02fe238522518dfdfbe208a00b14351abd5))

### 📝 Documentation

* Align resume generator spec with implemented storage system ([#187](https://github.com/dallay/cvix/issues/187)) ([5526903](https://github.com/dallay/cvix/commit/5526903ee836b8b38b5f4373955424703f48d973))
* conventions update ([#358](https://github.com/dallay/cvix/issues/358)) ([e46217f](https://github.com/dallay/cvix/commit/e46217f56174b2622ef4031e21174eb9349f294e))
* Implementation Plan: Resume Data Entry Screen ([#198](https://github.com/dallay/cvix/issues/198)) ([8742f7f](https://github.com/dallay/cvix/commit/8742f7f736037a0f6f0b95afdb2ae9986e51832c))
* ratify constitution v1.0.0 (code quality, testing, UX, performance principles) ([#331](https://github.com/dallay/cvix/issues/331)) ([42b8b35](https://github.com/dallay/cvix/commit/42b8b355b935f4a0191238911d9cefbf672b25bc))
* Tasks for entering screen resume data and generating PDFs on the screen ([#213](https://github.com/dallay/cvix/issues/213)) ([4e145fe](https://github.com/dallay/cvix/commit/4e145fe818106c20e364ac7d611db6e403e5fd1e))
* update terminology and clarify specifications in plan, spec, and tasks documents ([#216](https://github.com/dallay/cvix/issues/216)) ([543c91d](https://github.com/dallay/cvix/commit/543c91d5353bd34b32b275787a6b7cdbd8176959))

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

* **Workspace Selection Feature** - Complete frontend implementation for workspace selection functionality
  * Automatic workspace loading on user login (loads last selected or default workspace)
  * Manual workspace selection through an enhanced dropdown selector component
  * Workspace state management using Pinia with local storage persistence
  * Loading and error states with visual feedback
  * Retry logic with exponential backoff for failed workspace loads (3 attempts: 1s, 2s, 4s delays)
  * 5-minute cache TTL for workspace data to optimize performance
  * Comprehensive test coverage (>75%) across domain, application, and presentation layers
  * ARIA labels and keyboard navigation support for accessibility
  * Integration with existing authentication system (Keycloak OAuth2/OIDC)

### Technical Details

* **Domain Layer**: Pure TypeScript business logic with value objects (WorkspaceId, WorkspaceName) and selection service
* **Application Layer**: Composables (`useWorkspaceSelection`, `useWorkspaceLoader`) for orchestrating workspace operations
* **Infrastructure Layer**: Pinia store for state management, HTTP client for API integration, local storage adapter for persistence
* **Presentation Layer**: Vue.js components with Shadcn-Vue UI components (WorkspaceSelector, WorkspaceSelectorItem)
* **Testing**: Unit tests (Vitest), integration tests, E2E tests (Playwright) with 94% domain coverage, 100% application coverage

### Changed

* Enhanced workspace selector component with improved UX and visual feedback
* Updated router with navigation guard for automatic workspace loading on login

### Fixed

* Resolved 13 Biome linting warnings (noExplicitAny, noUselessConstructor)

---

## [1.0.0] - 2025-10-30

### Initial Release

* Base SaaS template with Spring Boot + Kotlin backend
* Vue.js 3 frontend with TypeScript
* Authentication system with Keycloak
* PostgreSQL database with Liquibase migrations
* Docker Compose infrastructure setup
* CI/CD with GitHub Actions
* Comprehensive documentation and development tools
