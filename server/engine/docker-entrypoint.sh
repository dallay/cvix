#!/bin/sh
# docker-entrypoint.sh: Export Docker secrets as env vars for Spring Boot

set -e

# List of secrets to map: SECRET_FILE_NAME=ENV_VAR_NAME
# NOTE: This list must be kept in sync with the secrets defined in cvix-deploy/cvix-stack.yml.
# A CI verification step should validate this synchronization to prevent drift.
# Expected secrets (as of cvix-stack.yml):
#   - database_password -> DATABASE_PASSWORD
#   - client_secret -> CLIENT_SECRET
#   - admin_realm_password -> ADMIN_REALM_PASSWORD
#   - sendgrid_api_key -> SENDGRID_API_KEY
#   - smtp_username -> SMTP_USERNAME
#   - smtp_password -> SMTP_PASSWORD
#   - ssl_keystore_password -> SSL_KEYSTORE_PASSWORD
#   - waitlist_ip_hmac_secret -> WAITLIST_IP_HMAC_SECRET
for secret in \
  DATABASE_PASSWORD \
  CLIENT_SECRET \
  ADMIN_REALM_PASSWORD \
  SENDGRID_API_KEY \
  SMTP_USERNAME \
  SMTP_PASSWORD \
  SSL_KEYSTORE_PASSWORD \
  WAITLIST_IP_HMAC_SECRET

do
  secret_file="/run/secrets/$secret"
  if [ ! -f "$secret_file" ]; then
    echo "ERROR: Required Docker secret '$secret' not found at $secret_file" >&2
    exit 1
  fi
  # Strip CRs from secrets created on Windows/Git or CI systems that use CRLF line endings
  export "$secret"="$(cat "$secret_file" | tr -d '\r')"
done

exec java $JAVA_OPTS -jar app.jar "$@"
