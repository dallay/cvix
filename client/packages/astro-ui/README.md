# Astro Starter Kit: Component Package

This is a template for an Astro component library. Use this template for writing components to use in multiple projects or publish to NPM.

```sh
pnpm create astro@latest -- --template component
```

[![Open in StackBlitz](https://developer.stackblitz.com/img/open_in_stackblitz.svg)](https://stackblitz.com/github/withastro/astro/tree/latest/examples/non-html-pages)
[![Open with CodeSandbox](https://assets.codesandbox.io/github/button-edit-lime.svg)](https://codesandbox.io/p/sandbox/github/withastro/astro/tree/latest/examples/non-html-pages)
[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/withastro/astro?devcontainer_path=.devcontainer/component/devcontainer.json)

## 🛑 IMPORTANT: Astro component libraries require Vite SSR configuration

This library exports Astro components (.astro files) directly. To use components from `@cvix/astro-ui` in any other Astro project, you MUST add this package to `vite.ssr.noExternal` in your consumer project's `astro.config.mjs`. If you skip this step, you'll get errors like:

```text
Unknown file extension ".astro" for /path/to/astro-ui/components/Component.astro
```

### ✅ How to fix

In your Astro project's `astro.config.mjs`:

```js
import { defineConfig } from 'astro/config';

export default defineConfig({
  // ...other config
  vite: {
    ssr: {
      noExternal: [
        '@cvix/astro-ui'
      ]
    }
  }
});
```

If you use multiple Astro component libraries, add all of them to `noExternal`.

> This is a current [Astro limitation](https://docs.astro.build/en/reference/publish-to-npm/#styling-and-scripts):
> Astro libraries that ship `.astro` files (not precompiled to `.js`) **MUST** be treated as internal by Vite to work with SSR/build.

## 🚀 Project Structure

Inside of your Astro project, you'll see the following folders and files:

```text
/
├── index.ts
├── src
│   └── MyComponent.astro
├── tsconfig.json
├── package.json
```

The `index.ts` file is the "entry point" for your package. Export your components in `index.ts` to make them importable from your package.

## 🧞 Commands

All commands are run from the root of the project, from a terminal:

| Command       | Action                                                                                                                                                                                                                           |
| :------------ | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `pnpm link`    | Registers this package locally. Run `pnpm link my-component-library` in an Astro project to install your components                                                                                                               |
| `pnpm publish` | [Publishes](https://docs.npmjs.com/creating-and-publishing-unscoped-public-packages#publishing-unscoped-public-packages) this package to NPM. Requires you to be [logged in](https://docs.npmjs.com/cli/v8/commands/pnpm-adduser) |
