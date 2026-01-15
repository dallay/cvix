# ProFileTailors Monorepo Makefile Help

Usage: make [command]

Available commands:
  all                      Run install, build, test, lint and check (common entry point).
  verify-all               🔍 Run comprehensive verification: all checks, lints, and tests in parallel.
  help                     Show this help message / list of make targets.
  install                  Install all dependencies.
  update-deps              Update all dependencies to their latest versions.
  prepare-env              Prepare the local developer environment (.env, symlinks, tooling checks).
  prepare                  Prepare the development environment.
  dev                      Run all applications in development mode.
  dev-landing              Run the landing page in development mode.
  dev-web                  Run the web application in development mode.
  dev-docs                 Run the documentation in development mode.
  ssl-cert                 Generate local development SSL certificates (requires mkcert & openssl).
  build                    Build all applications.
  build-landing            Build the landing page.
  preview-landing          Preview the landing page.
  build-web                Build the web application.
  build-docs               Build the documentation.
  test                     Run all tests.
  test-ui                  Run all UI tests.
  test-coverage            Run all tests with coverage.
  lint                     Lint all applications.
  lint-strict              Lint all applications in strict mode.
  check                    Run all checks.
  verify-secrets           Verify Docker secrets synchronization between entrypoint and compose files.
  clean                    Clean all applications.
  backend-build            Build the backend.
  backend-run              Run the backend.
  backend-test             Run the backend tests.
  backend-clean            Clean the backend.
  cleanup-test-containers  Clean up test containers left running from Testcontainers.
  start                    Start all applications.
  test-all                 Run all tests for all applications.
  precommit                Run the pre-commit checks.
  
  docker-build-backend     Build the backend Docker image (TAG=yourtag, default: latest)
  docker-build-marketing   Build the marketing Docker image (TAG=yourtag, default: latest)
  docker-build-webapp      Build the webapp Docker image (TAG=yourtag, default: latest)
  docker-build-all         Build all Docker images (TAG=yourtag, default: latest)
  docker-verify-nonroot    Verify Docker containers are running as non-root users

For a detailed explanation of each command, refer to the project's README.md.
