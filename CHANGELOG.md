## [3.0.3](https://github.com/dallay/cvix/compare/v3.0.2...v3.0.3) (2025-12-25)

### 🐛 Bug Fixes

* **pdf:** 🔒 run TexLive container with same UID as backend for secure file access ([#462](https://github.com/dallay/cvix/issues/462)) ([79b4776](https://github.com/dallay/cvix/commit/79b4776e37d7caf379b22c187687b3e812628022))

## [3.0.2](https://github.com/dallay/cvix/compare/v3.0.1...v3.0.2) (2025-12-24)

### 🐛 Bug Fixes

* **texlive:** 🐛 remove non-existent package names from tlmgr install ([c9fa699](https://github.com/dallay/cvix/commit/c9fa699d78b17c4fe3929307f0ce1b7d782840b3))

## [3.0.1](https://github.com/dallay/cvix/compare/v3.0.0...v3.0.1) (2025-12-24)

### 🐛 Bug Fixes

* correct SHA512 checksum verification in TexLive Dockerfile ([#458](https://github.com/dallay/cvix/issues/458)) ([15cdf68](https://github.com/dallay/cvix/commit/15cdf68bd4a0f35102822373f7983bce91891ac7))

## [3.0.0](https://github.com/dallay/cvix/compare/v2.1.0...v3.0.0) (2025-12-24)

### ⚠ BREAKING CHANGES

* Dependabot is removed, use Renovate for all dependency updates

* fix: 🐛 correct renovate config and documentation errors

- Remove duplicate test-results/ entry in .gitignore
- Fix Vue ecosystem grouping in renovate.json (was using AND logic)
- Move ignoreUnstable to root level in renovate.json for global scope
- Replace non-existent @astrojs/seo with custom SEO component
- Add version column to webapp tech stack table
- Fix missing onMounted import in webapp README example
- Correct repository URL in RENOVATE_CHANGES.md

Closes configuration issues that would prevent proper dependency grouping

* fix: 🐛 resolve AND logic in Astro, Vite, Tailwind, and TypeScript groups

Replace matchPackageNames + matchPackagePrefixes (AND logic) with
matchPackagePatterns (OR logic) for proper package matching:

- Astro: Now matches 'astro' AND '@astrojs/*' packages
- Vite: Now matches 'vite', 'vitest' AND '@vitejs/*' packages
- Tailwind: Now matches 'tailwindcss' AND '@tailwindcss/*' packages
- TypeScript: Now matches 'typescript' AND '@types/*' packages

Previous config used AND logic which excluded packages:
- 'astro' failed @astrojs/ prefix check
- 'vite'/'vitest' failed @vitejs/ prefix check
- 'tailwindcss' failed @tailwindcss/ prefix check
- 'typescript' failed @types/ prefix check

Changed from:
  matchPackageNames + matchPackagePrefixes (both must match)
To:
  matchPackagePatterns with regex (either can match)

Addresses PR review feedback on AND logic issue affecting all frontend tooling groups.

* chore: update renovate configuration to delay major updates for stability

### 🔧 Maintenance

* 🔧 optimize dependency management and update documentation ([#457](https://github.com/dallay/cvix/issues/457)) ([806268d](https://github.com/dallay/cvix/commit/806268d14fab5010bf02704f85f787692fe58793))

## [2.1.0](https://github.com/dallay/cvix/compare/v2.0.4...v2.1.0) (2025-12-23)

### ✨ Features

* implement dual driver strategy for database operations with R2DBC and JDBC ([#455](https://github.com/dallay/cvix/issues/455)) ([6dc09c9](https://github.com/dallay/cvix/commit/6dc09c9b136c45742d3fd5ad18dfa0411b2c6238))

### 📝 Documentation

* add environment variables for API and Keycloak configuration ([#452](https://github.com/dallay/cvix/issues/452)) ([93e1fe0](https://github.com/dallay/cvix/commit/93e1fe01d2ee3a7343c0875d746044485372e789))

## [2.0.4](https://github.com/dallay/cvix/compare/v2.0.3...v2.0.4) (2025-12-22)

### 🐛 Bug Fixes

* form style ([#451](https://github.com/dallay/cvix/issues/451)) ([d37cdb9](https://github.com/dallay/cvix/commit/d37cdb908baee4ed1007ff34db566009987ce707))

## [2.0.3](https://github.com/dallay/cvix/compare/v2.0.2...v2.0.3) (2025-12-22)

### 🐛 Bug Fixes

* auth verification ([#450](https://github.com/dallay/cvix/issues/450)) ([da9873b](https://github.com/dallay/cvix/commit/da9873b916fb001acd9a2711e8561fb04b87350d))

## [2.0.2](https://github.com/dallay/cvix/compare/v2.0.1...v2.0.2) (2025-12-22)

### 🐛 Bug Fixes

* **waitlist:** ✨ architecture dependencies ([#449](https://github.com/dallay/cvix/issues/449)) ([aac1017](https://github.com/dallay/cvix/commit/aac1017ef57d29009fec5b952c0e040121a9b497))

## [2.0.1](https://github.com/dallay/cvix/compare/v2.0.0...v2.0.1) (2025-12-21)

### 🐛 Bug Fixes

* **infra:** 🐛 force services to run on manager node for Dokploy deployment ([c6e3434](https://github.com/dallay/cvix/commit/c6e3434ae10a10a43ddc63a06eb648e5efce8a9d)), closes [#TBD](https://github.com/dallay/cvix/issues/TBD)

## [2.0.0](https://github.com/dallay/cvix/compare/v1.6.3...v2.0.0) (2025-12-20)

### ⚠ BREAKING CHANGES

* All Docker images now built on every release for version consistency

- Remove conditional builds based on frontend_changed/backend_changed
- All three Docker images (backend, marketing, webapp) now share the same version number
- Implement hybrid cache strategy: GitHub Actions cache with registry fallback
- Add multi-source cache-from: GHA cache (fast) → Registry cache (reliable)
- Add multi-source cache-to: Write to both GHA and registry caches simultaneously

Benefits:
- Eliminates version mismatch confusion between frontend and backend
- Ensures atomic deployment with synchronized versions across all services
- Improves cache reliability during GitHub Actions service outages
- Simplifies troubleshooting and rollback procedures
- Follows industry best practices for monolithic application versioning

Trade-offs:
- Slightly longer CI time when only one component changes
- Minimal storage impact due to Docker layer caching and deduplication

Resolves version desynchronization in infra/app-stack.yml deployment files

* refactor(ci): 🔧 extract Docker build logic into reusable composite action

- Create .github/actions/docker/build-and-push composite action
- Encapsulate registry login, metadata extraction, build, push, and summary
- Implement DRY principle by eliminating code duplication across 3 Docker jobs
- Reduce release.yml by 162 lines (-83% code reduction)
- Maintain hybrid cache strategy (GHA + Registry fallback) in centralized action
- Simplify maintenance: changes to build logic only need single update

Benefits:
- Single source of truth for Docker build configuration
- Consistent behavior across backend, marketing, and webapp builds
- Easier to test, debug, and extend build logic
- Reduced cognitive load when reviewing workflow changes

Technical details:
- Composite action accepts 15+ inputs for full customization
- Outputs include tags, digest, and imageID for downstream jobs
- Preserves all existing functionality (multi-platform, caching, tagging)
- No behavioral changes, pure refactoring

* ci: clean up action.yml by removing unnecessary blank lines

* chore(ci): 🔧 remove dead code from release workflow

- Remove unused git-sha input from docker/build-and-push action
- Remove detect-changes job (outputs no longer consumed)
- Simplify semantic-release job (no longer depends on path filtering)
- Remove 'Components Updated' section from release summary

Rationale:
- git-sha input was never used; type=sha auto-generates from Git context
- After unified versioning, per-component change detection is obsolete
- Semantic-release determines releases via commit messages, not file paths
- Reduces workflow complexity and CI execution time

* chore(ci): 🔒 pin Docker actions to SHA and remove dead GIT_SHA build-arg

Supply Chain Security:
- Pin docker/login-action to 9780b0c (v3.3.0)
- Pin docker/metadata-action to 369eb59 (v5.6.1)
- Pin docker/build-push-action to 48aba3b (v6.10.0)
- SHA pinning prevents supply chain attacks from compromised upstream actions
- Dependabot can still update these via PR

Dead Code Removal:
- Remove GIT_SHA build-arg from backend Docker build (lines 156-158)
- Backend Dockerfile does not declare ARG GIT_SHA nor reference it
- Marketing and webapp builds correctly omit GIT_SHA
- Eliminates misleading unused build argument

Security Impact:
- ✅ Prevents upstream action compromise (supply chain attack)
- ✅ Deterministic builds with explicit version control
- ✅ Cleaner build-args (no dead code passed to Docker)

Rationale:
- Major version pinning (@v3, @v6) allows automatic minor/patch updates
  but exposes workflow to supply chain attacks if upstream is compromised
- SHA pinning with version comments provides maximum security while
  maintaining readability
- Backend Dockerfile validation confirmed no GIT_SHA usage:
  `rg 'ARG GIT_SHA' server/engine/Dockerfile` returns no results

### ⚙️ CI/CD

* docker build ([#444](https://github.com/dallay/cvix/issues/444)) ([50600f3](https://github.com/dallay/cvix/commit/50600f3698a4c7bbe7f8d46bd828e16e27b73f2e)), closes [#437](https://github.com/dallay/cvix/issues/437) [#443](https://github.com/dallay/cvix/issues/443) [#441](https://github.com/dallay/cvix/issues/441)

## [1.6.3](https://github.com/dallay/cvix/compare/v1.6.2...v1.6.3) (2025-12-20)

### 🐛 Bug Fixes

* update environment and configuration files for improved security and clarity ([#439](https://github.com/dallay/cvix/issues/439)) ([cc8bad6](https://github.com/dallay/cvix/commit/cc8bad69cf25fded88c919f9eaf0f8c8eb868f4e))

## [1.6.2](https://github.com/dallay/cvix/compare/v1.6.1...v1.6.2) (2025-12-19)

### 🐛 Bug Fixes

* qodana report suggestions ([#435](https://github.com/dallay/cvix/issues/435)) ([95e303f](https://github.com/dallay/cvix/commit/95e303fde739ee4203e0c8000c905fc6f84b7f29)), closes [#XXX](https://github.com/dallay/cvix/issues/XXX)

## [1.6.1](https://github.com/dallay/cvix/compare/v1.6.0...v1.6.1) (2025-12-17)

### ♻️ Refactors

* **images:** clean up test file and remove unused dependency ([#433](https://github.com/dallay/cvix/issues/433)) ([d183ead](https://github.com/dallay/cvix/commit/d183ead076ec4ec263e744be8f5a08fd96209e9f))

## [1.6.0](https://github.com/dallay/cvix/compare/v1.5.0...v1.6.0) (2025-12-17)

### ✨ Features

* **COD-98:** Implement Waitlist Capture on Landing Page ([#430](https://github.com/dallay/cvix/issues/430)) ([1b26d92](https://github.com/dallay/cvix/commit/1b26d92d3d7867a954ac6c7d3980ea71db05cd20))

### 📝 Documentation

* **COD-33:** rule agent refactor automatic docs ([#431](https://github.com/dallay/cvix/issues/431)) ([0f40731](https://github.com/dallay/cvix/commit/0f40731d02228c7a1dc251847e978936bd3b40bc))

## [1.5.0](https://github.com/dallay/cvix/compare/v1.4.0...v1.5.0) (2025-12-14)

### ✨ Features

* **COD-97:** feature multi source template loading via strategy pattern [#423](https://github.com/dallay/cvix/issues/423) ([#426](https://github.com/dallay/cvix/issues/426)) ([598777b](https://github.com/dallay/cvix/commit/598777bc2a379a928b5fd015dd721808be03983b))

## [1.4.0](https://github.com/dallay/cvix/compare/v1.3.2...v1.4.0) (2025-12-12)

### ✨ Features

* **agents:** ✨ add new agents for database architecture, frontend development, and backend engineering ([#424](https://github.com/dallay/cvix/issues/424)) ([4d5a593](https://github.com/dallay/cvix/commit/4d5a59397caac99e134222919b9b16d6c9beafe2))

### ♻️ Refactors

* code quality suggestions ([#425](https://github.com/dallay/cvix/issues/425)) ([852ff38](https://github.com/dallay/cvix/commit/852ff382c3533d614440c2503b50c5f7dac37197))

## [1.3.2](https://github.com/dallay/cvix/compare/v1.3.1...v1.3.2) (2025-12-10)

### 🐛 Bug Fixes

* form resume loader ([#422](https://github.com/dallay/cvix/issues/422)) ([4fed367](https://github.com/dallay/cvix/commit/4fed367a27fb4da270b19697e3e9c825932bbbc5))

## [1.3.1](https://github.com/dallay/cvix/compare/v1.3.0...v1.3.1) (2025-12-10)

### 🐛 Bug Fixes

* enhance resume form functionality with load and clear methods ([#421](https://github.com/dallay/cvix/issues/421)) ([75fd05b](https://github.com/dallay/cvix/commit/75fd05b08cf60ff592af2b797bdd1b3a02455fd6))

## [1.3.0](https://github.com/dallay/cvix/compare/v1.2.1...v1.3.0) (2025-12-10)

### ✨ Features

* **cod-67:** epic pdf section selector resume customization for pdf [#384](https://github.com/dallay/cvix/issues/384) ([#407](https://github.com/dallay/cvix/issues/407)) ([f7888e5](https://github.com/dallay/cvix/commit/f7888e5047abe9c30dd87c19cb62f2a09318a491)), closes [#383](https://github.com/dallay/cvix/issues/383) [#343](https://github.com/dallay/cvix/issues/343) [#394](https://github.com/dallay/cvix/issues/394) [#395](https://github.com/dallay/cvix/issues/395) [#396](https://github.com/dallay/cvix/issues/396) [#397](https://github.com/dallay/cvix/issues/397) [#398](https://github.com/dallay/cvix/issues/398) [#399](https://github.com/dallay/cvix/issues/399) [#400](https://github.com/dallay/cvix/issues/400) [#401](https://github.com/dallay/cvix/issues/401) [#402](https://github.com/dallay/cvix/issues/402) [#403](https://github.com/dallay/cvix/issues/403) [#404](https://github.com/dallay/cvix/issues/404) [#405](https://github.com/dallay/cvix/issues/405) [#406](https://github.com/dallay/cvix/issues/406) [#408](https://github.com/dallay/cvix/issues/408)

## [1.2.1](https://github.com/dallay/cvix/compare/v1.2.0...v1.2.1) (2025-12-06)

### 🐛 Bug Fixes

* **deps:** update dependency org.junit.jupiter:junit-jupiter-api to v6 ([#349](https://github.com/dallay/cvix/issues/349)) ([ce7c944](https://github.com/dallay/cvix/commit/ce7c944f10edad86758b464ebe5b667219a6ed36))

## [1.2.0](https://github.com/dallay/cvix/compare/v1.1.0...v1.2.0) (2025-12-05)

### ✨ Features

* enhance resume mapping to include highlights and update dependencies ([#379](https://github.com/dallay/cvix/issues/379)) ([40cd257](https://github.com/dallay/cvix/commit/40cd2572968d0ac9cbfa4950dabeb83fc9e2db0d))

## [1.1.0](https://github.com/dallay/cvix/compare/v1.0.0...v1.1.0) (2025-12-05)

### ✨ Features

* add new blog articles ([#378](https://github.com/dallay/cvix/issues/378)) ([e245cd6](https://github.com/dallay/cvix/commit/e245cd6fbd1f9866c7b30f1744a9adefe559ffd9))

### ♻️ Refactors

* improve documentation and formatting in template mapping functions ([3c5c651](https://github.com/dallay/cvix/commit/3c5c6519269c8e65f9b7f353af86bf79dd7097cf))
