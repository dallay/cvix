#!/usr/bin/env bash
# Script: generate-ssl-certs.sh
# Description: Interactive script to generate SSL certificates for development using mkcert,
#              producing PEM and PKCS#12 keystore files.
# Usage: Run without arguments and follow prompts.

set -euo pipefail

# Default values
default_domain="localhost"
default_output_dir="./ssl"
default_password="changeit"

# Prompt helper
prompt() {
  local var_name="$1"
  local prompt_text="$2"
  local default_value="$3"
  local is_password="$4"
  local input=""

  if [[ "$is_password" == "true" ]]; then
    read -s -p "$prompt_text [$default_value]: " input
    echo
  else
    read -p "$prompt_text [$default_value]: " input
  fi

  if [[ -z "$input" ]]; then
    printf -v "$var_name" "%s" "$default_value"
  else
    printf -v "$var_name" "%s" "$input"
  fi
}

# Interactive prompts
echo "⚙️  SSL Certificate Generation Interactive Setup"
prompt DOMAIN "Enter the domain for the cert" "$default_domain" false
prompt OUTPUT_DIR "Enter output directory for certificates" "$default_output_dir" false
prompt PASSWORD "Enter PKCS12 keystore password" "$default_password" true

# Prepare output directory
mkdir -p "$OUTPUT_DIR"

# Check mkcert
if ! command -v mkcert &> /dev/null; then
  echo "💥 Error: mkcert is not installed. Install mkcert first: https://github.com/FiloSottile/mkcert" >&2
  exit 1
fi

# Install local CA if needed
echo "🔐 Installing or verifying local CA..."
mkcert -install

# Generate PEM certificate and key
echo "🛠 Generating PEM certificate and key for '$DOMAIN'..."
mkcert -cert-file "$OUTPUT_DIR/$DOMAIN.pem" -key-file "$OUTPUT_DIR/$DOMAIN-key.pem" "$DOMAIN"

# Create PKCS#12 keystore with custom password
echo "🔑 Creating PKCS#12 keystore with provided password..."
openssl pkcs12 -export \
  -in "$OUTPUT_DIR/$DOMAIN.pem" \
  -inkey "$OUTPUT_DIR/$DOMAIN-key.pem" \
  -out "$OUTPUT_DIR/$DOMAIN.p12" \
  -name "server" \
  -password pass:"$PASSWORD"

# Summary
echo -e "\n✅ SSL certificate generation complete!\n"
echo "Files in $OUTPUT_DIR:"
echo "  - $DOMAIN.pem       (Certificate)"
echo "  - $DOMAIN-key.pem   (Private Key)"
echo "  - $DOMAIN.p12       (PKCS#12 Keystore)"

echo -e "\nNext steps:\n"
echo "• Use the files in the output directory for your Spring Boot and Keycloak configurations."
echo "• Example Spring Boot application.properties snippet (adjust classpath or filepath as needed):"
echo "    server.port=8443"
echo "    server.ssl.key-store=classpath:ssl/$DOMAIN.p12  # or file:/path/to/ssl/$DOMAIN.p12"  \
     "server.ssl.key-store-password=$PASSWORD  server.ssl.key-store-type=PKCS12"

echo -e "\n• Example Keycloak Docker run (mount the same directory):"
echo "    docker run -p 8443:8443 \\" \
     "-v \\$(pwd)/ssl/$DOMAIN.pem:/etc/x509/https/tls.crt \\" \
     "-v \\$(pwd)/ssl/$DOMAIN-key.pem:/etc/x509/https/tls.key \\" \
     "-e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \\" \
     "quay.io/keycloak/keycloak:latest start --https-port=8443"
