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

# Production stage - using nginx for static file serving
FROM nginx:alpine AS production

# Install curl for healthcheck and gettext for envsubst
RUN apk add --no-cache curl gettext

# Create required directories with proper permissions for non-root user
# Nginx uses /tmp/nginx/* for temp files (configured in nginx.conf)
RUN mkdir -p /tmp/nginx \
    /var/log/nginx \
    && chown -R 101:101 /tmp/nginx /var/log/nginx /etc/nginx/conf.d

# Copy nginx configuration templates
COPY client/apps/webapp/nginx.conf.template /etc/nginx/nginx.conf.template
COPY client/apps/webapp/security-headers.conf.template /etc/nginx/conf.d/security-headers.conf.template

# Copy entrypoint script and make it executable
COPY client/apps/webapp/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# Copy built files from build stage with proper ownership
COPY --from=build --chown=101:101 /app/client/apps/webapp/dist /usr/share/nginx/html

# Environment variables for CSP and backend URL (set defaults for local dev)
# Override these in production:
#   BACKEND_URL: https://api.yourdomain.com
#   CSP_SCRIPT_SRC: 'self' 'sha256-HASH1' 'sha256-HASH2' (remove 'unsafe-inline')
#   CSP_STYLE_SRC: 'self' 'sha256-HASH1' (remove 'unsafe-inline')
ENV BACKEND_URL=https://backend.localhost \
    CSP_SCRIPT_SRC="'self' 'unsafe-inline'" \
    CSP_STYLE_SRC="'self' 'unsafe-inline'"

# Expose port 8080 (non-privileged port)
EXPOSE 8080

# Health check using curl to verify the server is responding
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# Entrypoint runs as root to perform config substitution, then starts nginx
# Nginx itself drops privileges based on the user directive in nginx.conf
ENTRYPOINT ["/docker-entrypoint.sh"]
