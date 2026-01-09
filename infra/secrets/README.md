# Docker Secrets

This directory contains sensitive values used by the backend application in Docker Compose deployments.

## Setup

Each secret should be stored in a plain text file with no trailing newline or extra whitespace:

```bash
# Example: Create a secret file
echo -n "my-secret-value" > client_secret.txt
```

## Required Secrets

The following secret files must be present for the backend service to function correctly:

- `database_password.txt` - PostgreSQL database password (required for R2DBC and Liquibase)
- `client_secret.txt` - OAuth2 client secret for authentication
- `admin_realm_password.txt` - Keycloak admin realm password
- `sendgrid_api_key.txt` - SendGrid API key for email notifications
- `smtp_username.txt` - SMTP username for email service
- `smtp_password.txt` - SMTP password for email service
- `ssl_keystore_password.txt` - SSL keystore password (if using TLS)
- `waitlist_ip_hmac_secret.txt` - HMAC secret for waitlist IP validation

## Verification

To verify that secrets are synchronized between the Docker entrypoint script and the compose configuration, run:

```bash
make verify-secrets
```

This will compare the list of secrets in `server/engine/docker-entrypoint.sh` with those defined in `infra/app.yml`.

## Security Notes

- **DO NOT commit secret files to version control**. This directory is gitignored.
- Use strong, randomly generated values for all secrets.
- Rotate secrets regularly, especially after personnel changes.
- In production, use a proper secret management solution (AWS Secrets Manager, HashiCorp Vault, etc.) instead of file-based secrets.
- Ensure file permissions are restrictive (`chmod 600 *.txt`).

## CI/CD Integration

For CI/CD pipelines:

1. Store secrets in your CI/CD platform's secret management (GitHub Secrets, GitLab CI/CD Variables, etc.)
2. Create secret files at deployment time from environment variables
3. Mount secrets into containers using Docker Compose secrets or Kubernetes secrets
4. Run `make verify-secrets` in CI to catch synchronization issues early

## Local Development

For local development, you can use placeholder values:

```bash
# Quick setup for local dev
echo -n "dev-database-password" > database_password.txt
echo -n "dev-client-secret" > client_secret.txt
echo -n "dev-admin-password" > admin_realm_password.txt
echo -n "SENDGRID_API_KEY_PLACEHOLDER" > sendgrid_api_key.txt
echo -n "developer" > smtp_username.txt
echo -n "secret" > smtp_password.txt
echo -n "changeit" > ssl_keystore_password.txt
echo -n "dev-waitlist-hmac-secret" > waitlist_ip_hmac_secret.txt

# Set restrictive permissions
chmod 600 *.txt
```
