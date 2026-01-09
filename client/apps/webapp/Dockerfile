# Multi-stage build for Vue.js webapp

# Build stage
FROM node:24-alpine AS build

# Install pnpm
RUN corepack enable && corepack prepare pnpm@10.27.0 --activate

# Install git
RUN apk add --no-cache git

# Set working directory
WORKDIR /app

# Copy package files for dependency installation
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY client/config/package.json ./client/config/
COPY client/packages/tsconfig/package.json ./client/packages/tsconfig/
COPY client/packages/utilities/package.json ./client/packages/utilities/
COPY client/packages/ui/package.json ./client/packages/ui/
COPY client/packages/lib/package.json ./client/packages/lib/
COPY client/apps/webapp/package.json ./client/apps/webapp/

# Install dependencies
RUN LEFTHOOK_SKIP=1 pnpm install --frozen-lockfile

# Copy source code
COPY client/config ./client/config/
COPY client/packages ./client/packages/
COPY client/apps/webapp ./client/apps/webapp/

# Build utilities package first
RUN pnpm --filter @cvix/utilities build

# Build the landing page with environment-specific configuration
ARG BUILD_ENV=production
ENV NODE_ENV=${BUILD_ENV}

# Set any additional environment variables for the build
ARG BASE_URL
ENV PUBLIC_BASE_URL=${BASE_URL}

# Build the application
WORKDIR /app/client/apps/webapp
RUN pnpm build

# =============================================================================
# Production stage - using Caddy for serving static files
# =============================================================================
# Why Caddy?
# - Non-root by default (runs as UID 1000)
# - Native environment variable support in Caddyfile (no envsubst needed)
# - Logs to stdout/stderr by default (Docker native)
# - Automatic compression (gzip/zstd)
# - Simple, readable configuration
# - Automatic HTTPS if needed (not used here, but available)
# =============================================================================
FROM caddy:2-alpine AS production

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy Caddy configuration
COPY client/apps/webapp/Caddyfile /etc/caddy/Caddyfile

# Copy built files from build stage
COPY --from=build /app/client/apps/webapp/dist /srv

# Environment variables for CSP and backend URL (set defaults for local dev)
# Override these in production:
#   CVIX_API_URL: https://api.yourdomain.com
#   CSP_SCRIPT_SRC: 'self' 'sha256-HASH1' 'sha256-HASH2' (remove 'unsafe-inline')
#   CSP_STYLE_SRC: 'self' 'sha256-HASH1' (remove 'unsafe-inline')
#   CSP_CONNECT_SRC: 'self' https://api.yourdomain.com https://auth.yourdomain.com
#   CSP_OBJECT_SRC: 'self' blob: (for PDF preview), or 'none' for strict security
# Note: frame-src is hardcoded to 'self' blob: in Caddyfile for PDF iframe preview
ENV CVIX_API_URL=https://backend.localhost \
    CSP_SCRIPT_SRC="'self' 'unsafe-inline'" \
    CSP_STYLE_SRC="'self' 'unsafe-inline'" \
    CSP_CONNECT_SRC="'self' https://backend.localhost" \
    CSP_OBJECT_SRC="'self' blob:"

# Expose port 8080 (non-privileged port)
EXPOSE 8080

# Health check using curl to verify the server is responding
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# Caddy runs as non-root by default and handles signals properly
CMD ["caddy", "run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
