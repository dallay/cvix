# Docker Deployment Guide

> How to build and deploy the ProFileTailors services using Docker and publish images to container registries.

This guide provides a concise overview for building and pushing images from this monorepo.

## Prerequisites

- Docker Engine or Docker Desktop installed and running
- Access to the target container registry (GHCR or Docker Hub)
- Logged in to the registry with `docker login`

## Build Images

You can build images using the repository Makefile and the project-specific Dockerfiles (when present) or using docker buildx directly. See `infra/README.md` for environment specifics and compose-based flows.

Example generic build using BuildKit:

```bash
# From repository root
docker build -t ghcr.io/<org-or-user>/cvix-engine:local -f server/engine/Dockerfile .
```

## Push Images

```bash
docker push ghcr.io/<org-or-user>/cvix-engine:local
```

For automated, versioned releases (recommended), this repository uses semantic-release in CI to tag and publish images. Refer to the main README “Release & Versioning” section for tags and policies.

## Compose-based Local Deployment

For local multi-service development and smoke testing, use Docker Compose manifests in `compose.yaml` and `infra/`:

```bash
# Start essential services
docker compose up -d postgresql keycloak greenmail

# Bring down services
docker compose down
```

## Notes

- For environment variables, ensure `.env` is present at the repo root. The `make prepare-env` target will create and link it to subprojects as needed.
- For production deployment, harden configurations and secrets management; avoid committing credentials.
- Consult `infra/README.md` for environment variables, ports, and service-specific guidance.

