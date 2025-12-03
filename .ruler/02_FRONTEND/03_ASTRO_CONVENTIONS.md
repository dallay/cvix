# Astro Conventions

> This document outlines the conventions for working with the Astro framework, primarily for the marketing site.

## Overview

Astro is an all-in-one web framework for building content-focused, high-performance websites:

- **Island Architecture**: Uses server-first design to reduce client-side JavaScript overhead and ship high-performance websites.
- **Content-Focused**: Built-in features like content collections and Markdown support make it excellent for blogs, marketing, and e-commerce sites.
- **HTML-First**: The `.astro` templating syntax provides powerful server rendering in a format that follows HTML standards.
- **Framework Agnostic**: Supports React, Vue, Svelte, Preact, and Solid through official integrations.
- **Powered by Vite**: Fast development server with built-in JavaScript and CSS bundling.

## Project Structure

- Use the default `src/pages`, `src/components`, and `src/layouts` directories.
- Use `src/content` with Astro Collections for all structured content (e.g., blog posts, documentation).

## Components

- Use `.astro` components for layout, structure, and static content.
- Use framework components (e.g., Vue) only for islands of interactivity (`client:*` directives).
- Name components in `PascalCase` (e.g., `HeroBanner.astro`).
- Co-locate styles within component `<style>` blocks.

## Content Management

- Use **Astro Collections** to define schemas and validate frontmatter for all content.
- Use Markdown/MDX for all long-form content.

## Styling

- Use **Tailwind CSS** as the primary utility-first CSS framework.
- Prefer component-scoped styles. Global styles should be minimal and reside in `src/styles/global.css`.

## Performance

- Avoid shipping unnecessary JavaScript to the client. Prefer static HTML where possible.
- Use `client:load`, `client:idle`, or `client:visible` directives strategically to hydrate interactive components.
- Optimize images using Astro's built-in `<Image />` component.

## SEO

- Use the `<Head>` component to inject dynamic meta tags, canonical URLs, and `og:` tags.
- Use structured data (JSON-LD) for relevant content types like articles.

## Documentation Resources

When working with Astro, refer to these official documentation sets:

- [Abridged documentation](https://docs.astro.build/llms-small.txt): Compact version with essential content.
- [Complete documentation](https://docs.astro.build/llms-full.txt): Full documentation for comprehensive reference.
- [API Reference](https://docs.astro.build/_llms-txt/api-reference.txt): Structured descriptions of Astro's APIs.
- [How-to Recipes](https://docs.astro.build/_llms-txt/how-to-recipes.txt): Guided examples for adding features.
- [Build a Blog Tutorial](https://docs.astro.build/_llms-txt/build-a-blog-tutorial.txt): Step-by-step guide to building a basic blog.
- [Deployment Guides](https://docs.astro.build/_llms-txt/deployment-guides.txt): Recipes for deploying to different services.
- [CMS Guides](https://docs.astro.build/_llms-txt/cms-guides.txt): Integration recipes for content management systems.
- [Backend Services](https://docs.astro.build/_llms-txt/backend-services.txt): Integration advice for Firebase, Sentry, Supabase, etc.
- [Migration Guides](https://docs.astro.build/_llms-txt/migration-guides.txt): Advice on migrating from other tools to Astro.
- [Additional Guides](https://docs.astro.build/_llms-txt/additional-guides.txt): E-commerce, authentication, testing, and digital asset management.
- [The Astro Blog](https://astro.build/blog/): Latest news about Astro development.
