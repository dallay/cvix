#!/bin/bash
# verify-secrets-sync.sh: Verify Docker secrets are synchronized between docker-entrypoint.sh and infra/app.yml
# This script should be run in CI to catch drift between the hardcoded list and the compose configuration.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

ENTRYPOINT_FILE="$REPO_ROOT/server/engine/docker-entrypoint.sh"
APP_YML_FILE="$REPO_ROOT/infra/app.yml"

echo "🔍 Verifying Docker secrets synchronization..."
echo "   Entrypoint: $ENTRYPOINT_FILE"
echo "   Compose file: $APP_YML_FILE"
echo ""

# Extract secrets from docker-entrypoint.sh (lines between "for secret in" and "do")
# Convert from SCREAMING_SNAKE_CASE to snake_case for comparison
ENTRYPOINT_SECRETS=$(grep -A 20 "for secret in" "$ENTRYPOINT_FILE" | \
  grep -E "^  [A-Z_]+" | \
  grep -v "^do$" | \
  sed 's/^ *//' | \
  sed 's/ *\\$//' | \
  tr '[:upper:]' '[:lower:]' | \
  sort)

# Extract secrets from infra/app.yml (keys under "secrets:" section at root level)
# Use awk to find the secrets: block and extract keys (two-space indentation)
APP_YML_SECRETS=$(awk '/^secrets:/{flag=1; next} flag && /^[^ ]/{flag=0} flag && /^  [a-z_]+:/{gsub(/:$/,"",$1); print $1}' "$APP_YML_FILE" | \
  sort)

echo "Secrets in docker-entrypoint.sh:"
echo "$ENTRYPOINT_SECRETS" | sed 's/^/  - /'
echo ""

echo "Secrets in infra/app.yml:"
echo "$APP_YML_SECRETS" | sed 's/^/  - /'
echo ""

# Compare the two lists
if [ "$ENTRYPOINT_SECRETS" = "$APP_YML_SECRETS" ]; then
  echo "✅ SUCCESS: Secrets are synchronized between docker-entrypoint.sh and infra/app.yml"
  exit 0
else
  echo "❌ FAILURE: Secrets mismatch detected!"
  echo ""
  echo "Secrets only in docker-entrypoint.sh:"
  comm -23 <(echo "$ENTRYPOINT_SECRETS") <(echo "$APP_YML_SECRETS") | sed 's/^/  - /'
  echo ""
  echo "Secrets only in infra/app.yml:"
  comm -13 <(echo "$ENTRYPOINT_SECRETS") <(echo "$APP_YML_SECRETS") | sed 's/^/  - /'
  echo ""
  echo "Please update both files to include the same set of secrets."
  echo "See server/engine/docker-entrypoint.sh and infra/app.yml"
  exit 1
fi
