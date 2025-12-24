# Marketing Site (Astro)

> **Astro-powered marketing and landing pages** for ProFileTailors with optimized performance, SEO, and static generation.

---

## Overview

This is the **public-facing marketing site** built with Astro 5 and Vue 3 components. It delivers blazing-fast page loads through static generation while maintaining interactive islands where needed.

### Key Features

- 🚀 **Astro 5**: Static-first with islands architecture for optimal performance
- 🎨 **Tailwind CSS 4**: Utility-first styling with design tokens
- 🧩 **Vue 3 Islands**: Interactive components hydrated only when needed
- 📝 **Markdown/MDX**: Content-focused authoring with component embeds
- 🔍 **SEO Optimized**: Structured data, semantic HTML, and performance best practices
- ♿ **Accessible**: WCAG 2.1 AA compliant

---

## Tech Stack

| Technology       | Purpose                                   |
|------------------|-------------------------------------------|
| **Astro 5**      | Static site generator with partial hydration |
| **Vue 3**        | Interactive components (islands)          |
| **Tailwind CSS** | Utility-first CSS framework               |
| **TypeScript**   | Type-safe development                     |
| **Vite**         | Build tooling and dev server              |

---

## Project Structure

```text
src/
├── components/       # Reusable Astro and Vue components
├── content/          # Markdown/MDX content collections
├── layouts/          # Page layouts
├── pages/            # File-based routing
└── styles/           # Global styles
```

---

## Development

### Prerequisites

- Node.js 20+
- pnpm 10+

### Commands

```bash
# Install dependencies (from repo root or marketing directory)
pnpm install

# Start dev server with hot reload
pnpm dev

# Type checking
pnpm check

# Build for production
pnpm build

# Preview production build
pnpm preview
```

---

## Content Management

### Adding Pages

Create new files in `src/pages/` for automatic routing:

```text
src/pages/
├── index.astro          → /
├── about.astro          → /about
├── pricing.astro        → /pricing
└── blog/
    ├── index.astro      → /blog
    └── [slug].astro     → /blog/[slug]
```

### Content Collections

Structured content lives in `src/content/` with schema validation:

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

---

## Component Guidelines

### Astro Components

Use for **static content** and **layouts**:

```astro
---
// components/HeroSection.astro
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

### Vue Components (Islands)

Use for **interactive elements** with explicit hydration:

```vue
<!-- components/NewsletterForm.vue -->
<script setup lang="ts">
import { ref } from 'vue';

const email = ref('');
const handleSubmit = async () => {
  // Subscription logic
};
</script>

<template>
  <form @submit.prevent="handleSubmit">
    <input v-model="email" type="email" placeholder="Enter your email" />
    <button type="submit">Subscribe</button>
  </form>
</template>
```

```astro
---
// Using the Vue island
import NewsletterForm from '../components/NewsletterForm.vue';
---

<!-- Only hydrate when visible -->
<NewsletterForm client:visible />
```

### Hydration Directives

| Directive         | When to Hydrate                        |
|-------------------|----------------------------------------|
| `client:load`     | Immediately on page load               |
| `client:idle`     | When browser is idle                   |
| `client:visible`  | When component enters viewport         |
| `client:media`    | When media query matches               |
| `client:only`     | Client-side only (no SSR)              |

**Rule of thumb**: Use `client:visible` for below-the-fold interactivity.

---

## Styling

### Design Tokens

Use semantic tokens from the global design system:

```html
<div class="bg-background text-foreground">
  <button class="bg-primary text-primary-foreground">Click me</button>
</div>
```

See `../../config/styles/README.md` for the complete design system.

### Component Styles

- **Scoped styles** in `<style>` blocks for Astro components
- **Tailwind utilities** for rapid development
- **Global styles** in `src/styles/global.css` (minimal)

---

## SEO Best Practices

### Meta Tags

```astro
---
import { SEO } from '@astrojs/seo';
---

<SEO
  title="ProFileTailors - Professional Resume Generator"
  description="Create polished, ATS-friendly resumes in minutes"
  openGraph={{
    basic: {
      title: "ProFileTailors",
      type: "website",
      image: "/og-image.jpg",
    }
  }}
  twitter={{
    creator: "@profiletailors",
  }}
/>
```

### Structured Data

```astro
<script type="application/ld+json">
{
  "@context": "https://schema.org",
  "@type": "SoftwareApplication",
  "name": "ProFileTailors",
  "applicationCategory": "BusinessApplication",
  "offers": {
    "@type": "Offer",
    "price": "0"
  }
}
</script>
```

---

## Performance Optimization

- ✅ **Static Generation**: All pages pre-rendered at build time
- ✅ **Image Optimization**: Astro's `<Image>` component for responsive images
- ✅ **Code Splitting**: Automatic per-page JS bundles
- ✅ **CSS Scoping**: Zero unused CSS shipped to production
- ✅ **Font Optimization**: Preload critical fonts

---

## Deployment

### Build

```bash
pnpm build
```

Output: `dist/` (static files ready for deployment)

### Deploy Targets

- **Vercel**: Zero-config deployment
- **Netlify**: Drag-and-drop or Git integration
- **AWS S3 + CloudFront**: Static hosting
- **GitHub Pages**: Free static hosting

---

## Conventions

- Follow `.ruler/frontend/astro.md` for Astro-specific guidelines
- Follow `.ruler/frontend/html-css.md` for HTML and CSS standards
- Use semantic HTML for accessibility and SEO
- Keep components small and focused

---

## References

- [Astro Documentation](https://docs.astro.build)
- [Tailwind CSS](https://tailwindcss.com/docs)
- [Vue 3 Composition API](https://vuejs.org/guide/introduction.html)
- [Design System](./../config/styles/README.md)
