#!/usr/bin/env bash
# SSL Certificate generation script using mkcert and openssl
# Interactive, idempotent, and suitable for local development.

set -euo pipefail

DEFAULT_DOMAIN="localhost"
DEFAULT_OUTPUT_DIR="./ssl"
DEFAULT_PASSWORD="changeit"
DEFAULT_ALIAS="server"

prompt() {
  local var_name="$1"; local prompt_text="$2"; local default_value="$3"; local is_password="${4:-false}"; local input=""
  if [[ "$is_password" == "true" ]]; then
    read -r -s -p "$prompt_text [$default_value]: " input; echo
  else
    read -r -p "$prompt_text [$default_value]: " input
  fi
  if [[ -z "$input" ]]; then printf -v "$var_name" "%s" "$default_value"; else printf -v "$var_name" "%s" "$input"; fi
}

info() { echo -e "\033[36m[info]\033[0m $*"; }
error() { echo -e "\033[31m[error]\033[0m $*" >&2; }
pass() { echo -e "\033[32m[ok]\033[0m $*"; }

info "SSL Certificate Generation Interactive Setup"

prompt DOMAIN "Enter the domain for the cert" "$DEFAULT_DOMAIN"
prompt OUTPUT_DIR "Enter output directory for certificates" "$DEFAULT_OUTPUT_DIR"
prompt ALIAS "Enter keystore alias name" "$DEFAULT_ALIAS"
prompt PASSWORD "Enter PKCS12 keystore password" "$DEFAULT_PASSWORD" true

mkdir -p "$OUTPUT_DIR"

if ! command -v mkcert >/dev/null 2>&1; then
  error "mkcert is not installed. Install mkcert first: https://github.com/FiloSottile/mkcert"
  exit 1
fi

info "Installing or verifying local CA..."
mkcert -install

info "Generating PEM certificate and key for '$DOMAIN'..."
mkcert -cert-file "$OUTPUT_DIR/$DOMAIN.pem" -key-file "$OUTPUT_DIR/$DOMAIN-key.pem" "$DOMAIN"

info "Creating PKCS#12 keystore with provided password..."
openssl pkcs12 -export \
  -in "$OUTPUT_DIR/$DOMAIN.pem" \
  -inkey "$OUTPUT_DIR/$DOMAIN-key.pem" \
  -out "$OUTPUT_DIR/$DOMAIN.p12" \
  -name "$ALIAS" \
  -password pass:"$PASSWORD"

pass "SSL certificate generation complete!"
cat <<EOT
Files in $OUTPUT_DIR:
  - $DOMAIN.pem       (Certificate)
  - $DOMAIN-key.pem   (Private Key)
  - $DOMAIN.p12       (PKCS#12 Keystore - alias: $ALIAS)

Next steps:
• Use the files in the output directory for Spring Boot and Keycloak configurations.
• Example Spring Boot application.properties:
    server.port=8443
    server.ssl.key-store=classpath:ssl/$DOMAIN.p12  # or file:/path/to/ssl/$DOMAIN.p12
    server.ssl.key-store-password=$PASSWORD
    server.ssl.key-store-type=PKCS12
    server.ssl.key-alias=$ALIAS

• Example Keycloak Docker run (mount the same directory):
    docker run -p 8443:8443 \
      -v $(pwd)/ssl/$DOMAIN.pem:/etc/x509/https/tls.crt \
      -v $(pwd)/ssl/$DOMAIN-key.pem:/etc/x509/https/tls.key \
      -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \
      quay.io/keycloak/keycloak:latest start --https-port=8443
EOT

