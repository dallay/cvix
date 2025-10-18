# Loomify Docs (Astro + Starlight)

This README explains how to run, edit, and contribute to the documentation site for the Loomify.

## What this is

The `docs/` site is built with Astro and the Starlight integration. It contains the project's reference pages (Overview, Quick Start, Developer Guide, Configuration, Conventions, Changelog) and the site home page at `src/content/docs/index.mdx`.

## Quick local dev

From the repository root or inside `docs/`:

```bash
# from repo root
cd docs
pnpm install
pnpm dev
```

Open the local URL shown by the dev server (usually <http://localhost:4321>) to preview pages.

## Adding or editing content

Content pages live under `src/content/docs/`. Each document must include YAML frontmatter with at least a `title` field. Recommended frontmatter fields:

- `title` (required)
- `description` (recommended)
- `template` (optional, e.g. `splash`)
- `hero` / `actions` (optional for the index page)

Example frontmatter at the top of a markdown file:

```md
---
title: "Quick Start"
description: "How to get the Loomify running locally"
---
```

Follow the Starlight conventions for layout and components. See: <https://starlight.astro.build/>

### Organizing pages

- Put section pages in folders that match the sidebar structure, e.g. `src/content/docs/quick-start/quick-start.md`.
- Use `index.mdx` as the splash/home page. The current file is `src/content/docs/index.mdx` using Starlight `Card` components.
- Assets used by the docs should be stored under `src/assets/` and referenced with relative paths from the content files (e.g. `../../assets/logo.svg`).

## Linting & schema

- Content entries are validated against the collection schema. Every entry needs a `title` in frontmatter; missing fields will cause a sync error.
- Keep an eye on markdown lint rules in CI (headings and blank lines). If CI or `pnpm dev` reports markdown-lint issues, fix spacing and list numbering (the repo uses the `1.` style for ordered lists).

## Common tasks

- Run the docs dev server:

```bash
cd docs
pnpm dev
```

- Build a production version:

```bash
cd docs
pnpm build
```

## Contributing

- Follow the repo's contribution guidelines in the root `CONTRIBUTING.md`.
- Add or update docs alongside code changes when behavior or APIs change.
- Use small, focused PRs for doc updates and include a preview URL if possible.

## Troubleshooting

- InvalidContentEntryDataError: check the failing file for missing `title` frontmatter.
- Blank page / missing component: ensure assets exist at the referenced path and frontmatter `template` is correct.

If you want, I can also:

- Run the docs dev server here and report the local URL and any errors.
- Add a tiny contributing guide for doc authors (frontmatter template + examples).
