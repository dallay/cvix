#!/bin/bash

# Script to switch between email testing servers (GreenMail and MailDev)
# Usage: ./scripts/switch-mail-server.sh [greenmail|maildev]

set -euo pipefail

# Default to help if no argument is provided
if [ $# -eq 0 ]; then
  echo "Usage: $0 [greenmail|maildev]"
  echo "  greenmail - Switch to GreenMail email server"
  echo "  maildev   - Switch to MailDev email server"
  exit 1
fi

SERVER_TYPE=$1
PROJECT_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)

# Stop any running email servers
echo "Stopping any running email servers..."
docker compose -f "$PROJECT_ROOT/infra/greenmail/greenmail-compose.yml" down 2>/dev/null || true
docker compose -f "$PROJECT_ROOT/infra/maildev/maildev-compose.yml" down 2>/dev/null || true

# Start the requested email server
case $SERVER_TYPE in
  greenmail)
    echo "Starting GreenMail email server..."
    docker compose -f "$PROJECT_ROOT/infra/greenmail/greenmail-compose.yml" up -d
    echo "GreenMail is now running."
    echo "SMTP server: localhost:3025"
    echo "Web interface: http://localhost:8080"
    ;;
  maildev)
    echo "Starting MailDev email server..."
    docker compose -f "$PROJECT_ROOT/infra/maildev/maildev-compose.yml" up -d
    echo "MailDev is now running."
    echo "SMTP server: localhost:1025"
    echo "Web interface: http://localhost:1080"
    ;;
  *)
    echo "Error: Unknown server type '$SERVER_TYPE'"
    echo "Usage: $0 [greenmail|maildev]"
    exit 1
    ;;
esac

echo ""
echo "To update your application configuration, set the following properties:"
echo ""
echo "spring:"
echo "  mail:"
echo "    host: localhost"
if [ "$SERVER_TYPE" = "greenmail" ]; then
  echo "    port: 3025"
else
  echo "    port: 1025"
fi
echo "    protocol: smtp"
echo "    properties:"
echo "      mail:"
echo "        smtp:"
echo "          auth: false"
echo "          starttls:"
echo "            enable: false"
echo ""
echo "Done!"
