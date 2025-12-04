#!/bin/sh
# docker-entrypoint.sh: Export Docker secrets as env vars for Spring Boot

set -e

# List of secrets to map: SECRET_FILE_NAME=ENV_VAR_NAME
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
    export "$secret"="$(cat "$secret_file" | tr -d '\r')"
  fi
done

exec java $JAVA_OPTS -jar app.jar "$@"
