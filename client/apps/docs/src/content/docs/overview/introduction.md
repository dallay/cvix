---
title: "Overview"
description: "An overview of the ProFileTailors platform."
---

## What is ProFileTailors?

ProFileTailors is a production-grade platform for creating, customizing, and exporting professional résumés. It is designed with a modern, robust architecture to provide a seamless user experience and a solid foundation for developers.

### Key Features

- **Full-featured résumé generator:** Interactive web forms, dynamic previews, and fast PDF export.
- **Modern SPA frontend:** Built with Vue 3, TypeScript, and Tailwind CSS.
- **Reactive, modular backend:** Kotlin + Spring Boot 3 with a clean, hexagonal architecture.
- **Security-first:** OAuth2 SSO via Keycloak and strict data privacy measures.

## Goal

The goal of ProFileTailors is to provide a high-quality, open-source resume generation tool that simplifies the process of creating professional documents while offering developers a powerful and extensible platform to build upon.

## Project Structure

The repository is organized as a monorepo with the following high-level structure:

```text
├── client/   # Frontend: Vue SPA, Astro marketing, Docs site, UI lib
├── server/   # Backend: Spring Boot, Kotlin, PostgreSQL
├── shared/   # Kotlin shared libs
├── infra/    # Infra as code: Docker Compose, secrets, monitoring
├── docs/     # Astro-powered docs site (this folder)
├── .agents/  # AI agent configs, skills & workflow guides
```

Proceed to the "Quick Start" section to run the project locally.
