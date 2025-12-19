#!/usr/bin/env bash
set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Function to check if container is running as non-root
check_nonroot() {
    local container_name=$1
    local image_name=$2
    
    print_status "$YELLOW" "\n🔍 Testing ${image_name}..."
    
    # Build the image
    print_status "$YELLOW" "  Building image..."
    if docker build -f "client/apps/${container_name}/Dockerfile" -t "${image_name}:test" . > /dev/null 2>&1; then
        print_status "$GREEN" "  ✅ Build successful"
    else
        print_status "$RED" "  ❌ Build failed"
        return 1
    fi
    
    # Check user ID
    print_status "$YELLOW" "  Checking user ID..."
    local user_id
    user_id=$(docker run --rm "${image_name}:test" id -u)
    if [ "$user_id" -eq 101 ]; then
        print_status "$GREEN" "  ✅ Running as UID 101 (non-root)"
    else
        print_status "$RED" "  ❌ Running as UID ${user_id} (expected 101)"
        return 1
    fi
    
    # Start container
    print_status "$YELLOW" "  Starting container..."
    if docker run -d --name "test-${container_name}" -p "8080:8080" "${image_name}:test" > /dev/null 2>&1; then
        print_status "$GREEN" "  ✅ Container started"
    else
        print_status "$RED" "  ❌ Container failed to start"
        docker logs "test-${container_name}" || true
        docker rm -f "test-${container_name}" 2>/dev/null || true
        return 1
    fi
    
    # Wait for container to be ready
    print_status "$YELLOW" "  Waiting for service to be ready..."
    local retries=0
    local max_retries=30
    while [ $retries -lt $max_retries ]; do
        if curl -sf http://localhost:8080/ > /dev/null 2>&1; then
            print_status "$GREEN" "  ✅ Service is responding on port 8080"
            break
        fi
        retries=$((retries + 1))
        sleep 1
    done
    
    if [ $retries -eq $max_retries ]; then
        print_status "$RED" "  ❌ Service did not respond in time"
        docker logs "test-${container_name}"
        docker rm -f "test-${container_name}" 2>/dev/null || true
        return 1
    fi
    
    # Check process user
    print_status "$YELLOW" "  Checking process ownership..."
    local process_user
    process_user=$(docker exec "test-${container_name}" ps aux | grep nginx | grep -v grep | awk '{print $1}' | head -n1)
    if [ "$process_user" = "nginx" ] || [ "$process_user" = "101" ]; then
        print_status "$GREEN" "  ✅ NGINX process running as non-root user"
    else
        print_status "$RED" "  ❌ NGINX process running as: ${process_user}"
        docker rm -f "test-${container_name}" 2>/dev/null || true
        return 1
    fi
    
    # Cleanup
    print_status "$YELLOW" "  Cleaning up..."
    docker stop "test-${container_name}" > /dev/null 2>&1
    docker rm "test-${container_name}" > /dev/null 2>&1
    
    print_status "$GREEN" "✅ All tests passed for ${image_name}!\n"
}

# Main execution
print_status "$GREEN" "🚀 Docker Non-Root User Verification Script"
print_status "$GREEN" "========================================\n"

# Check if we're in the right directory
if [ ! -f "compose.yaml" ]; then
    print_status "$RED" "❌ Error: compose.yaml not found. Please run this script from the project root."
    exit 1
fi

# Test webapp
if ! check_nonroot "webapp" "cvix-webapp"; then
    print_status "$RED" "❌ webapp tests failed"
    exit 1
fi

# Test marketing
if ! check_nonroot "marketing" "cvix-marketing"; then
    print_status "$RED" "❌ marketing tests failed"
    exit 1
fi

print_status "$GREEN" "🎉 All containers are running as non-root users successfully!"
print_status "$GREEN" "========================================\n"

# Optional: Show docker images
print_status "$YELLOW" "📦 Created test images:"
docker images | grep -E "cvix-(webapp|marketing):test" || true
