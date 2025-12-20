#!/bin/sh
set -e

# Substitute environment variables in configuration templates
# BACKEND_URL, CSP_SCRIPT_SRC, CSP_STYLE_SRC are expected to be set as environment variables

echo "Substituting environment variables in nginx configuration..."
envsubst '${BACKEND_URL} ${CSP_SCRIPT_SRC} ${CSP_STYLE_SRC}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf
envsubst '${BACKEND_URL} ${CSP_SCRIPT_SRC} ${CSP_STYLE_SRC}' < /etc/nginx/conf.d/security-headers.conf.template > /etc/nginx/conf.d/security-headers.conf

echo "Starting nginx..."
exec nginx -g 'daemon off;'
