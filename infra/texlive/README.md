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

| Architecture | Platform |
|--------------|----------|
| `amd64` | x86_64 / Intel / AMD |
| `arm64` | ARM64 / Apple Silicon / AWS Graviton |

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
  -t ghcr.io/cvix/texlive:latest \
  -t ghcr.io/cvix/texlive:2024 \
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
docker build --build-arg TEXLIVE_VERSION=2025 -t cvix/texlive:2025 .
```

## Verification

To verify the image works correctly:

```bash
# Check pdflatex version
docker run --rm cvix/texlive:latest pdflatex --version

# Check architecture
docker run --rm cvix/texlive:latest uname -m
```
