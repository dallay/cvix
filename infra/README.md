# Infrastructure Services

This directory contains the infrastructure configuration for the cvix application, including
database, authentication, email testing, and SSL certificate management.

## Quick Start

### 1. Set Up Environment Variables

Before running any Docker Compose commands, you need to create a `.env` file in this directory (`infra/`):

```bash
cd infra/
cp .env.example .env
```

Then edit `.env` and adjust the values for your environment (database credentials, URLs, API keys, etc.).

**Important**: The `.env` file is automatically loaded by Docker Compose and is gitignored to prevent accidental commits of sensitive data.

### 2. Run Docker Compose

From the `infra/` directory, start all services:

```bash
docker-compose -f app.yml up -d
```

This command will:
- Load all environment variables from `.env`
- Start PostgreSQL, Keycloak, and other services
- Include all referenced compose files (`common.yml`, `postgresql/`, `keycloak/`, etc.)

### 3. Verify Services

Check that services are running:

```bash
docker-compose -f app.yml ps
```

## Environment Variables

All services are configured via environment variables defined in `.env`. Key variables include:

- **Database**: `POSTGRESQL_USER`, `POSTGRESQL_PASSWORD`, `DATABASE_URL`
- **Keycloak**: `KEYCLOAK_ADMIN`, `KEYCLOAK_ADMIN_PASSWORD`, `KC_HOSTNAME`
- **Backend**: `BACKEND_URL`, `OAUTH2_SERVER_URL`, `CORS_ALLOWED_ORIGINS`
- **Email**: `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`
- **PDF Generation**: `PDF_DOCKER_IMAGE`, `PDF_MAX_CONCURRENT_CONTAINERS`, etc.

For a complete list of variables and their descriptions, see `.env.example`.

## Services

### PostgreSQL Database

- **Location**: `postgresql/`
- **Port**: 5432
- **Configuration**: Database initialization scripts and Docker Compose setup

### Keycloak Authentication

- **Location**: `keycloak/`
- **Port**: 9080 (HTTP), 9443 (HTTPS)
- **Configuration**: Realm configuration and themes

### GreenMail Email Testing

- **Location**: `greenmail/`
- **Ports**:
  - 3025 (SMTP)
  - 3110 (POP3)
  - 3143 (IMAP)
  - 3465 (SMTPS)
  - 3993 (IMAPS)
  - 3995 (POP3S)
  - 8080 (Web Interface)
- **Configuration**: Test email server for development and testing

### SSL Certificates

- **Location**: `ssl/`
- **Configuration**: Local SSL certificates for HTTPS development

## SSL Certificate and Keystore Generation Script

This repository contains a script to generate SSL certificates and keystores using `mkcert`, and configure them for a Spring Boot application. The script will generate PEM files (`key.pem` and `cert.pem`), a PKCS12 keystore (`keystore.p12`), and optionally a Java KeyStore (JKS) (`keystore.jks`).

## Prerequisites

Ensure the following tools are installed on your system:

- [mkcert](https://github.com/FiloSottile/mkcert)
- [openssl](https://www.openssl.org/)
- `keytool` (comes with JDK)

## Script Usage

The script will:

1. Create an SSL directory.
2. Generate a private key and certificate using `mkcert`.
3. Convert the generated PEM files into a PKCS12 keystore.
4. Optionally convert the PKCS12 keystore to a JKS keystore.
5. Prompt for a keystore password, use an environment variable if set, or use a default password.

### Running the Script

1. **Clone the repository** (if applicable):

    ```sh
    git clone <repository-url>
    cd <repository-directory>
    ```

2. **Make the script executable**:

    ```sh
    chmod +x generate-ssl-certificate.sh
    ```

3. **Run the script**:

    ```sh
    ./generate-ssl-certificate.sh
    ```

   You can specify a custom base directory as the first argument:

    ```sh
    ./generate-ssl-certificate.sh /path/to/directory
    ```

   The script will prompt you to enter a keystore password. If you don't provide one, it will use the password from the environment variable `SSL_KEYSTORE_PASSWORD` if set, or default to `changeme`.

### Environment Variable

You can set the keystore password via an environment variable `SSL_KEYSTORE_PASSWORD`:

```sh
export SSL_KEYSTORE_PASSWORD=mysecurepassword
```

Or in Windows:

```shell
set SSL_KEYSTORE_PASSWORD=mysecurepassword
```

### Generated Files

The script will generate the following files in the ssl directory:

- `key.pem`: Private key
- `cert.pem`: Certificate
- `keystore.p12`: PKCS12 keystore
- `keystore.jks`: Java KeyStore (optional)
