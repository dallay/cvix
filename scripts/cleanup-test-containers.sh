#!/usr/bin/env bash
# Script to clean up Testcontainers that may be left running after tests
# This is useful when running tests locally with testcontainers.reuse.enable=true

set -e

echo "🧹 Cleaning up test containers..."

# Stop and remove test containers
containers=(
    "keycloak-tests"
    "greenmail-tests"
)

for container in "${containers[@]}"; do
    if docker ps -a --format '{{.Names}}' | grep -q "^${container}$"; then
        echo "  ➜ Removing container: ${container}"
        docker rm -f "${container}" 2>/dev/null || true
    else
        echo "  ✓ Container ${container} not found (already clean)"
    fi
done

echo "✨ Test containers cleanup complete!"
