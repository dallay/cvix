#!/bin/bash
# Use KC_HTTP_PORT (6080) which is the actual port Keycloak runs on
exec 3<>/dev/tcp/localhost/6080

echo -e "GET /health/ready HTTP/1.1\nhost: localhost:6080\n" >&3

timeout --preserve-status 1 cat <&3 | grep -m 1 status | grep -m 1 UP
ERROR=$?

exec 3<&-
exec 3>&-

exit $ERROR
