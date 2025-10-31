# Design System

> This document defines the unified design system and color token conventions for the frontend applications.

## Overview

The application uses a **token-based design system** built on CSS custom properties (CSS variables) that support both light and dark themes seamlessly. All components must use these semantic tokens instead of hardcoded color values.

## Color Tokens

### Semantic Color Variables

All colors are defined using the OKLCH color space for better perceptual uniformity. Components should always reference these semantic tokens:

#### Core Colors

- **`--background`**: Main background color for the application
- **`--foreground`**: Primary text color
- **`--card`**: Background for card-like components
- **`--card-foreground`**: Text color on cards
- **`--popover`**: Background for popovers and tooltips
- **`--popover-foreground`**: Text color for popovers

#### Interactive Colors

- **`--primary`**: Primary action color (buttons, links)
- **`--primary-foreground`**: Text on primary colored elements
- **`--secondary`**: Secondary action color
- **`--secondary-foreground`**: Text on secondary colored elements
- **`--accent`**: Accent color for highlights
- **`--accent-foreground`**: Text on accented elements

#### Status Colors

- **`--destructive`**: Destructive actions and errors
- **`--destructive-foreground`**: Text for destructive actions
- **`--muted`**: Muted background for subtle elements
- **`--muted-foreground`**: Text for muted elements

#### UI Elements

- **`--border`**: Border color for all components
- **`--input`**: Background for input fields
- **`--ring`**: Focus ring color

#### Sidebar Tokens

- **`--sidebar`**: Sidebar background
- **`--sidebar-foreground`**: Sidebar text color
- **`--sidebar-primary`**: Sidebar primary action color
- **`--sidebar-accent`**: Sidebar hover/active states
- **`--sidebar-border`**: Sidebar borders and separators

## Usage in Tailwind CSS

These tokens are exposed as Tailwind utility classes:

```html
<!-- Background and text -->
<div class="bg-background text-foreground">...</div>

<!-- Cards -->
<div class="bg-card text-card-foreground border border-border">...</div>

<!-- Buttons -->
<button class="bg-primary text-primary-foreground hover:bg-primary/90">...</button>

<!-- Muted elements -->
<p class="text-muted-foreground">...</p>

<!-- Inputs -->
<input class="bg-input border-border focus:ring-ring" />
```

## Dark Mode

The design system automatically adapts to dark mode via the `.dark` class on the root element. Dark mode values are carefully calibrated to maintain consistency:

### Dark Mode Color Hierarchy

In dark mode, colors follow this lightness hierarchy:

1. **Background** (`oklch(0.145 0 0)`) - Darkest, base layer
2. **Card/Popover** (`oklch(0.18 0 0)`) - Slightly elevated
3. **Secondary/Muted/Border** (`oklch(0.22 0 0)`) - Interactive surfaces
4. **Foreground** (`oklch(0.985 0 0)`) - Text, brightest

This creates a subtle, cohesive depth hierarchy throughout the UI.

## Layout Components

### Header

The header uses `bg-card` with `border-border` to maintain consistency:

```vue
<header class="bg-card border-b border-border sticky top-0 z-50">
  <!-- Header content -->
</header>
```

### Sidebar

The sidebar uses dedicated sidebar tokens that automatically match the design system:

```vue
<!-- Sidebar automatically uses --sidebar-* tokens via the Sidebar component -->
```

### Main Content

Main content areas use `bg-background`:

```vue
<main class="bg-background">
  <!-- Content -->
</main>
```

## Anti-Patterns

### ❌ Never Do This

```html
<!-- Hardcoded colors -->
<div class="bg-gray-900 text-white dark:bg-gray-100">...</div>
<div class="border-gray-300 dark:border-gray-700">...</div>

<!-- Hardcoded color values -->
<div style="background-color: #1a1a1a">...</div>
```

### ✅ Always Do This

```html
<!-- Use semantic tokens -->
<div class="bg-card text-card-foreground">...</div>
<div class="border-border">...</div>

<!-- Use CSS variables if needed -->
<div style="background-color: var(--card)">...</div>
```

## Component Guidelines

### Buttons

Use the `Button` component from `@/components/ui/button` which automatically uses the design tokens:

```vue
<Button variant="default">Primary Action</Button>
<Button variant="secondary">Secondary Action</Button>
<Button variant="destructive">Delete</Button>
<Button variant="ghost">Subtle Action</Button>
```

### Forms

Always use semantic tokens for inputs:

```html
<input class="bg-input border-border text-foreground focus:ring-ring" />
```

### Cards

Use the `Card` component or apply card tokens manually:

```html
<div class="bg-card text-card-foreground border border-border rounded-lg">
  <!-- Card content -->
</div>
```

## Maintaining Consistency

When creating new components or pages:

1. **Never use hardcoded color classes** like `bg-gray-900`, `text-white`, `border-gray-300`
2. **Always reference semantic tokens**: `bg-background`, `text-foreground`, `border-border`
3. **Test both light and dark themes** to ensure proper contrast
4. **Use existing UI components** from `@/components/ui` when possible

## Extending the System

If you need to add new colors:

1. Add them to both light and dark theme sections in `globals.css`
2. Expose them in the `@theme inline` block
3. Document them in this file
4. Ensure they follow the OKLCH color space convention

## References

- Color definitions: `client/apps/webapp/src/styles/globals.css`
- UI Components: `client/apps/webapp/src/components/ui/`
- Layout components: `client/apps/webapp/src/layouts/components/`
