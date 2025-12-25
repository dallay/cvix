# Multi-stage build for Vue.js webapp

# Build stage
FROM node:24-alpine AS build

# Install pnpm
RUN corepack enable && corepack prepare pnpm@10.13.1 --activate

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
# Production stage - using nginx-unprivileged for non-root operation
# =============================================================================
# Why nginx-unprivileged instead of nginx:alpine?
# - Already configured to run as non-root user (UID 101)
# - Default port 8080 (non-privileged)
# - PID file in /tmp/nginx.pid
# - Temp paths already configured in /tmp/
# - No permission issues with /dev/stdout or /dev/stderr
# - Maintained by Nginx Inc. with weekly security updates
# =============================================================================
FROM nginxinc/nginx-unprivileged:alpine AS production

# Switch to root temporarily to install packages and configure
USER root

# Install required packages:
#   curl: for healthcheck
#   gettext: for envsubst (environment variable substitution)
#   su-exec: for dropping privileges after config generation
RUN apk add --no-cache curl gettext su-exec

# Copy nginx configuration templates
COPY client/apps/webapp/nginx.conf.template /etc/nginx/nginx.conf.template
COPY client/apps/webapp/security-headers.conf.template /etc/nginx/conf.d/security-headers.conf.template

# Copy entrypoint script and make it executable
COPY client/apps/webapp/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# Ensure config directory is writable by nginx user for envsubst output
RUN chown -R nginx:nginx /etc/nginx/conf.d

# Copy built files from build stage with proper ownership
COPY --from=build --chown=nginx:nginx /app/client/apps/webapp/dist /usr/share/nginx/html

# Environment variables for CSP and backend URL (set defaults for local dev)
# Override these in production:
#   BACKEND_URL: https://api.yourdomain.com
#   CSP_SCRIPT_SRC: 'self' 'sha256-HASH1' 'sha256-HASH2' (remove 'unsafe-inline')
#   CSP_STYLE_SRC: 'self' 'sha256-HASH1' (remove 'unsafe-inline')
#   CSP_CONNECT_SRC: 'self' https://api.yourdomain.com https://auth.yourdomain.com
#   CSP_OBJECT_SRC: 'self' blob: (for PDF preview), or 'none' for strict security
ENV BACKEND_URL=https://backend.localhost \
    CSP_SCRIPT_SRC="'self' 'unsafe-inline'" \
    CSP_STYLE_SRC="'self' 'unsafe-inline'" \
    CSP_CONNECT_SRC="'self' https://backend.localhost" \
    CSP_OBJECT_SRC="'self' blob:"

# Expose port 8080 (non-privileged port, nginx-unprivileged default)
EXPOSE 8080

# Health check using curl to verify the server is responding
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# Entrypoint handles:
#   1. Validate required environment variables
#   2. Substitute variables in nginx config templates
#   3. Start nginx as non-root user using su-exec
# Note: Entrypoint runs as root initially to perform envsubst,
#       then explicitly switches to nginx user (UID 101) via su-exec before starting nginx.
#       Do NOT add USER directive here - it would break envsubst.
ENTRYPOINT ["/docker-entrypoint.sh"]
