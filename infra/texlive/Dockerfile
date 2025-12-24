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
ARG TEXLIVE_VERSION=2024

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

# Download and verify TeX Live installer
RUN curl -L -o install-tl-unx.tar.gz https://mirror.ctan.org/systems/texlive/tlnet/install-tl-unx.tar.gz \
    && curl -L -o install-tl-unx.tar.gz.sha512 https://mirror.ctan.org/systems/texlive/tlnet/install-tl-unx.tar.gz.sha512 \
    && echo "$(cat install-tl-unx.tar.gz.sha512)  install-tl-unx.tar.gz" | sha512sum -c - \
    && tar -xzf install-tl-unx.tar.gz --strip-components=1 \
    && rm install-tl-unx.tar.gz install-tl-unx.tar.gz.sha512

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
