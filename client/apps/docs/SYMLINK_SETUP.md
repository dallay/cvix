# Docs Symlink Setup

## Overview

The documentation content is physically located at `client/apps/docs/src/content/docs/` but is accessible via a symlink at the project root `docs/` for convenience.

This allows:
- Easy access to documentation from the root: `vim docs/index.mdx`
- Proper Starlight integration (expects content in `src/content/docs/`)
- Clean project structure (all apps in `client/apps/`)

## How It Works

```markdown
cvix/
├── docs -> client/apps/docs/src/content/docs  (symlink)
└── client/
    └── apps/
        └── docs/
            └── src/
                └── content/
                    └── docs/  (actual content location)
```

## Automatic Setup

The symlink is **automatically handled** in two ways:

### 1. Git Tracks the Symlink

The symlink is committed to the repository. When you clone the project:

- **macOS/Linux**: Git automatically creates the symlink ✅
- **Windows**: Git creates the symlink **if**:
  - Developer Mode is enabled, OR
  - Git is configured with `core.symlinks=true` and you have SeCreateSymbolicLinkPrivilege
  - Otherwise, Git creates it as a text file containing the link target

### 2. Setup Script Creates It

The `scripts/prepare-env.sh` script verifies and creates the symlink if missing:

```bash
make prepare-env
# OR
bash scripts/prepare-env.sh
```

This runs automatically during `pnpm install` (via the `prepare` hook) and will:
- Check if `docs/` symlink exists
- Verify it points to the correct location
- Create/fix it if missing or incorrect

## Windows Users

### If Symlinks Don't Work

If you can't create symlinks on Windows (no Developer Mode, no admin rights), you have **two options**:

#### Option A: Use Developer Mode (Recommended)
1. Open Settings → Update & Security → For Developers
2. Enable "Developer Mode"
3. Run `git reset --hard` or clone the repo again

#### Option B: Manual Copy (Fallback)
If symlinks are impossible, copy the content directory:

```powershell
# From repository root
xcopy /E /I client\apps\docs\src\content\docs docs
```

⚠️ **Warning**: With manual copy, you need to:
- Edit content in `client/apps/docs/src/content/docs/` (not the `docs/` copy)
- Run the copy command again after making changes
- This is error-prone and not recommended

## Verifying the Symlink

```bash
# Check if it's a symlink
ls -la docs

# Should show:
# lrwxr-xr-x ... docs -> client/apps/docs/src/content/docs

# Test that it works
ls docs/index.mdx
# Should show the file without errors
```

## Troubleshooting

### "docs is not a symlink"

Run:
```bash
rm -rf docs
bash scripts/prepare-env.sh
```

### "Permission denied"

On macOS/Linux, ensure you have write permissions:
```bash
ls -la . | grep docs
```

On Windows, enable Developer Mode (see above).

### "Symlink points to wrong location"

Run:
```bash
bash scripts/prepare-env.sh
```

The script will automatically fix incorrect symlinks.

## For Maintainers

When modifying the docs structure:

1. **Never delete the symlink from git**: It's tracked and needed for all users
2. **Test the setup script**: After structural changes, verify `scripts/prepare-env.sh` still works
3. **Update this document**: If the symlink location changes, update this file

## Alternative Considered (But Not Used)

We considered keeping content at root `docs/` and symlinking **into** the project, but this creates issues:
- Astro/Starlight can't resolve imports from outside the project
- Relative asset paths break
- Content collections must be inside `src/content/`

The current approach (content inside project, symlink at root) is the cleanest solution.
