# Multi-stage build for Astro landing page

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
COPY client/apps/marketing/package.json ./client/apps/marketing/

# Install dependencies
RUN LEFTHOOK_SKIP=1 pnpm install --frozen-lockfile

# Copy source code
COPY client/config ./client/config/
COPY client/packages ./client/packages/
COPY client/apps/marketing ./client/apps/marketing/

# Build utilities package first
RUN pnpm --filter @cvix/utilities build

# Build the landing page with environment-specific configuration
ARG BUILD_ENV=production
ENV NODE_ENV=${BUILD_ENV}

# Set any additional environment variables for the build
ARG BASE_URL
ENV PUBLIC_BASE_URL=${BASE_URL}

# Build the application
WORKDIR /app/client/apps/marketing
RUN pnpm build

# Production stage - using nginx for static file serving
FROM nginx:alpine AS production

# Install curl for healthcheck
RUN apk add --no-cache curl

# Create required directories with proper permissions for non-root user
RUN mkdir -p /var/cache/nginx/client_temp \
    /var/cache/nginx/proxy_temp \
    /var/cache/nginx/fastcgi_temp \
    /var/cache/nginx/uwsgi_temp \
    /var/cache/nginx/scgi_temp \
    /tmp/nginx \
    /var/log/nginx \
    && chown -R 101:101 /var/cache/nginx /tmp/nginx /var/log/nginx /etc/nginx/conf.d

# Copy custom nginx configuration (must be done as root before switching user)
COPY client/apps/marketing/nginx.conf /etc/nginx/nginx.conf

# Copy built files from build stage with proper ownership
COPY --from=build --chown=101:101 /app/client/apps/marketing/dist /usr/share/nginx/html

# Expose port 8080 (non-privileged port)
EXPOSE 8080

# Switch to non-root user (nginx)
USER 101:101

# Health check using curl to verify the server is responding
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
