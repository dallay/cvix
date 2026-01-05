#!/bin/bash

# Contact Form Backend Test Script
# Tests the /api/contact endpoint with various scenarios

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="${CVIX_API_URL:-http://localhost:8080}"
ENDPOINT="$BASE_URL/api/contact"

echo "=========================================="
echo "Contact Form Backend Test"
echo "=========================================="
echo "Endpoint: $ENDPOINT"
echo ""

# Test 1: Valid request with hCaptcha test token
echo -e "${YELLOW}Test 1: Valid Contact Form Submission${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$ENDPOINT" \
  -H "Content-Type: application/json" \
  -H "API-Version: v1" \
  -H "Accept-Language: en" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "subject": "Test Subject",
    "message": "This is a test message from the backend test script.",
    "hcaptchaToken": "10000000-aaaa-bbbb-cccc-000000000001"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "201" ]; then
    echo -e "${GREEN}✓ PASSED${NC} - HTTP $HTTP_CODE"
    echo "Response: $BODY"
else
    echo -e "${RED}✗ FAILED${NC} - HTTP $HTTP_CODE"
    echo "Response: $BODY"
fi
echo ""

# Test 2: Missing required field (email)
echo -e "${YELLOW}Test 2: Missing Required Field (email)${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$ENDPOINT" \
  -H "Content-Type: application/json" \
  -H "API-Version: v1" \
  -H "Accept-Language: en" \
  -d '{
    "name": "John Doe",
    "subject": "Test Subject",
    "message": "This is a test message.",
    "hcaptchaToken": "10000000-aaaa-bbbb-cccc-000000000001"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "400" ]; then
    echo -e "${GREEN}✓ PASSED${NC} - HTTP $HTTP_CODE (Expected validation error)"
    echo "Response: $BODY"
else
    echo -e "${RED}✗ FAILED${NC} - HTTP $HTTP_CODE (Expected 400)"
    echo "Response: $BODY"
fi
echo ""

# Test 3: Invalid email format
echo -e "${YELLOW}Test 3: Invalid Email Format${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$ENDPOINT" \
  -H "Content-Type: application/json" \
  -H "API-Version: v1" \
  -H "Accept-Language: en" \
  -d '{
    "name": "John Doe",
    "email": "not-an-email",
    "subject": "Test Subject",
    "message": "This is a test message.",
    "hcaptchaToken": "10000000-aaaa-bbbb-cccc-000000000001"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "400" ]; then
    echo -e "${GREEN}✓ PASSED${NC} - HTTP $HTTP_CODE (Expected validation error)"
    echo "Response: $BODY"
else
    echo -e "${RED}✗ FAILED${NC} - HTTP $HTTP_CODE (Expected 400)"
    echo "Response: $BODY"
fi
echo ""

# Test 4: Message too long
echo -e "${YELLOW}Test 4: Message Too Long (>5000 chars)${NC}"
LONG_MESSAGE=$(printf 'A%.0s' {1..5001})
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$ENDPOINT" \
  -H "Content-Type: application/json" \
  -H "API-Version: v1" \
  -H "Accept-Language: en" \
  -d "{
    \"name\": \"John Doe\",
    \"email\": \"john.doe@example.com\",
    \"subject\": \"Test Subject\",
    \"message\": \"$LONG_MESSAGE\",
    \"hcaptchaToken\": \"10000000-aaaa-bbbb-cccc-000000000001\"
  }")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "400" ]; then
    echo -e "${GREEN}✓ PASSED${NC} - HTTP $HTTP_CODE (Expected validation error)"
    echo "Response: $BODY"
else
    echo -e "${RED}✗ FAILED${NC} - HTTP $HTTP_CODE (Expected 400)"
    echo "Response: $BODY"
fi
echo ""

# Test 5: Spanish localization
echo -e "${YELLOW}Test 5: Spanish Localization (Accept-Language: es)${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$ENDPOINT" \
  -H "Content-Type: application/json" \
  -H "API-Version: v1" \
  -H "Accept-Language: es" \
  -d '{
    "name": "Juan Pérez",
    "email": "juan.perez@example.com",
    "subject": "Asunto de prueba",
    "message": "Este es un mensaje de prueba en español.",
    "hcaptchaToken": "10000000-aaaa-bbbb-cccc-000000000001"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "201" ]; then
    echo -e "${GREEN}✓ PASSED${NC} - HTTP $HTTP_CODE"
    echo "Response: $BODY"
    if echo "$BODY" | grep -q "Gracias por contactarnos"; then
        echo -e "${GREEN}✓ Spanish message detected${NC}"
    else
        echo -e "${YELLOW}⚠ Spanish message not detected (might be English fallback)${NC}"
    fi
else
    echo -e "${RED}✗ FAILED${NC} - HTTP $HTTP_CODE"
    echo "Response: $BODY"
fi
echo ""

# Test 6: Missing hCaptcha token
echo -e "${YELLOW}Test 6: Missing hCaptcha Token${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$ENDPOINT" \
  -H "Content-Type: application/json" \
  -H "API-Version: v1" \
  -H "Accept-Language: en" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "subject": "Test Subject",
    "message": "This is a test message."
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "400" ]; then
    echo -e "${GREEN}✓ PASSED${NC} - HTTP $HTTP_CODE (Expected validation error)"
    echo "Response: $BODY"
else
    echo -e "${RED}✗ FAILED${NC} - HTTP $HTTP_CODE (Expected 400)"
    echo "Response: $BODY"
fi
echo ""

echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo -e "${YELLOW}Note: Test 1 and 5 might fail if:${NC}"
echo "  - Backend is not running"
echo "  - Environment variables are not set"
echo "  - hCaptcha secret key is not configured"
echo "  - n8n webhook is unreachable"
echo ""
echo -e "${GREEN}Tests 2, 3, 4, 6 should pass if validation works${NC}"
echo ""
