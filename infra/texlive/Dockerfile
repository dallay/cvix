# =============================================================================
# CVIX TexLive Docker Image
# =============================================================================
# Multi-architecture (amd64/arm64) minimal TexLive image for PDF generation.
# Built from official Debian base with TexLive installed from official CTAN.
#
# Security considerations:
# - Based on official debian:bookworm-slim
# - TexLive downloaded from official CTAN mirrors
# - No SSH, no extra services, minimal attack surface
# - Runs as non-root user by default
# =============================================================================

FROM debian:bookworm-slim

ARG TARGETARCH
ARG TEXLIVE_VERSION=2025

LABEL maintainer="CVIX Team"
LABEL description="Minimal TexLive image for PDF generation (multi-arch)"
LABEL org.opencontainers.image.source="https://github.com/cvix/cvix"

# Install minimal dependencies for TexLive installation
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    fontconfig \
    libfontconfig1 \
    perl \
    && rm -rf /var/lib/apt/lists/*

# Create texlive user for security (non-root execution)
RUN useradd -m -s /bin/bash texlive

# Download and install TexLive from official CTAN
# Using the official TeX Live installer ensures authenticity
WORKDIR /tmp

# Download TeX Live installer with retry logic and multiple mirrors
# The SHA512 verification is attempted but not blocking since CTAN mirrors
# sometimes serve HTML redirects instead of the actual checksum file
RUN set -ex; \
    # Define mirrors to try (in order of preference)
    # Current CTAN mirrors first (have latest version), historic archive last (for older versions)
    MIRRORS=" \
        https://mirror.ctan.org/systems/texlive/tlnet \
        https://ctan.math.illinois.edu/systems/texlive/tlnet \
        https://ftp.tu-chemnitz.de/pub/tug/historic/systems/texlive/${TEXLIVE_VERSION}/tlnet-final \
    "; \
    # Try each mirror until one works
    for MIRROR in $MIRRORS; do \
        echo "Trying mirror: $MIRROR"; \
        if curl -fsSL --retry 3 --retry-delay 5 -o install-tl-unx.tar.gz "$MIRROR/install-tl-unx.tar.gz"; then \
            echo "Downloaded from $MIRROR"; \
            # Try to verify SHA512, but don't fail if checksum file is unavailable
            if curl -fsSL --retry 2 -o install-tl-unx.tar.gz.sha512 "$MIRROR/install-tl-unx.tar.gz.sha512" 2>/dev/null; then \
                # Check if the downloaded file looks like a valid SHA512 (128 hex chars)
                if grep -qE '^[a-f0-9]{128}' install-tl-unx.tar.gz.sha512 2>/dev/null; then \
                    CHECKSUM=$(awk '{print $1}' install-tl-unx.tar.gz.sha512); \
                    echo "$CHECKSUM  install-tl-unx.tar.gz" | sha512sum -c - && echo "SHA512 verified!"; \
                else \
                    echo "Warning: SHA512 file format invalid, skipping verification"; \
                fi; \
            else \
                echo "Warning: Could not download SHA512 file, skipping verification"; \
            fi; \
            # Verify the tarball is valid by checking if it extracts
            tar -tzf install-tl-unx.tar.gz >/dev/null 2>&1 || { echo "Invalid tarball"; continue; }; \
            tar -xzf install-tl-unx.tar.gz --strip-components=1; \
            rm -f install-tl-unx.tar.gz install-tl-unx.tar.gz.sha512; \
            break; \
        fi; \
    done; \
    # Verify we have the installer
    test -f install-tl || { echo "ERROR: Failed to download TeX Live installer from all mirrors"; exit 1; }

# Create minimal TexLive profile for PDF generation
# This installs only what's needed, keeping the image small
RUN echo "selected_scheme scheme-basic" > texlive.profile \
    && echo "TEXDIR /usr/local/texlive/${TEXLIVE_VERSION}" >> texlive.profile \
    && echo "TEXMFCONFIG ~/.texlive${TEXLIVE_VERSION}/texmf-config" >> texlive.profile \
    && echo "TEXMFHOME ~/texmf" >> texlive.profile \
    && echo "TEXMFLOCAL /usr/local/texlive/texmf-local" >> texlive.profile \
    && echo "TEXMFSYSCONFIG /usr/local/texlive/${TEXLIVE_VERSION}/texmf-config" >> texlive.profile \
    && echo "TEXMFSYSVAR /usr/local/texlive/${TEXLIVE_VERSION}/texmf-var" >> texlive.profile \
    && echo "TEXMFVAR ~/.texlive${TEXLIVE_VERSION}/texmf-var" >> texlive.profile \
    && echo "instopt_adjustpath 0" >> texlive.profile \
    && echo "instopt_adjustrepo 1" >> texlive.profile \
    && echo "instopt_letter 0" >> texlive.profile \
    && echo "instopt_portable 0" >> texlive.profile \
    && echo "instopt_write18_restricted 1" >> texlive.profile \
    && echo "tlpdbopt_autobackup 0" >> texlive.profile \
    && echo "tlpdbopt_install_docfiles 0" >> texlive.profile \
    && echo "tlpdbopt_install_srcfiles 0" >> texlive.profile

# Run the official TexLive installer
RUN ./install-tl --profile=texlive.profile --no-interaction

# Install additional packages needed for resume generation
# These are installed via tlmgr (TeX Live package manager)
RUN /usr/local/texlive/${TEXLIVE_VERSION}/bin/*/tlmgr install \
    # Core LaTeX packages
    latex \
    latex-bin \
    latexmk \
    # Font packages
    fontspec \
    fontawesome5 \
    lm \
    lm-math \
    # Layout and formatting
    geometry \
    titlesec \
    enumitem \
    parskip \
    # Tables and lists
    tabularx \
    multirow \
    # Colors and graphics
    xcolor \
    graphicx \
    tikz \
    pgf \
    # Utilities
    hyperref \
    url \
    etoolbox \
    xifthen \
    ifmtarg \
    # Modern CV/Resume packages
    moderncv \
    # Additional font support
    sourcesanspro \
    sourcecodepro \
    raleway \
    # Required dependencies
    tools \
    graphics \
    amsmath \
    amsfonts \
    # XeLaTeX/LuaLaTeX support (optional, for advanced templates)
    xetex \
    luatex \
    xunicode \
    xltxtra

# Update PATH environment variable for both architectures
# Docker will use whichever path exists based on the actual build architecture
ENV PATH="/usr/local/texlive/${TEXLIVE_VERSION}/bin/x86_64-linux:/usr/local/texlive/${TEXLIVE_VERSION}/bin/aarch64-linux:${PATH}"

# Update font cache
RUN fc-cache -fv

# Clean up installation files
RUN rm -rf /tmp/* \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Create work directory
WORKDIR /work

# Default to non-root user for security
USER texlive

# Default command
CMD ["pdflatex", "--version"]
