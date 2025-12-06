# cvix Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-11-16

## Active Technologies
- Kotlin 2.0.20 (backend), TypeScript 5.x (frontend) + Spring Boot 3.3.4 with WebFlux (backend), Vue.js 3.5.17 with Composition API, Pinia 3.0.3, TailwindCSS 4.1.11 (frontend) (005-pdf-section-selector)
- PostgreSQL with R2DBC (backend), SessionStorage/LocalStorage (frontend preference persistence) (005-pdf-section-selector)

- Backend: Kotlin 2.x + Spring Boot 3.3 (WebFlux, R2DBC); Frontend: Vue 3 + TypeScript (Vite) + Frontend: Ajv v8 (+ ajv-formats, ajv-errors) for schema validation; `libphonenumber-js` for phone validation; `lodash.debounce`; optional `@vueuse/core` for shortcuts; `html2pdf.js` for initial client-side PDF. Backend: Spring WebFlux, Spring Security, Spring Data R2DBC, PostgreSQL. (004-resume-data-entry)

## Project Structure

```text
src/
tests/
```

## Commands

npm test && npm run lint

## Code Style

Backend: Kotlin 2.x + Spring Boot 3.3 (WebFlux, R2DBC); Frontend: Vue 3 + TypeScript (Vite): Follow standard conventions

## Recent Changes
- 005-pdf-section-selector: Added Kotlin 2.0.20 (backend), TypeScript 5.x (frontend) + Spring Boot 3.3.4 with WebFlux (backend), Vue.js 3.5.17 with Composition API, Pinia 3.0.3, TailwindCSS 4.1.11 (frontend)

- 004-resume-data-entry: Added Backend: Kotlin 2.x + Spring Boot 3.3 (WebFlux, R2DBC); Frontend: Vue 3 + TypeScript (Vite) + Frontend: Ajv v8 (+ ajv-formats, ajv-errors) for schema validation; `libphonenumber-js` for phone validation; `lodash.debounce`; optional `@vueuse/core` for shortcuts; `html2pdf.js` for initial client-side PDF. Backend: Spring WebFlux, Spring Security, Spring Data R2DBC, PostgreSQL.

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
