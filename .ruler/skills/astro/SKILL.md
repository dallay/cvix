---
name: astro
description: >
  Astro framework patterns and best practices for content-focused sites.
  Trigger: When working with .astro files, content collections, or Astro routing.
allowed-tools: Read, Edit, Write, Glob, Grep, Bash
metadata:
  author: cvix
  version: "1.0"
---

# Astro Framework Skill

Conventions for building content-focused, high-performance websites with Astro.

## When to Use

- Creating or modifying `.astro` components
- Working with content collections (blog, docs)
- Configuring Astro routing and layouts
- Optimizing performance with island architecture
- Integrating Vue components as islands

## Critical Patterns

### 1. Island Architecture - Server First

**ALWAYS minimize client-side JavaScript**. Astro ships zero JS by default.

```astro
---
// ✅ Static component - NO JS shipped
import Header from '../components/Header.astro';
---
<Header title="Welcome" />

---
// ✅ Interactive island - JS only for this component
import Counter from '../components/Counter.vue';
---
<Counter client:visible />
```

### 2. Hydration Directives

| Directive                           | When to Use                               |
|-------------------------------------|-------------------------------------------|
| `client:load`                       | Critical interactivity needed immediately |
| `client:idle`                       | Non-critical, can wait for browser idle   |
| `client:visible`                    | Below the fold, hydrate on scroll         |
| `client:media="(max-width: 768px)"` | Mobile-only interactivity                 |
| `client:only="vue"`                 | No SSR, client-only rendering             |

**Default choice**: `client:visible` unless there's a reason otherwise.

### 3. Component Structure

```astro
---
// 1. Type definitions FIRST
type Props = {
  title: string;
  description?: string;
  isActive?: boolean;
};

// 2. Props destructuring with defaults
const { title, description, isActive = false } = Astro.props;

// 3. Imports and logic
import { getCollection } from 'astro:content';
const posts = await getCollection('blog');
---

<!-- 4. Template -->
<section class:list={["hero", { active: isActive }]}>
  <h1>{title}</h1>
  {description && <p>{description}</p>}
  <slot />
</section>

<!-- 5. Scoped styles -->
<style>
  .hero {
    padding: var(--space-8);
  }
</style>
```

### 4. Content Collections

**ALWAYS define schemas for type safety**:

```typescript
// src/content/config.ts
import { defineCollection, z } from 'astro:content';

const blog = defineCollection({
  type: 'content',
  schema: z.object({
    title: z.string(),
    description: z.string(),
    publishDate: z.coerce.date(),
    author: z.string(),
    tags: z.array(z.string()).default([]),
    draft: z.boolean().default(false),
  }),
});

export const collections = { blog };
```

### 5. Image Optimization

**ALWAYS use Astro's Image component**:

```astro
---
import { Image } from 'astro:assets';
import heroImage from '../assets/hero.jpg';
---

<!-- ✅ Optimized, responsive -->
<Image
  src={heroImage}
  alt="Hero banner"
  width={1200}
  height={600}
  format="webp"
/>

<!-- ❌ NEVER use raw img for local images -->
<img src="/hero.jpg" alt="Hero" />
```

## Project Structure (This Project)

```markdown
client/apps/{marketing,blog}/
├── src/
│   ├── pages/          # File-based routing
│   ├── components/     # .astro and Vue components
│   ├── layouts/        # BaseLayout.astro, etc.
│   ├── content/        # Collections (blog posts, docs)
│   └── styles/         # Global CSS
├── public/             # Static assets
└── astro.config.mjs
```

## Anti-Patterns

❌ **Shipping unnecessary JS** - Use `.astro` for static content
❌ **Using `client:load` everywhere** - Most components don't need immediate hydration
❌ **Raw `<img>` tags** - Use `<Image>` for optimization
❌ **Inline styles for everything** - Use scoped `<style>` blocks
❌ **Hardcoded meta tags** - Use a reusable `BaseHead.astro`

## SEO Pattern

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
<meta property="og:url" content={canonicalURL} />
{image && <meta property="og:image" content={new URL(image, Astro.site)} />}
```

## Commands

```bash
# Development
pnpm --filter @cvix/marketing dev
pnpm --filter @cvix/blog dev

# Build
pnpm --filter @cvix/marketing build
pnpm --filter @cvix/blog build

# Preview production build
pnpm --filter @cvix/marketing preview
```

## Integration with Vue

When using Vue components as islands:

```astro
---
import InteractiveWidget from '../components/InteractiveWidget.vue';
---

<!-- Pass props, use appropriate hydration -->
<InteractiveWidget
  client:visible
  title="Dashboard"
  :items={data.items}
/>
```

## Resources

- [Astro Documentation](https://docs.astro.build)
- [Content Collections](https://docs.astro.build/en/guides/content-collections/)
- [Image Optimization](https://docs.astro.build/en/guides/images/)
