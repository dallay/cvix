#!/bin/bash
# verify-secrets-sync.sh: Verify Docker secrets are synchronized between docker-entrypoint.sh and infra/app.yml
# This script should be run in CI to catch drift between the hardcoded list and the compose configuration.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

ENTRYPOINT_FILE="$REPO_ROOT/server/engine/docker-entrypoint.sh"
APP_YML_FILE="$REPO_ROOT/infra/app.yml"

# Validate that required files exist before processing
if [ ! -f "$ENTRYPOINT_FILE" ]; then
  echo "❌ ERROR: Entrypoint file not found: $ENTRYPOINT_FILE" >&2
  exit 1
fi

if [ ! -f "$APP_YML_FILE" ]; then
  echo "❌ ERROR: App YAML file not found: $APP_YML_FILE" >&2
  exit 1
fi

echo "🔍 Verifying Docker secrets synchronization..."
echo "   Entrypoint: $ENTRYPOINT_FILE"
echo "   Compose file: $APP_YML_FILE"
echo ""

# Extract secrets from docker-entrypoint.sh (lines between "for secret in" and "do")
# Robustly capture the full block from "for secret in" until "do"
# Use flexible pattern to handle variable indentation (spaces or tabs)
# Convert from SCREAMING_SNAKE_CASE to snake_case for comparison
ENTRYPOINT_SECRETS=$(awk '/for secret in/,/^do$/ {if ($0 ~ /^[ \t]+[A-Z_]+/) print}' "$ENTRYPOINT_FILE" | \
  sed 's/^ *//' | \
  sed 's/ *\\$//' | \
  tr '[:upper:]' '[:lower:]' | \
  sort)

# Extract secrets from infra/app.yml (keys under "secrets:" section at root level)
# Use AWK with flexible pattern to handle variable indentation (spaces or tabs)
# Extract only the top-level secret names (2-space indented keys that don't have nested indentation)
APP_YML_SECRETS=$(awk '/^secrets:/{flag=1; next} flag && /^[^ \t]/{flag=0} flag && /^  [a-z_]+:$/{gsub(/^  |:$/,"",$0); print}' "$APP_YML_FILE" | sort)

# Validate that extraction succeeded (both lists must be non-empty)
if [ -z "$ENTRYPOINT_SECRETS" ]; then
  echo "❌ ERROR: Could not extract secrets from docker-entrypoint.sh. Check file format and AWK patterns." >&2
  exit 1
fi

if [ -z "$APP_YML_SECRETS" ]; then
  echo "❌ ERROR: Could not extract secrets from infra/app.yml. Check file format and AWK patterns." >&2
  exit 1
fi

echo "Secrets in docker-entrypoint.sh:"
while IFS= read -r secret; do
  [ -n "$secret" ] && echo "  - $secret"
done <<< "$ENTRYPOINT_SECRETS"
echo ""

echo "Secrets in infra/app.yml:"
while IFS= read -r secret; do
  [ -n "$secret" ] && echo "  - $secret"
done <<< "$APP_YML_SECRETS"
echo ""

# Compare the two lists
if [ "$ENTRYPOINT_SECRETS" = "$APP_YML_SECRETS" ]; then
  echo "✅ SUCCESS: Secrets are synchronized between docker-entrypoint.sh and infra/app.yml"
  exit 0
else
  echo "❌ FAILURE: Secrets mismatch detected!"
  echo ""
  echo "Secrets only in docker-entrypoint.sh:"
  comm -23 <(echo "$ENTRYPOINT_SECRETS") <(echo "$APP_YML_SECRETS") | while IFS= read -r secret; do
    [ -n "$secret" ] && echo "  - $secret"
  done
  echo ""
  echo "Secrets only in infra/app.yml:"
  comm -13 <(echo "$ENTRYPOINT_SECRETS") <(echo "$APP_YML_SECRETS") | while IFS= read -r secret; do
    [ -n "$secret" ] && echo "  - $secret"
  done
  echo ""
  echo "Please update both files to include the same set of secrets."
  echo "See server/engine/docker-entrypoint.sh and infra/app.yml"
  exit 1
fi
