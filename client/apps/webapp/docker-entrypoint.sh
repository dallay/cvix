#!/bin/sh
set -e

# =============================================================================
# Docker Entrypoint for Vue.js Webapp (nginx-unprivileged)
# =============================================================================
# This script:
#   1. Validates required environment variables
#   2. Substitutes variables in nginx config templates using envsubst
#   3. Sets up log forwarding from /tmp/*.log to Docker stdout/stderr
#   4. Drops to non-root user (nginx) and starts nginx
#
# Why log forwarding?
#   - nginx-unprivileged writes logs to /tmp/ (writable by non-root)
#   - Docker expects logs on /dev/stdout and /dev/stderr
#   - In Docker Swarm, /dev/stdout is not writable by non-root users
#   - Solution: Use tail -F to forward logs in background
# =============================================================================

echo "Validating required environment variables..."

# Pre-check: Ensure all required environment variables are set
MISSING_VARS=""
[ -z "$BACKEND_URL" ] && MISSING_VARS="$MISSING_VARS BACKEND_URL"
[ -z "$CSP_SCRIPT_SRC" ] && MISSING_VARS="$MISSING_VARS CSP_SCRIPT_SRC"
[ -z "$CSP_STYLE_SRC" ] && MISSING_VARS="$MISSING_VARS CSP_STYLE_SRC"

if [ -n "$MISSING_VARS" ]; then
  echo "❌ ERROR: Required environment variables are not set:$MISSING_VARS" >&2
  echo "" >&2
  echo "Please set the following variables:" >&2
  echo "  BACKEND_URL       - Backend API endpoint (e.g., https://api.example.com)" >&2
  echo "  CSP_SCRIPT_SRC    - CSP script-src directive (e.g., 'self' 'unsafe-inline')" >&2
  echo "  CSP_STYLE_SRC     - CSP style-src directive (e.g., 'self' 'unsafe-inline')" >&2
  echo "" >&2
  echo "See Dockerfile ENV section or infra/.env.example for defaults." >&2
  exit 1
fi

echo "✅ All required environment variables are set"
echo "   BACKEND_URL: $BACKEND_URL"
echo "   CSP_SCRIPT_SRC: $CSP_SCRIPT_SRC"
echo "   CSP_STYLE_SRC: $CSP_STYLE_SRC"

echo "Substituting environment variables in nginx configuration..."
envsubst '${BACKEND_URL} ${CSP_SCRIPT_SRC} ${CSP_STYLE_SRC}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf
envsubst '${BACKEND_URL} ${CSP_SCRIPT_SRC} ${CSP_STYLE_SRC}' < /etc/nginx/conf.d/security-headers.conf.template > /etc/nginx/conf.d/security-headers.conf

echo "✅ Configuration files generated successfully"

# Create empty log files (nginx needs them to exist before tail -F)
touch /tmp/access.log /tmp/error.log
chown nginx:nginx /tmp/access.log /tmp/error.log

# Create /var/log/nginx symlinks to /tmp to suppress nginx startup warning
# nginx binary tries to open /var/log/nginx/error.log BEFORE reading config
mkdir -p /var/log/nginx
ln -sf /tmp/error.log /var/log/nginx/error.log
ln -sf /tmp/access.log /var/log/nginx/access.log
chown -R nginx:nginx /var/log/nginx

# Set up signal trap for graceful shutdown
cleanup() {
  echo "Shutting down..."
  kill $(jobs -p) 2>/dev/null || true
  wait 2>/dev/null || true
  exit 0
}
trap cleanup SIGTERM SIGINT

# Set up log forwarding to Docker stdout/stderr
# This runs as root (PID 1 owns stdout/stderr) and forwards logs from nginx
echo "Setting up log forwarding to Docker stdout/stderr..."
tail -F /tmp/access.log &
tail -F /tmp/error.log >&2 &

# Start nginx as non-root user
# nginx-unprivileged is already configured to run as UID 101 (nginx user)
echo "Starting nginx as non-root user..."
exec su-exec nginx nginx -g 'daemon off;'
