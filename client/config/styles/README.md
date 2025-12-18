# Centralized CSS Variables & Styles

This directory contains **centralized CSS design tokens and base styles** used across all apps in the monorepo.

## 📂 File Structure

```text
client/config/styles/
├── index.css         # Main entry point (imports all files)
├── global.css        # Tailwind base config
├── variables.css     # ✨ Design tokens (CSS custom properties)
└── base.css          # ✨ Base styles (scrollbars, resets, global elements)
```

## 🎯 Purpose

**Problem Solved:** Before this refactor, CSS variables were **duplicated** in 3 different files:
- `client/apps/webapp/src/styles/globals.css`
- `client/apps/marketing/src/styles/global.css`
- `client/packages/ui/src/assets/index.css`

This caused:
- ❌ Maintenance nightmare (update 3 places for 1 change)
- ❌ Risk of desynchronization
- ❌ Difficult to ensure design consistency

**Solution:** All design tokens are now defined in **ONE place**:
- ✅ `variables.css` - All CSS custom properties (colors, radius, sidebar, charts)
- ✅ `base.css` - All base styles (scrollbars, resets, global elements)

## 📝 Files Explained

### `variables.css`

**Contains:** All CSS custom properties (design tokens)

- **Design tokens for light mode** (`:root`)
- **Design tokens for dark mode** (`.dark`)
- **Tailwind theme integration** (`@theme inline`)
- **Animation keyframes** (accordion animations)

**DO NOT edit app-specific CSS files to add variables. Edit this file instead.**

### `base.css`

**Contains:** Global base styles

- **Global element resets** (`@layer base`)
- **Custom scrollbars** (WebKit & Firefox)
- **Dark mode scrollbar overrides**
- **Responsive scrollbar styles** (mobile, code blocks)

**DO NOT duplicate scrollbar styles in apps. Import this file instead.**

### `global.css`

**Contains:** Tailwind CSS base configuration

- Tailwind imports
- Custom variants for dark mode
- Typography plugin

**This file is minimal and should stay that way.**

### `index.css`

**Main entry point** that imports all other files in the correct order:

1. `global.css` (Tailwind base)
2. `variables.css` (Design tokens)
3. `base.css` (Base styles)

## 🔧 How Apps Use These Styles

### Webapp (`client/apps/webapp/src/styles/globals.css`)

```css
@import "tailwindcss";
@import "tw-animate-css";
@custom-variant dark (&:is(.dark *));
@source "../../../../packages/ui/src";

/* Import centralized design tokens and base styles */
@import "../../../../config/styles/variables.css";
@import "../../../../config/styles/base.css";
```

### Marketing Site (`client/apps/marketing/src/styles/global.css`)

```css
@import "tailwindcss";
@import "tw-animate-css";
@plugin "@tailwindcss/typography";
@custom-variant dark (&:is(.dark *));
@source "../../../../packages/ui/src";

/* Import centralized design tokens and base styles */
@import "../../../../config/styles/variables.css";
@import "../../../../config/styles/base.css";
```

### UI Package (`client/packages/ui/src/assets/index.css`)

```css
@import "tailwindcss";
@import "tw-animate-css";
@custom-variant dark (&:is(.dark *));

/* Import centralized design tokens and base styles */
@import "../../../../config/styles/variables.css";
@import "../../../../config/styles/base.css";
```

## ✅ Benefits

1. **Single Source of Truth** - All design tokens in one place
2. **Easy Maintenance** - Change once, apply everywhere
3. **Consistency Guaranteed** - All apps use the exact same values
4. **No Duplication** - DRY principle applied
5. **Clear Separation** - Design tokens vs base styles vs app-specific styles

## 🚫 Rules

### ❌ DO NOT

- ❌ Copy CSS variables from `variables.css` into app-specific CSS files
- ❌ Duplicate scrollbar styles in app CSS files
- ❌ Define new design tokens in app CSS files

### ✅ DO

- ✅ Import `variables.css` and `base.css` in app CSS files
- ✅ Add new design tokens to `variables.css` only
- ✅ Keep app CSS files minimal (only app-specific styles)

## 🎨 Available Design Tokens

### Colors

```css
/* Core */
--background, --foreground
--card, --card-foreground
--popover, --popover-foreground
--primary, --primary-foreground
--secondary, --secondary-foreground
--muted, --muted-foreground
--accent, --accent-foreground
--destructive
--border, --input, --ring

/* Charts */
--chart-1, --chart-2, --chart-3, --chart-4, --chart-5

/* Sidebar */
--sidebar, --sidebar-foreground
--sidebar-primary, --sidebar-primary-foreground
--sidebar-accent, --sidebar-accent-foreground
--sidebar-border, --sidebar-ring
```

### Radius

```css
--radius-sm: calc(var(--radius) - 4px)
--radius-md: calc(var(--radius) - 2px)
--radius-lg: var(--radius)          /* 0.65rem */
--radius-xl: calc(var(--radius) + 4px)
```

### Tailwind Utilities

All tokens are exposed as Tailwind utilities:

```html
<div class="bg-background text-foreground">...</div>
<div class="bg-card border-border rounded-lg">...</div>
<button class="bg-primary text-primary-foreground">...</button>
```

## 📚 References

- Design system documentation: `.ruler/frontend/design-system.md`
- Tailwind CSS v4 documentation: <https://tailwindcss.com>
- OKLCH color space: <https://oklch.com>

## 🔄 Migration Notes

**Before:**
- 236 lines duplicated in `webapp/globals.css`
- 238 lines duplicated in `marketing/global.css`
- 235 lines duplicated in `ui/index.css`
- **Total: ~700 lines of duplicated code**

**After:**
- `variables.css`: 218 lines (design tokens)
- `base.css`: 133 lines (base styles)
- Each app CSS: ~20 lines (imports only)
- **Total: ~350 lines (50% reduction, 100% consistency)**

---

**Last Updated:** December 2024  
**Maintained By:** Frontend Team
