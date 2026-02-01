# 📨 CVIX Subscribe Forms (Iframe Host)

The `@cvix/subscribe-forms` project is a lightweight **Astro-powered SSR application** specifically designed to host subscription forms that can be embedded into any website via `<iframe>`. It acts as a bridge between the CVIX API and the end-user's website, providing a seamless, responsive, and styled form experience.

## 🏗 Architecture & Tech Stack

- **Framework**: [Astro](https://astro.build/) (Server-Side Rendering enabled).
- **Styling**: [Tailwind CSS 4](https://tailwindcss.com/) via `@tailwindcss/vite`.
- **UI Library**: Uses `@cvix/astro-ui` for core components like `EmailCaptureForm`.
- **Communication**: Uses the `postMessage` API for cross-origin communication between the iframe and the parent host.

## 📁 Key Components

### 1. Dynamic Form Route (`src/pages/[formId].astro`)
This is the heart of the application. It's a **Server-Side Rendered (SSR)** page that:
- Extracts `formId` from the URL.
- Fetches form configuration (colors, labels, etc.) from the CVIX API.
- Renders the `EmailCaptureForm` component with the fetched data.
- **Auto-Height Logic**: Injects a script that uses `ResizeObserver` to calculate its own height and notifies the parent window via `postMessage('cvix:height', ...)`.

### 2. Integration Script (`public/embed.js`)
A client-side utility for the **parent page** (the site embedding the form). It:
- Listens for messages from the CVIX iframe.
- Automatically adjusts the iframe's height to prevent scrollbars.
- Dispatches custom events (`cvix:ready`, `cvix:submit:success`, etc.) so the parent site can react to form submissions.

### 3. Global Styles (`public/embed.css`)
Provides a baseline reset and essential styling for the form elements to ensure consistency across different embedding environments.

## 🔄 Communication Flow

1. **Parent Page**: Includes an `<iframe>` pointing to `https://subscribe-forms.profiletailors.com/[formId]`.
2. **Astro Server**: Fetches form settings and renders the HTML.
3. **Iframe**: Once loaded, sends a `cvix:ready` message and its initial `height`.
4. **Embed Script**: Receives the height and updates the `<iframe>` style in the parent DOM.
5. **User Action**: When the user subscribes, the form notifies the parent of success or failure.

## 🛠 Local Development

1. **Add host mapping** (optional for pretty domain):
   Add to your `/etc/hosts`:
   `127.0.0.1 subscribe-forms.profiletailors.com`

2. **Start dev server**:
   ```bash
   pnpm install
   pnpm dev
   ```

The app is configured to run on a specific port defined in the CVIX workspace constants (`PORTS.SUBSCRIBE_FORMS`). It also supports **HTTPS by default** if SSL certificates are found in the `infra/` folder.

## ♿ Accessibility & UX
- **WCAG Compliance**: Includes a "Skip to subscription form" link for screen readers.
- **Error Handling**: Custom UI for `404 Not Found` (invalid Form IDs) and network errors.
- **Performance**: Extremely lightweight (minimal JS) to ensure fast loading on third-party sites.
