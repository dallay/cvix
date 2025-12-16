#!/usr/bin/env bash
# Prepare local developer environment for the cvix monorepo
# - Verifies required tools (Docker, Java, Node, pnpm, Git, Make)
# - Ensures root .env exists (copies from .env.example if missing)
# - Creates .env symlinks for subprojects (server/engine, client/apps/marketing, client/apps/webapp)
# - Designed to be idempotent and easy to extend

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")"/.. && pwd)"
cd "$ROOT_DIR"

# ------------------------------
# Helpers
# ------------------------------
RED="\033[31m"; GREEN="\033[32m"; YELLOW="\033[33m"; CYAN="\033[36m"; BOLD="\033[1m"; RESET="\033[0m"
PASS="${GREEN}✔${RESET}"; FAIL="${RED}✘${RESET}"; WARN="${YELLOW}!${RESET}"

log() { echo -e "$1"; }
section() { echo -e "\n${BOLD}${CYAN}➤ $1${RESET}"; }

has_cmd() { command -v "$1" >/dev/null 2>&1; }

parse_major() {
  # Extract a major version number from an input like: 21.0.3, v22.1.0
  echo "$1" | sed -E 's/^v//' | awk -F. '{print $1}'
}

check_version_ge() {
  # Usage: check_version_ge <have> <need>
  local have_major need_major
  have_major=$(parse_major "$1")
  need_major=$(parse_major "$2")
  [[ "$have_major" -ge "$need_major" ]]
}

# ------------------------------
# 1) Prerequisite checks
# ------------------------------
section "Checking required tools"

missing=0

# Docker
if has_cmd docker; then
  if docker info >/dev/null 2>&1; then
    log "${PASS} docker: running"
  else
    log "${WARN} docker: installed but not running (start Docker Desktop)"
  fi
else
  log "${FAIL} docker: not found"
  missing=$((missing+1))
fi

# Java (JDK 21+)
if has_cmd java; then
  # java -version outputs to stderr
  jv_raw=$(java -version 2>&1 | head -n1)
  jv=$(echo "$jv_raw" | sed -E 's/.*"([0-9]+(\.[0-9]+)*)".*/\1/')
  if [[ -z "$jv" ]]; then jv="unknown"; fi
  if check_version_ge "$jv" 21; then
    log "${PASS} java: $jv (>=21)"
  else
    log "${FAIL} java: $jv (need >=21)"
    missing=$((missing+1))
  fi
else
  log "${FAIL} java: not found (need JDK 21+)"
  missing=$((missing+1))
fi

# Node.js (20+ recommended)
if has_cmd node; then
  nv=$(node -v 2>/dev/null || true)
  if check_version_ge "$nv" 20; then
    log "${PASS} node: $nv (>=20 recommended)"
  else
    log "${WARN} node: $nv (recommend >=20)"
  fi
else
  log "${FAIL} node: not found"
  missing=$((missing+1))
fi

# pnpm (10+)
if has_cmd pnpm; then
  pv=$(pnpm -v 2>/dev/null || true)
  if check_version_ge "$pv" 10; then
    log "${PASS} pnpm: $pv (>=10)"
  else
    log "${FAIL} pnpm: $pv (need >=10)"
    missing=$((missing+1))
  fi
else
  log "${FAIL} pnpm: not found"
  missing=$((missing+1))
fi

# Git
if has_cmd git; then
  log "${PASS} git: $(git --version | awk '{print $3}')"
else
  log "${FAIL} git: not found"
  missing=$((missing+1))
fi

# Make (useful but optional since Makefile wraps commands)
if has_cmd make; then
  log "${PASS} make: $(make -v | head -n1 | awk '{print $3}')"
else
  log "${WARN} make: not found (Makefile targets won't be available)"
fi

# Docker Compose (plugin)
if docker compose version >/dev/null 2>&1; then
  dc_v=$(docker compose version --short 2>/dev/null || docker compose version 2>/dev/null | head -n1 || echo "available")
  log "${PASS} docker compose: $dc_v"
else
  log "${WARN} docker compose: not available (install Docker Desktop 2.20+)"
fi

if [[ $missing -gt 0 ]]; then
  echo ""
  log "${RED}Some required tools are missing. Please install them, then re-run this script.${RESET}"
  if [[ "$(uname -s)" == "Darwin" ]]; then
    log "\nSuggested (macOS):"
    log "  - Docker Desktop: https://www.docker.com/products/docker-desktop/"
    log "  - Java 21 (Temurin): brew install --cask temurin@21"
    log "  - Node.js (via nvm): brew install nvm && nvm install --lts"
    log "  - pnpm: corepack enable && corepack prepare pnpm@latest --activate"
  else
    log "\nSuggested (Linux):"
    log "  - Docker Engine: https://docs.docker.com/engine/install/"
    log "  - Java 21: use your distro package manager (e.g., apt, dnf)"
    log "  - Node.js: via nvm (https://github.com/nvm-sh/nvm)"
    log "  - pnpm: corepack enable && corepack prepare pnpm@latest --activate"
  fi
  exit 1
fi

# ------------------------------
# 2) Ensure root .env
# ------------------------------
section "Ensuring root .env"
if [[ -f .env ]]; then
  log "${PASS} .env exists at repo root"
else
  if [[ -f .env.example ]]; then
    cp .env.example .env
    log "${PASS} created .env from .env.example"
  else
    log "${FAIL} missing .env and .env.example — please provide one"
    exit 1
  fi
fi

# ------------------------------
# 3) Create .env symlinks for subprojects
# ------------------------------
section "Linking .env into subprojects"

# Allow extension via an optional file at repo root: env.symlink-projects.txt
# Each non-empty, non-comment line should be a relative path to a directory
# where a .env symlink should be created.
DEFAULT_LINK_DIRS=(
  "server/engine"
  "client/apps/marketing"
  "client/apps/webapp"
)

LINK_DIRS=()
if [[ -f env.symlink-projects.txt ]]; then
  while IFS= read -r line; do
    [[ -z "$line" || "$line" =~ ^# ]] && continue
    LINK_DIRS+=("$line")
  done < env.symlink-projects.txt
else
  LINK_DIRS=("${DEFAULT_LINK_DIRS[@]}")
fi

for dir in "${LINK_DIRS[@]}"; do
  if [[ ! -d "$dir" ]]; then
    log "${WARN} skip: $dir (directory not found)"
    continue
  fi
  target="$ROOT_DIR/.env"
  dest="$ROOT_DIR/$dir/.env"
  mkdir -p "$ROOT_DIR/$dir"
  ln -sfn "$target" "$dest"
  if [[ -L "$dest" ]]; then
    log "${PASS} linked $dest -> $target"
  else
    log "${FAIL} could not link $dest"
    exit 1
  fi
done

# ------------------------------
# 4) Nice-to-have checks
# ------------------------------
section "Additional checks"

# Check Node version aligns with .nvmrc if present
if [[ -f .nvmrc && -s .nvmrc ]] && has_cmd node; then
  nvmrc_content=$(tr -d '\n' < .nvmrc)
  # Normalize: add 'v' prefix only if not present (strip any leading v/V)
  desired="${nvmrc_content#v}"
  desired="v$desired"
  current="$(node -v 2>/dev/null || true)"
  if [[ -n "$desired" && -n "$current" && "$desired" != "$current" ]]; then
    log "${WARN} Node version mismatch: current $current, desired $desired (from .nvmrc)"
  else
    log "${PASS} Node matches .nvmrc ($current)"
  fi
fi

# Ensure gradle wrapper exists and is executable
if [[ -f ./gradlew ]]; then
  chmod +x ./gradlew || true
  log "${PASS} gradle wrapper present"
else
  log "${WARN} gradle wrapper not found (expected ./gradlew)"
fi

# Ensure all repo shell scripts are executable
if [[ -d ./scripts ]]; then
  chmod +x ./scripts/*.sh 2>/dev/null || true
  log "${PASS} scripts marked executable"
fi

# Detect presence of an npm prepare script to hint Lefthook installation (no jq dependency)
if [[ -f package.json ]]; then
  if grep -q '"prepare"' package.json 2>/dev/null; then
    log "${PASS} npm prepare script detected (hooks via Lefthook will be set on pnpm install)"
  fi
fi

echo -e "\n${BOLD}${GREEN}Environment preparation complete.${RESET}"
