# HTML & CSS Conventions

> Guidelines for writing semantic HTML and maintainable CSS with focus on performance, accessibility, and security.

## HTML

### Semantic Structure

Use HTML elements for their intended purpose to improve SEO, accessibility, and maintainability:

| Element | Purpose |
|---------|---------|
| `<nav>` | Navigation menus |
| `<main>` | Primary content (one per page) |
| `<article>` | Self-contained content |
| `<section>` | Thematic groupings |
| `<aside>` | Tangentially related content |
| `<header>` / `<footer>` | Page/section headers and footers |

**Document Structure:**

- Use proper heading hierarchy (`<h1>` to `<h6>`) without skipping levels
- Each page should have exactly one `<h1>`
- Use `<ul>`, `<ol>`, and `<dl>` for appropriate list types
- Use `<label>` elements associated with inputs, `<fieldset>` for grouping, `<legend>` for group labels

### Accessibility (WCAG 2.2 / 3.0 Compliance)

| Requirement | Implementation |
|-------------|----------------|
| **Alternative Text** | All `<img>` elements must have descriptive `alt` attributes. Decorative images use `alt=""` |
| **ARIA Attributes** | Use only when semantic HTML is insufficient. Prefer `<button>` over `<div role="button">` |
| **Keyboard Navigation** | All interactive elements must be keyboard accessible. Use `tabindex="0"` (avoid positive values) |
| **Color Contrast** | WCAG AA minimum: 4.5:1 for normal text, 3:1 for large text |
| **Skip Links** | Provide "Skip to main content" links for keyboard and screen reader users |
| **Form Labels** | All inputs must have associated `<label>` elements or `aria-label` attributes |

### Performance

- **Resource Hints**: Use `<link rel="preconnect">` for critical third-party origins, `<link rel="dns-prefetch">` for others
- **Lazy Loading**: Use `loading="lazy"` for images and iframes below the fold
- **Responsive Images**: Use `<picture>` and `srcset` for art direction and resolution switching
- **Script Loading**: Use `defer` or `async` attributes on `<script>` tags

### DOM Manipulation & Security

#### Safe Element Selection

```javascript
// ✅ Good
document.querySelector('.my-element');
document.querySelectorAll('[data-id]');
element.closest('.parent');
```

#### Safe Content Updates

```javascript
// ✅ Safe - prevents XSS
element.textContent = userInput;
const newDiv = document.createElement('div');
newDiv.textContent = userInput;
element.appendChild(newDiv);

// ✅ Safe with sanitization (when HTML is needed)
import DOMPurify from 'dompurify';
element.innerHTML = DOMPurify.sanitize(userInput);

// ❌ Unsafe - vulnerable to XSS
element.innerHTML = userInput;
element.outerHTML = `<div>${userInput}</div>`;
eval(userCode);
```

#### Avoid Dangerous Methods

- **Never use** `eval()`, `document.write()`, or `innerHTML` with untrusted data
- **Never use** `setTimeout()` or `Function()` with string arguments
- Avoid `element.outerHTML` with user-supplied content
- Implement **Content Security Policy (CSP)** headers

---

## CSS

### Framework & Methodology

- **Primary Framework**: Use **Tailwind CSS** for all styling
- **Design Tokens**: Use Tailwind's `@theme` directive (v4+) for central token definitions
- **Component Organization**: Extract repeated Tailwind patterns into reusable components
- **Custom CSS**: When unavoidable, keep minimal and well-documented:
  - Scope styles to components (`<style scoped>` in Vue)
  - Use BEM-like naming (`block__element--modifier`)
  - Place custom styles in component files, not global stylesheets

### Performance Optimization

| Technique | Implementation |
|-----------|----------------|
| **JIT Mode** | Enable Just-in-Time compilation in Tailwind |
| **Purging** | Ensure proper PurgeCSS/content configuration |
| **Critical CSS** | Inline critical CSS for above-the-fold content |
| **CSS Containment** | Use `contain: layout` or `contain: paint` for isolated components |
| **Will-Change** | Use sparingly, only for elements that will definitely animate |

### Accessibility in CSS

**Focus Indicators:**

```css
button:focus-visible {
  outline: 2px solid currentColor;
  outline-offset: 2px;
}
```

**Reduced Motion:**

```css
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
```

- Test and support **Windows High Contrast Mode** using `@media (prefers-contrast: high)`
- Support system **dark mode** preference using `@media (prefers-color-scheme: dark)` and Tailwind's `dark:` variant

### Units & Sizing

| Use Case | Unit | Reason |
|----------|------|--------|
| Font sizes | `rem` | Respects user font size preferences |
| Spacing | `rem` or `em` | Scales with font size |
| Borders | `px` | 1px should always be 1px |
| Media queries | `em` | Supports browser zoom |
| Line height | unitless | e.g., `1.5` instead of `1.5rem` |

### Color & Contrast

- Define colors **semantically** (primary, secondary, success, error) rather than by value
- Ensure text meets WCAG AA standards (4.5:1 for normal text, 3:1 for large text)
- **Never rely on color alone** to convey information—use icons, text, or patterns as well

### Responsive Design

- **Mobile-First**: Write styles for mobile first, then use `md:`, `lg:`, `xl:` variants
- **Logical Properties**: Use `margin-inline`, `padding-block` for better RTL support
- **Container Queries**: Use for component-level responsive design when appropriate

### Best Practices

| Rule | Description |
|------|-------------|
| Avoid `!important` | Use specificity and proper cascade instead |
| Consistent Spacing | Use Tailwind's spacing scale consistently |
| Color System | Stick to defined palette; avoid arbitrary values |
| Z-Index Management | Define values centrally using Tailwind's z-index scale |
| CSS Specificity | Keep low and consistent for predictable styles |
