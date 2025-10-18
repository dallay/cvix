#!/usr/bin/env bash
set -euo pipefail

# 🎉✨ Gitleaks Secret Checker ✨🎉

# Color codes
RED="\033[0;31m"
GREEN="\033[0;32m"
YELLOW="\033[1;33m"
BLUE="\033[1;34m"
RESET="\033[0m"
BOLD="\033[1m"

GITLEAKS_VERSION="8.18.2"
OS="$(uname -s | tr '[:upper:]' '[:lower:]')"
ARCH="$(uname -m)"
case "$ARCH" in
  x86_64) ARCH="amd64" ;;
  aarch64 | arm64) ARCH="arm64" ;;
  *) echo -e "${RED}❌ Unsupported architecture: $ARCH${RESET}" >&2; exit 1 ;;
esac

TARBALL="gitleaks_${GITLEAKS_VERSION}_${OS}_${ARCH}.tar.gz"
CHECKSUMS_URL="https://github.com/gitleaks/gitleaks/releases/download/v${GITLEAKS_VERSION}/checksums.txt"
TARBALL_URL="https://github.com/gitleaks/gitleaks/releases/download/v${GITLEAKS_VERSION}/${TARBALL}"

INSTALL_DIR="$HOME/.local/bin"
mkdir -p "$INSTALL_DIR"

if ! command -v gitleaks >/dev/null 2>&1; then
  echo -e "${YELLOW}🔍 gitleaks not found, installing to $INSTALL_DIR...${RESET}"
  curl -sSL -O "$TARBALL_URL"
  curl -sSL -O "$CHECKSUMS_URL"
  grep "$TARBALL" checksums.txt > "${TARBALL}.sha256"
  sha256sum -c "${TARBALL}.sha256"
  tar -xzf "$TARBALL" gitleaks
  mv gitleaks "$INSTALL_DIR/"
  chmod +x "$INSTALL_DIR/gitleaks"
  export PATH="$INSTALL_DIR:$PATH"
  echo -e "${GREEN}✅ gitleaks installed successfully!${RESET}"
else
  echo -e "${GREEN}🛡️ gitleaks is already installed!${RESET}"
fi

# Run gitleaks
if gitleaks detect --no-git -v --config=.gitleaks.toml; then
  echo -e "${GREEN}🎉 No secrets detected! Your code is clean. 🚀${RESET}"
else
  echo -e "${RED}🚨 Secrets detected! Please review the report above. 🕵️‍♂️${RESET}"
  exit 1
fi
