#!/bin/sh
# docker-entrypoint.sh: Export Docker secrets as env vars for Spring Boot

set -e

# List of secrets to map: SECRET_FILE_NAME=ENV_VAR_NAME
# NOTE: This list must be kept in sync with the secrets defined in infra/app.yml.
# A CI verification step should validate this synchronization to prevent drift.
# Expected secrets (as of infra/app.yml):
#   - client_secret -> CLIENT_SECRET
#   - admin_realm_password -> ADMIN_REALM_PASSWORD
#   - sendgrid_api_key -> SENDGRID_API_KEY
#   - smtp_username -> SMTP_USERNAME
#   - smtp_password -> SMTP_PASSWORD
#   - ssl_keystore_password -> SSL_KEYSTORE_PASSWORD
for secret in \
  CLIENT_SECRET \
  ADMIN_REALM_PASSWORD \
  SENDGRID_API_KEY \
  SMTP_USERNAME \
  SMTP_PASSWORD \
  SSL_KEYSTORE_PASSWORD

do
  secret_file="/run/secrets/$secret"
  if [ -f "$secret_file" ]; then
    # Strip CRs to handle Windows/CI CRLF line endings in secret files
    export "$secret"="$(cat "$secret_file" | tr -d '\r')"
  fi
done

exec java $JAVA_OPTS -jar app.jar "$@"
