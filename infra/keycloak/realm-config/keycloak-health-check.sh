#!/bin/bash
# Use KC_HTTP_PORT environment variable with fallback to 6080
PORT=${KC_HTTP_PORT:-6080}

# Attempt to open TCP connection, exit on failure
if ! exec 3<>/dev/tcp/localhost/"$PORT"; then
  echo "ERROR: Failed to connect to Keycloak on localhost:$PORT" >&2
  exit 1
fi

# Send HTTP GET request with proper capitalization
echo -e "GET /health/ready HTTP/1.1\nHost: localhost:$PORT\n" >&3

# Check response for UP status
timeout --preserve-status 1 cat <&3 | grep -m 1 status | grep -m 1 UP
ERROR=$?

# Close file descriptor
exec 3<&-
exec 3>&-

exit $ERROR
