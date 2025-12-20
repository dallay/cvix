#!/bin/sh
set -e

# Substitute environment variables in configuration templates
# BACKEND_URL, CSP_SCRIPT_SRC, CSP_STYLE_SRC are required to be set

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

# Drop privileges and start nginx as non-root user
echo "Starting nginx as non-root user (UID 101)..."
exec su-exec 101:101 nginx -g 'daemon off;'
