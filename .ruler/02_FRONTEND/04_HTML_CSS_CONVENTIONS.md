# HTML & CSS Conventions

> This document provides guidelines for writing semantic HTML and maintainable CSS with focus on performance, accessibility, and security.

## HTML

### Semantic Structure

- **Use Semantic Elements**: Always use HTML elements for their intended purpose to improve SEO, accessibility, and maintainability:
  - `<nav>` for navigation menus
  - `<main>` for primary content (one per page)
  - `<article>` for self-contained content
  - `<section>` for thematic groupings
  - `<aside>` for tangentially related content
  - `<header>` and `<footer>` for page/section headers and footers
- **Document Structure**: Use proper heading hierarchy (`<h1>` to `<h6>`) without skipping levels. Each page should have exactly one `<h1>`.
- **Lists**: Use `<ul>`, `<ol>`, and `<dl>` for appropriate content types (unordered, ordered, and definition lists).
- **Forms**: Use `<label>` elements associated with inputs, `<fieldset>` for grouping related controls, and `<legend>` for group labels.

### Accessibility (WCAG 2.2 / 3.0 Compliance)

- **Alternative Text**: All `<img>` elements must have descriptive `alt` attributes. Decorative images should use `alt=""`.
- **ARIA Attributes**: Use ARIA roles, states, and properties only when semantic HTML is insufficient:
  - Prefer semantic HTML over ARIA (`<button>` over `<div role="button">`)
  - Use `aria-label`, `aria-labelledby`, and `aria-describedby` for additional context
  - Implement `aria-live` regions for dynamic content updates
- **Keyboard Navigation**: Ensure all interactive elements are keyboard accessible:
  - Use `tabindex="0"` to add elements to tab order (avoid positive values)
  - Provide visible focus indicators (`:focus-visible`)
  - Support standard keyboard shortcuts (Enter, Space, Escape, Arrow keys)
- **Color Contrast**: Maintain WCAG AA minimum contrast ratios (4.5:1 for normal text, 3:1 for large text).
- **Skip Links**: Provide "Skip to main content" links for keyboard and screen reader users.
- **Form Labels**: All form inputs must have associated `<label>` elements or `aria-label` attributes.

### Performance

- **Resource Hints**: Use `<link rel="preconnect">` for critical third-party origins and `<link rel="dns-prefetch">` for others.
- **Lazy Loading**: Use `loading="lazy"` for images and iframes below the fold.
- **Responsive Images**: Use `<picture>` and `srcset` for art direction and resolution switching.
- **Script Loading**: Use `defer` or `async` attributes on `<script>` tags to avoid blocking rendering.

### DOM Manipulation & Security

When manipulating the DOM with JavaScript, follow these secure practices:

- **Safe Element Selection**:
  - Use `.querySelector()` and `.querySelectorAll()` for selecting elements
  - Use `.closest()` to find ancestor elements
  - Cache DOM references to avoid repeated queries

- **Safe Content Updates**:
  - **Prefer `.textContent`** over `.innerHTML` for plain text to prevent XSS
  - Use `.setAttribute()` for setting attributes
  - Use `.classList.add()`, `.classList.remove()`, `.classList.toggle()` for class manipulation

- **Avoid Dangerous Methods**:
  - **Never use** `eval()`, `document.write()`, or `innerHTML` with untrusted data
  - **Never use** `setTimeout()` or `Function()` with string arguments
  - Avoid `element.outerHTML` with user-supplied content

- **Safe HTML Insertion**:
  - Use `document.createElement()`, `element.appendChild()`, and `.textContent` to build DOM structures
  - Use the Sanitizer API (`element.setHTML()`) when available with proper configuration
  - Use established sanitization libraries (e.g., DOMPurify) for complex HTML
  - Always validate and encode user input before DOM insertion

- **Content Security Policy (CSP)**: Implement CSP headers to restrict script execution sources and prevent inline script execution.

Example of safe DOM manipulation:

```javascript
// ❌ Unsafe - vulnerable to XSS
element.innerHTML = userInput;
element.outerHTML = `<div>${userInput}</div>`;
eval(userCode);

// ✅ Safe - prevents XSS
element.textContent = userInput;
const newDiv = document.createElement('div');
newDiv.textContent = userInput;
element.appendChild(newDiv);

// ✅ Safe with sanitization (when HTML is needed)
import DOMPurify from 'dompurify';
element.innerHTML = DOMPurify.sanitize(userInput);
```

## CSS

### Framework & Methodology

- **Primary Framework**: Use **Tailwind CSS** for all styling. Leverage utility classes to build designs directly in markup.
- **Design Tokens**: Use Tailwind's `@theme` directive (v4+) to define design tokens centrally for consistency across brands and themes.
- **Component Organization**: Extract repeated Tailwind patterns into reusable components rather than creating custom CSS classes.
- **Custom CSS**: When custom CSS is unavoidable (complex animations, third-party overrides), follow these guidelines:
  - Keep custom CSS minimal and well-documented
  - Scope styles to components (`<style scoped>` in Vue, CSS modules in React)
  - Use BEM-like naming (`block__element--modifier`) for custom classes
  - Place custom styles in component files, not global stylesheets

### Performance Optimization

- **JIT Mode**: Enable Just-in-Time (JIT) compilation in Tailwind for optimal performance and smaller bundle sizes.
- **Purging**: Ensure proper PurgeCSS/content configuration to remove unused styles in production.
- **Critical CSS**: Inline critical CSS for above-the-fold content to improve First Contentful Paint (FCP).
- **CSS Containment**: Use `contain: layout` or `contain: paint` for isolated components to improve rendering performance.
- **Will-Change**: Use `will-change` sparingly and only for elements that will definitely animate.

### Accessibility in CSS

- **Focus Indicators**: Always provide visible focus indicators using `:focus-visible` (prefer over `:focus` to avoid mouse-focus indicators):

  ```css
  button:focus-visible {
    outline: 2px solid currentColor;
    outline-offset: 2px;
  }
  ```

- **Prefers-Reduced-Motion**: Respect user preferences for reduced motion:

  ```css
  @media (prefers-reduced-motion: reduce) {
    * {
      animation-duration: 0.01ms !important;
      animation-iteration-count: 1 !important;
      transition-duration: 0.01ms !important;
    }
  }
  ```

- **High Contrast Mode**: Test and support Windows High Contrast Mode using `@media (prefers-contrast: high)`.
- **Dark Mode**: Support system dark mode preference using `@media (prefers-color-scheme: dark)` and Tailwind's `dark:` variant.

### Units & Sizing

- **Font Sizes**: Use `rem` for font sizes to respect user font size preferences (1rem = user's base font size).
- **Spacing**: Use `rem` or `em` for spacing (margin, padding) to scale with font size.
- **Borders**: Use `px` for borders (1px should always be 1px).
- **Media Queries**: Use `em` for breakpoints to support browser zoom.
- **Line Height**: Use unitless values for `line-height` (e.g., `1.5` instead of `1.5rem`).

### Color & Contrast

- **Semantic Colors**: Define colors semantically (primary, secondary, success, error) rather than by value (blue-500).
- **Contrast Ratios**: Ensure text meets WCAG AA standards (4.5:1 for normal text, 3:1 for large text).
- **Color Independence**: Never rely on color alone to convey information (use icons, text, or patterns as well).

### Responsive Design

- **Mobile-First**: Write styles for mobile first, then use `md:`, `lg:`, `xl:` variants for larger screens.
- **Logical Properties**: Use logical properties (`margin-inline`, `padding-block`) instead of directional ones for better internationalization (RTL support).
- **Container Queries**: Use container queries for component-level responsive design when appropriate.

### Best Practices

- **Avoid `!important`**: Use specificity and proper cascade instead of `!important` (unless overriding third-party styles).
- **Consistent Spacing**: Use Tailwind's spacing scale consistently (don't mix arbitrary values unnecessarily).
- **Color System**: Stick to the defined color palette; avoid arbitrary color values.
- **Z-Index Management**: Define z-index values centrally to avoid conflicts (use Tailwind's z-index scale).
- **CSS Specificity**: Keep specificity low and consistent to make styles predictable and maintainable.
