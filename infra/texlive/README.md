# CVIX TexLive Docker Image

Multi-architecture Docker image for PDF generation from LaTeX sources.

## Security

This image is built with security as a priority:

- **Official base image**: `debian:bookworm-slim` (official Debian)
- **Official TexLive**: Downloaded directly from [CTAN](https://ctan.org/) (Comprehensive TeX Archive Network)
- **Minimal installation**: Only packages required for PDF generation
- **Non-root user**: Runs as `texlive` user by default
- **No extra services**: No SSH, no web servers, minimal attack surface

## Supported Architectures

| Architecture | Platform                             |
| ------------ | ------------------------------------ |
| `amd64`      | x86_64 / Intel / AMD                 |
| `arm64`      | ARM64 / Apple Silicon / AWS Graviton |

## Building

### Local build (current architecture)

```bash
docker build -t cvix/texlive:latest ./infra/texlive
```

### Multi-architecture build (requires Docker Buildx)

```bash
# Create builder if not exists
docker buildx create --name multiarch --use

# Build and push multi-arch image
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t ghcr.io/dallay/texlive:latest \
  -t dallay/texlive:2025 \
  --push \
  ./infra/texlive
```

## Usage

### Generate PDF from LaTeX

```bash
docker run --rm \
  -v $(pwd):/work \
  cvix/texlive:latest \
  pdflatex -interaction=nonstopmode -halt-on-error resume.tex
```

### Interactive shell

```bash
docker run --rm -it \
  -v $(pwd):/work \
  cvix/texlive:latest \
  /bin/bash
```

## Included Packages

The image includes a minimal set of TeX packages optimized for resume/CV generation:

- **Core**: latex, latex-bin, latexmk
- **Fonts**: fontspec, fontawesome5, lm, sourcesanspro, raleway
- **Layout**: geometry, titlesec, enumitem, parskip
- **Tables**: tabularx, multirow
- **Colors/Graphics**: xcolor, graphicx, tikz, pgf
- **Utilities**: hyperref, url, etoolbox
- **CV Packages**: moderncv

## Image Size

The image is optimized for size by:
- Using `scheme-basic` installation
- Excluding documentation (`install_docfiles 0`)
- Excluding source files (`install_srcfiles 0`)
- Cleaning up after installation

Approximate sizes:
- Compressed: ~800MB
- Uncompressed: ~2GB

## Updating TexLive Version

To update to a new TexLive version, modify the `TEXLIVE_VERSION` build argument:

```bash
docker build --build-arg TEXLIVE_VERSION=2025 -t cvix/texlive:2025 ./infra/texlive
```

## Verification

To verify the image works correctly:

```bash
# Check pdflatex version
docker run --rm cvix/texlive:latest pdflatex --version

# Check architecture
docker run --rm cvix/texlive:latest uname -m
```

## Image Registry and Naming Convention

The authoritative registry for the CVIX TexLive Docker image is `dallay/texlive`. Users should pull the official image from this registry to ensure they are using the latest and verified version. Local builds (`cvix/texlive`) are intended for development and testing purposes only.

### Examples

#### Pulling the Official Image
```bash
docker pull dallay/texlive:latest
```

#### Local Build (Development Only)
```bash
docker build -t cvix/texlive:latest ./infra/texlive
```

### Multi-Architecture Builds
For production use, always pull from `dallay/texlive` as it includes multi-architecture support. The following command is used to build and push multi-arch images:
```bash
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t ghcr.io/dallay/texlive:latest \
  -t dallay/texlive:2025 \
  --push \
  ./infra/texlive
```

## Volume Permission Handling

The CVIX TexLive Docker image runs as a non-root user (`texlive`) by default. This ensures better security but may cause permission issues when writing to mounted volumes (e.g., `/work`).

### User Mapping

The `texlive` user inside the container has the following UID/GID:
- **UID**: 1001
- **GID**: 1001

Ensure that the host directory mounted to `/work` is writable by this UID/GID. You can adjust permissions on the host using:
```bash
sudo chown -R 1001:1001 /path/to/host/directory
```

### Using `--user` Flag

Alternatively, you can run the container with the `--user` flag to map the container user to your host user:
```bash
docker run --rm \
  --user $(id -u):$(id -g) \
  -v $(pwd):/work \
  dallay/texlive:latest \
  pdflatex -interaction=nonstopmode -halt-on-error resume.tex
```

### Recommended Strategies

1. **Adjust Host Permissions**: Use `chown` to ensure the mounted directory is writable by UID/GID 1001.
2. **Use `--user` Flag**: Map the container user to your host user for seamless permission handling.
3. **Pre-generate Files**: If possible, pre-generate files in the mounted directory to avoid write conflicts.

By following these strategies, you can avoid permission issues and ensure smooth operation when using mounted volumes.
