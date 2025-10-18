# Keycloak Setup

> A guide for setting up and configuring Keycloak for local development authentication and authorization.

## Overview

We use **Keycloak** as our identity and access management (IAM) solution, providing OAuth2/OIDC capabilities for the application.

## Local Development Setup

Keycloak runs as a Docker container via Docker Compose.

- **Configuration**: `infra/keycloak/keycloak-compose.yml`
- **Environment Variables**: `infra/keycloak/.env`

### How to Start

From the project root, run:

```bash
docker compose up -d keycloak
```

This starts Keycloak and automatically imports the pre-configured realm from `infra/keycloak/realm-config/loomify-realm.json`.

### Accessing the Admin Console

- **URL**: `http://localhost:9080/admin/`
- **Admin User**: `admin`
- **Admin Password**: `secret`

(Credentials can be changed in `infra/keycloak/.env`)

## Realm Configuration

The `loomify` realm is pre-configured with:

- Client configurations for the backend and frontend.
- User roles (`admin`, `user`).
- Authentication flows and password policies.

## Application Integration

The Spring Boot backend is configured as an OAuth2 resource server that validates JWTs issued by Keycloak. The issuer URI is configured in `application.yml`:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:9080/realms/loomify
```

## Customizing Keycloak

If you make changes to the realm via the admin console, you must export the configuration to persist it:

1. Execute the export command:

    ```bash
    docker exec -it keycloak /opt/keycloak/bin/kc.sh export --realm loomify --file /tmp/loomify-realm.json
    ```

2. Copy the file from the container to your local machine:

    ```bash
    docker cp keycloak:/tmp/loomify-realm.json infra/keycloak/realm-config/loomify-realm.json
    ```

3. Commit the updated `loomify-realm.json` file.
