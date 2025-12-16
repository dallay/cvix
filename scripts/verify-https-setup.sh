#!/usr/bin/env bash
# Script to verify HTTPS development setup
# Usage: ./scripts/verify-https-setup.sh

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "🔍 Verifying HTTPS Development Setup..."
echo ""

# Check if certificates exist
CERTS_FOUND=false
echo "1️⃣  Checking SSL certificates..."
if [[ -f "infra/ssl/localhost.pem" && -f "infra/ssl/localhost-key.pem" && -f "infra/ssl/localhost.p12" ]]; then
    echo -e "${GREEN}✅ SSL certificates found${NC}"
    CERTS_FOUND=true
    
    # Check certificate validity
    if openssl x509 -checkend 86400 -noout -in infra/ssl/localhost.pem > /dev/null 2>&1; then
        expiry=$(openssl x509 -enddate -noout -in infra/ssl/localhost.pem | cut -d= -f2)
        echo -e "${GREEN}   Certificate valid until: $expiry${NC}"
    else
        echo -e "${YELLOW}⚠️  Certificate expired or will expire within 24 hours${NC}"
        echo -e "${YELLOW}   Run: cd infra && ./generate-ssl-certificate.sh${NC}"
        CERTS_FOUND=false
    fi
else
    echo -e "${YELLOW}⚠️  SSL certificates not found${NC}"
    echo -e "${YELLOW}   Dev servers will run in HTTP mode (graceful fallback)${NC}"
    echo -e "${YELLOW}   To enable HTTPS: cd infra && ./generate-ssl-certificate.sh${NC}"
fi

echo ""

# Check if mkcert CA is installed
echo "2️⃣  Checking mkcert CA..."
if command -v mkcert &> /dev/null; then
    ca_location=$(mkcert -CAROOT)
    if [[ -f "$ca_location/rootCA.pem" ]]; then
        echo -e "${GREEN}✅ mkcert CA installed at: $ca_location${NC}"
    else
        echo -e "${YELLOW}⚠️  mkcert CA not installed${NC}"
        echo -e "${YELLOW}   Run: mkcert -install${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  mkcert not found${NC}"
    echo -e "${YELLOW}   Install: brew install mkcert (macOS)${NC}"
fi

echo ""

# Check Astro config
echo "3️⃣  Checking Astro HTTPS configuration..."
if grep -q "getHttpsConfig()" client/apps/marketing/astro.config.mjs; then
    echo -e "${GREEN}✅ Astro configured with automatic HTTPS detection${NC}"
    if [[ "$CERTS_FOUND" == true ]]; then
        echo -e "${GREEN}   → Will run on HTTPS${NC}"
    else
        echo -e "${YELLOW}   → Will run on HTTP (no certs)${NC}"
    fi
else
    echo -e "${RED}❌ Astro HTTPS configuration missing getHttpsConfig()${NC}"
    exit 1
fi

echo ""

# Check Vite config (webapp)
echo "4️⃣  Checking Vite (webapp) HTTPS configuration..."
if grep -q "getHttpsConfig()" client/apps/webapp/vite.config.ts; then
    echo -e "${GREEN}✅ Vite (webapp) configured with automatic HTTPS detection${NC}"
    if [[ "$CERTS_FOUND" == true ]]; then
        echo -e "${GREEN}   → Will run on HTTPS${NC}"
    else
        echo -e "${YELLOW}   → Will run on HTTP (no certs)${NC}"
    fi
else
    echo -e "${RED}❌ Vite (webapp) HTTPS configuration missing getHttpsConfig()${NC}"
    exit 1
fi

echo ""

# Check environment variables (if .env exists)
echo "5️⃣  Checking environment variables..."
if [[ -f ".env" ]]; then
    backend_url=$(grep "^BACKEND_URL=" .env | cut -d= -f2 | tr -d '\r\n' || echo "")
    
    if [[ "$backend_url" == "https://localhost:8443" ]]; then
        echo -e "${GREEN}✅ BACKEND_URL correctly set to HTTPS${NC}"
    elif [[ "$backend_url" == "http://localhost:"* ]]; then
        echo -e "${YELLOW}⚠️  BACKEND_URL is HTTP, backend requires HTTPS${NC}"
        echo -e "${YELLOW}   Update .env: BACKEND_URL=https://localhost:8443${NC}"
    else
        echo -e "${YELLOW}⚠️  BACKEND_URL not found in .env${NC}"
        echo -e "${YELLOW}   Add to .env: BACKEND_URL=https://localhost:8443${NC}"
    fi
    
    marketing_url=$(grep "^PUBLIC_BASE_URL_LOCAL=" .env | cut -d= -f2 | tr -d '\r\n' || echo "")
    if [[ "$CERTS_FOUND" == true ]]; then
        if [[ "$marketing_url" == "https://localhost:7766" ]]; then
            echo -e "${GREEN}✅ PUBLIC_BASE_URL_LOCAL correctly set to HTTPS${NC}"
        else
            echo -e "${YELLOW}⚠️  PUBLIC_BASE_URL_LOCAL should be HTTPS (certs found)${NC}"
            echo -e "${YELLOW}   Update .env: PUBLIC_BASE_URL_LOCAL=https://localhost:7766${NC}"
        fi
    else
        if [[ "$marketing_url" == "http://localhost:7766" ]]; then
            echo -e "${GREEN}✅ PUBLIC_BASE_URL_LOCAL correctly set to HTTP (no certs)${NC}"
        else
            echo -e "${YELLOW}⚠️  PUBLIC_BASE_URL_LOCAL should be HTTP (no certs)${NC}"
            echo -e "${YELLOW}   Update .env: PUBLIC_BASE_URL_LOCAL=http://localhost:7766${NC}"
        fi
    fi
    
    webapp_url=$(grep "^PUBLIC_BASE_WEBAPP_URL_LOCAL=" .env | cut -d= -f2 | tr -d '\r\n' || echo "")
    if [[ "$CERTS_FOUND" == true ]]; then
        if [[ "$webapp_url" == "https://localhost:9876" ]]; then
            echo -e "${GREEN}✅ PUBLIC_BASE_WEBAPP_URL_LOCAL correctly set to HTTPS${NC}"
        else
            echo -e "${YELLOW}⚠️  PUBLIC_BASE_WEBAPP_URL_LOCAL should be HTTPS (certs found)${NC}"
            echo -e "${YELLOW}   Update .env: PUBLIC_BASE_WEBAPP_URL_LOCAL=https://localhost:9876${NC}"
        fi
    else
        if [[ "$webapp_url" == "http://localhost:9876" ]]; then
            echo -e "${GREEN}✅ PUBLIC_BASE_WEBAPP_URL_LOCAL correctly set to HTTP (no certs)${NC}"
        else
            echo -e "${YELLOW}⚠️  PUBLIC_BASE_WEBAPP_URL_LOCAL should be HTTP (no certs)${NC}"
            echo -e "${YELLOW}   Update .env: PUBLIC_BASE_WEBAPP_URL_LOCAL=http://localhost:9876${NC}"
        fi
    fi
else
    echo -e "${YELLOW}⚠️  .env file not found${NC}"
    echo -e "${YELLOW}   Copy .env.example to .env${NC}"
fi

echo ""

# Check if ports are available
echo "6️⃣  Checking port availability..."
ports=(7766 8443 9876)
port_names=("Marketing (Astro)" "Backend (Spring Boot)" "Webapp (Vue.js)")

for i in "${!ports[@]}"; do
    port="${ports[$i]}"
    name="${port_names[$i]}"
    
    if lsof -Pi ":$port" -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  Port $port ($name) is already in use${NC}"
        echo -e "${YELLOW}   Kill process: lsof -ti:$port | xargs kill -9${NC}"
    else
        echo -e "${GREEN}✅ Port $port ($name) is available${NC}"
    fi
done

echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
if [[ "$CERTS_FOUND" == true ]]; then
    echo -e "${GREEN}✨ HTTPS Development Setup Verified!${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "📚 Next steps:"
    echo "   1. Start backend: ./gradlew :server:engine:bootRun --args='--spring.profiles.active=dev,tls'"
    echo "   2. Start marketing: cd client/apps/marketing && pnpm dev → https://localhost:7766"
    echo "   3. Start webapp: cd client/apps/webapp && pnpm dev → https://localhost:9876"
else
    echo -e "${YELLOW}⚠️  HTTP Development Mode (No SSL Certificates)${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "📚 Next steps:"
    echo "   1. Generate certificates: cd infra && ./generate-ssl-certificate.sh"
    echo "   2. OR run in HTTP mode: cd client/apps/marketing && pnpm dev → http://localhost:7766"
    echo "   3. Note: Backend requires HTTPS, you'll see mixed content errors in HTTP mode"
fi
echo ""
echo "📖 For more details, see: client/HTTPS_DEVELOPMENT.md"
