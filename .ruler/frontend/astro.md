# Astro Conventions

> Conventions for working with the Astro framework, primarily for the marketing site.

## Overview

Astro is an all-in-one web framework for building content-focused, high-performance websites:

| Feature | Description |
|---------|-------------|
| **Island Architecture** | Server-first design to reduce client-side JavaScript overhead |
| **Content-Focused** | Built-in content collections and Markdown support |
| **HTML-First** | `.astro` templating syntax follows HTML standards |
| **Framework Agnostic** | Supports React, Vue, Svelte, Preact, and Solid |
| **Powered by Vite** | Fast development server with built-in JS and CSS bundling |

## Project Structure

- Use the default `src/pages`, `src/components`, and `src/layouts` directories
- Use `src/content` with Astro Collections for all structured content (blog posts, documentation)

```text
src/
├── pages/          # File-based routing
├── components/     # Reusable components
├── layouts/        # Page layouts
├── content/        # Content collections (Markdown/MDX)
└── styles/         # Global styles
```

## Components

- Use `.astro` components for layout, structure, and static content
- Use framework components (e.g., Vue) only for **islands of interactivity** (`client:*` directives)
- Name components in `PascalCase` (e.g., `HeroBanner.astro`)
- Co-locate styles within component `<style>` blocks

```astro
---
// HeroBanner.astro
type Props = {
  title: string;
  subtitle?: string;
};

const { title, subtitle } = Astro.props;
---

<section class="hero">
  <h1>{title}</h1>
  {subtitle && <p>{subtitle}</p>}
  <slot />
</section>

<style>
  .hero {
    padding: 4rem 2rem;
    text-align: center;
  }
</style>
```

## Content Management

- Use **Astro Collections** to define schemas and validate frontmatter for all content
- Use Markdown/MDX for all long-form content

```typescript
// src/content/config.ts
import { defineCollection, z } from 'astro:content';

const blog = defineCollection({
  type: 'content',
  schema: z.object({
    title: z.string(),
    description: z.string(),
    publishDate: z.date(),
    author: z.string(),
  }),
});

export const collections = { blog };
```

## Styling

- Use **Tailwind CSS** as the primary utility-first CSS framework
- Prefer component-scoped styles
- Global styles should be minimal and reside in `src/styles/global.css`

## Performance

- **Avoid shipping unnecessary JavaScript** to the client—prefer static HTML where possible
- Use `client:*` directives strategically to hydrate interactive components:

| Directive | When it Hydrates |
|-----------|------------------|
| `client:load` | Immediately on page load |
| `client:idle` | When browser is idle |
| `client:visible` | When component enters viewport |
| `client:media` | When media query matches |
| `client:only` | Only on client, skip SSR |

- Optimize images using Astro's built-in `<Image />` component

```astro
---
import { Image } from 'astro:assets';
import heroImage from '../assets/hero.jpg';
---

<Image src={heroImage} alt="Hero banner" width={1200} height={600} />
```

## SEO

- Use the `<Head>` component to inject dynamic meta tags, canonical URLs, and `og:` tags
- Use structured data (JSON-LD) for relevant content types like articles

```astro
---
// BaseHead.astro
type Props = {
  title: string;
  description: string;
  image?: string;
};

const { title, description, image } = Astro.props;
const canonicalURL = new URL(Astro.url.pathname, Astro.site);
---

<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<link rel="canonical" href={canonicalURL} />
<title>{title}</title>
<meta name="description" content={description} />
<meta property="og:title" content={title} />
<meta property="og:description" content={description} />
{image && <meta property="og:image" content={image} />}
```

## Documentation Resources

When working with Astro, refer to these official documentation sets:

- [Abridged documentation](https://docs.astro.build/llms-small.txt): Compact version with essential content
- [Complete documentation](https://docs.astro.build/llms-full.txt): Full documentation for comprehensive reference
- [API Reference](https://docs.astro.build/_llms-txt/api-reference.txt): Structured descriptions of Astro's APIs
- [How-to Recipes](https://docs.astro.build/_llms-txt/how-to-recipes.txt): Guided examples for adding features
- [Build a Blog Tutorial](https://docs.astro.build/_llms-txt/build-a-blog-tutorial.txt): Step-by-step guide
- [Deployment Guides](https://docs.astro.build/_llms-txt/deployment-guides.txt): Recipes for deploying to different services
- [CMS Guides](https://docs.astro.build/_llms-txt/cms-guides.txt): Integration recipes for content management systems
- [Backend Services](https://docs.astro.build/_llms-txt/backend-services.txt): Integration advice for Firebase, Sentry, Supabase, etc.
- [Migration Guides](https://docs.astro.build/_llms-txt/migration-guides.txt): Advice on migrating from other tools
- [Additional Guides](https://docs.astro.build/_llms-txt/additional-guides.txt): E-commerce, authentication, testing
- [The Astro Blog](https://astro.build/blog/): Latest news about Astro development
