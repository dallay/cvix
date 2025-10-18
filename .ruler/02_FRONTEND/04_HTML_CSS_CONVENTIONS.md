# HTML & CSS Conventions

> This document provides guidelines for writing semantic HTML and maintainable CSS.

## HTML

- **Semantic HTML**: Always use HTML elements for their intended purpose. Use `<nav>`, `<main>`, `<article>`, `<section>`, `<aside>`, and `<footer>` to structure the page.
- **Accessibility**: Ensure all interactive elements are accessible. Use `aria-*` attributes where necessary. Images must have descriptive `alt` attributes.
- **DOM Manipulation**: When manipulating the DOM with JavaScript, prefer modern and safe APIs:
  - Find elements with `.querySelector()` and `.closest()`.
  - Manipulate classes with `.classList`.
  - Set text content with `.textContent`.
  - To insert HTML, use `.setHTML()` (when available) or a sanitization library to prevent XSS.
  - Add/remove elements with `.append()`, `.prepend()`, `.before()`, `.after()`.

## CSS

- **Framework**: Use **Tailwind CSS** for all styling. Avoid writing custom CSS files where a utility class can be used.
- **Methodology**: When custom CSS is unavoidable (e.g., for complex animations or third-party library overrides), follow these guidelines:
  - Keep custom CSS minimal.
  - Scope styles to components whenever possible (`<style scoped>` in Vue).
- **Naming**: For the rare cases where custom classes are needed, use a BEM-like naming convention (`block__element--modifier`) to avoid conflicts.
- **Units**: Use `rem` for font sizes and `px` for borders.
