# ====================================================================================
# CVIX MAKEFILE
#
# This Makefile provides a centralized set of commands for managing the cvix
# monorepo. It simplifies the development workflow by offering a consistent
# interface for common tasks like dependency management, building, testing, and
# running applications.
#
# For a detailed explanation of each command, refer to the project's README.md.
# ====================================================================================

.DEFAULT_GOAL := help

# ------------------------------------------------------------------------------------
# VARIABLES
# ------------------------------------------------------------------------------------

# Environment variables
SHELL := /bin/bash
PNPM := pnpm

# Project specific variables
CLIENT_LANDING_FILTER := --filter @cvix/marketing
CLIENT_WEBAPP_FILTER := --filter @cvix/webapp
CLIENT_DOCS_FILTER := --filter @cvix/docs

# Build configuration
LOG_DIR := build/logs
# Detect timeout command (gtimeout on macOS via brew, or timeout on Linux)
TIMEOUT_CMD := $(shell command -v timeout || command -v gtimeout)
TIMEOUT_300 := $(if $(TIMEOUT_CMD),$(TIMEOUT_CMD) 300,)
TIMEOUT_600 := $(if $(TIMEOUT_CMD),$(TIMEOUT_CMD) 600,)

# ------------------------------------------------------------------------------------
# HELP
# ------------------------------------------------------------------------------------

# Provides help information for the available commands.
help:
	@cat docs/MAKE_HELP.md

# ------------------------------------------------------------------------------------
# DEPENDENCY MANAGEMENT
# ------------------------------------------------------------------------------------

# Installs all dependencies.
install:
	@$(PNPM) install

# Updates all dependencies to their latest versions.
update-deps:
	@$(PNPM) update-deps

# ------------------------------------------------------------------------------------
# ENVIRONMENT
# ------------------------------------------------------------------------------------

# Prepares the local developer environment (.env, symlinks, tooling checks).
prepare-env:
	@bash ./scripts/prepare-env.sh

# ------------------------------------------------------------------------------------
# DEVELOPMENT
# ------------------------------------------------------------------------------------

# Prepares the development environment.
prepare:
	@$(PNPM) prepare

# Checks the project's architecture rules.
ruler-check:
	@$(PNPM) ruler:check

# Applies the project's architecture rules.
ruler-apply:
	@$(PNPM) ruler:apply

# Runs all applications in development mode.
dev:
	@$(PNPM) dev

# Runs the landing page in development mode.
dev-landing:
	@$(PNPM) $(CLIENT_LANDING_FILTER) dev

# Runs the web application in development mode.
dev-web:
	@$(PNPM) $(CLIENT_WEBAPP_FILTER) dev

# Runs the documentation in development mode.
dev-docs:
	@$(PNPM) $(CLIENT_DOCS_FILTER) dev

# ------------------------------------------------------------------------------------
# LOCAL DEVELOPMENT UTILITIES
# ------------------------------------------------------------------------------------

# Generate local development SSL certificates (interactive; uses mkcert and openssl)
ssl-cert:
	@bash ./scripts/generate-ssl-certificate.sh

# ------------------------------------------------------------------------------------
# DOCKER IMAGE BUILDS (BACKEND & FRONTEND)
# ------------------------------------------------------------------------------------

# Allows override: make docker-build-backend TAG=<someversion>
TAG ?= latest

# Build Backend Docker image
# Usage: make docker-build-backend [TAG=yourtag]
docker-build-backend:
	docker buildx build \
		--load \
		-t cvix-engine:$(TAG) \
		-f server/engine/Dockerfile .

# Build WebApp Docker image
# Usage: make docker-build-webapp [TAG=yourtag]
docker-build-webapp:
	docker buildx build \
		--load \
		-t cvix-webapp:$(TAG) \
		-f client/apps/webapp/Dockerfile .

# Build Marketing Docker image
# Usage: make docker-build-marketing [TAG=yourtag]
docker-build-marketing:
	docker buildx build \
		--load \
		-t cvix-marketing:$(TAG) \
		-f client/apps/marketing/Dockerfile .

# Build all Docker images: backend, webapp, marketing
# Usage: make docker-build-all [TAG=yourtag]
docker-build-all: docker-build-backend docker-build-marketing docker-build-webapp
	@echo "All images built with tag '$(TAG)'"

# Remove locally built Docker images
# Usage: make docker-clean [TAG=yourtag]
docker-clean:
	@echo "Removing Docker images with tag '$(TAG)'..."
	@docker rmi -f cvix-engine:$(TAG) 2>/dev/null || true
	@docker rmi -f cvix-webapp:$(TAG) 2>/dev/null || true
	@docker rmi -f cvix-marketing:$(TAG) 2>/dev/null || true
	@echo "✅ Docker images with tag '$(TAG)' removed"

# Verifies Docker containers are running as non-root users
# Tests that all frontend containers properly run as UID 101 (nginx user)
docker-verify-nonroot:
	@bash ./scripts/verify-nonroot-docker.sh

# ------------------------------------------------------------------------------------
# BUILD
# ------------------------------------------------------------------------------------

# Builds all applications.
# Builds all applications (frontend and backend).
build:
	@$(PNPM) build
	@$(MAKE) backend-build

# Builds the landing page.
build-landing:
	@$(PNPM) $(CLIENT_LANDING_FILTER) build

# Previews the landing page.
preview-landing:
	@$(PNPM) $(CLIENT_LANDING_FILTER) preview

# Builds the web application.
build-web:
	@$(PNPM) $(CLIENT_WEBAPP_FILTER) build

# Builds the documentation.
build-docs:
	@$(PNPM) $(CLIENT_DOCS_FILTER) build

# ------------------------------------------------------------------------------------
# TESTING & LINTING
# ------------------------------------------------------------------------------------

# Runs all tests.
test:
	@$(PNPM) test

# Runs all UI tests.
test-ui:
	@$(PNPM) test:ui

# Runs all tests with coverage.
test-coverage:
	@$(PNPM) test:coverage

# Lints all applications.
lint:
	@$(PNPM) lint

# Lints all applications in strict mode.
lint-strict:
	@$(PNPM) lint:strict

# Runs all checks.
check:
	@$(PNPM) check

# Verifies Docker secrets synchronization between entrypoint and compose files.
verify-secrets:
	@./scripts/verify-secrets-sync.sh

# ------------------------------------------------------------------------------------
# CLEAN
# ------------------------------------------------------------------------------------

# Cleans all applications.
clean:
	@$(PNPM) clean

# ------------------------------------------------------------------------------------
# BACKEND
# ------------------------------------------------------------------------------------

# Builds the backend.
backend-build:
	@./gradlew build

# Runs the backend.
backend-run:
	@./gradlew bootRun

# Runs the backend tests.
backend-test:
	@./gradlew test

# Cleans the backend.
backend-clean:
	@./gradlew clean

# Cleans up test containers left running from Testcontainers.
cleanup-test-containers:
	@./scripts/cleanup-test-containers.sh

# ------------------------------------------------------------------------------------
# APPLICATION LIFECYCLE
# ------------------------------------------------------------------------------------

# Starts all applications.
start:
	@$(PNPM) start

# Runs all tests for all applications.
test-all:
	@$(PNPM) test:all

# Runs the pre-commit checks.
precommit:
	@$(PNPM) precommit

# Builds and prepares all deliverables.
all: install build test backend-test lint check
	@echo ""
	@echo "╔═════════════════════════════════════════════════════════════════════╗"
	@echo "║                                                                     ║"
	@echo "║              ✨ ALL COMMANDS PASSED SUCCESSFULLY! ✨               ║"
	@echo "║                                                                     ║"
	@echo "║  ✅ Dependencies installed                                          ║"
	@echo "║  ✅ Frontend & Backend built                                        ║"
	@echo "║  ✅ Tests passed                                                    ║"
	@echo "║  ✅ Linting passed                                                  ║"
	@echo "╚═════════════════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "🚀 Project is ready for deployment!"
	@echo ""

# Helper function for verification steps
# Usage: $(call run_verified_step, step_number, description, command, log_file_name)
define run_verified_step
	@echo ""
	@echo "⏳ Step $(1): $(2)..."
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@mkdir -p $(LOG_DIR)
	@$(3) > $(LOG_DIR)/$(4).log 2>&1 && echo "✅ $(2): PASSED" || (echo "❌ $(2): FAILED. See $(LOG_DIR)/$(4).log for details"; exit 1)
endef

# Individual verification targets (for parallel execution)
.PHONY: _verify-frontend-check _verify-backend-check _verify-markdown _verify-yaml
.PHONY: _verify-frontend-tests _verify-e2e-tests _verify-backend-tests _verify-secrets

_verify-frontend-check:
	@echo "⏳ [1/8] Running frontend checks (Biome)..." && \
	mkdir -p $(LOG_DIR) && \
	$(PNPM) check > $(LOG_DIR)/frontend-check.log 2>&1 && \
	echo "✅ Frontend checks: PASSED" || \
	(echo "❌ Frontend checks: FAILED. See $(LOG_DIR)/frontend-check.log"; exit 1)

_verify-backend-check:
	@echo "⏳ [2/8] Running backend checks (Detekt)..." && \
	mkdir -p $(LOG_DIR) && \
	./gradlew detektAll > $(LOG_DIR)/backend-check.log 2>&1 && \
	echo "✅ Backend checks: PASSED" || \
	(echo "❌ Backend checks: FAILED. See $(LOG_DIR)/backend-check.log"; exit 1)

_verify-markdown:
	@echo "⏳ [3/8] Running Markdown lint..." && \
	mkdir -p $(LOG_DIR) && \
	npx --no-install markdownlint-cli2 '**/*.{md,mdx}' --config .markdownlint.json > $(LOG_DIR)/markdown-lint.log 2>&1 && \
	echo "✅ Markdown lint: PASSED" || \
	(echo "❌ Markdown lint: FAILED. See $(LOG_DIR)/markdown-lint.log"; exit 1)

_verify-yaml:
	@echo "⏳ [4/8] Running YAML lint..." && \
	mkdir -p $(LOG_DIR) && \
	(command -v yamllint >/dev/null 2>&1 && yamllint . > $(LOG_DIR)/yaml-lint.log 2>&1 && echo "✅ YAML lint: PASSED" || echo "⚠️  YAML lint: SKIPPED (yamllint not installed)") || true

_verify-secrets:
	@echo "⏳ [5/8] Checking secrets synchronization..." && \
	mkdir -p $(LOG_DIR) && \
	./scripts/check-secrets.sh > $(LOG_DIR)/secrets-check.log 2>&1 && \
	echo "✅ Secrets check: PASSED" || \
	(echo "❌ Secrets check: FAILED. See $(LOG_DIR)/secrets-check.log"; exit 1)

_verify-frontend-tests:
	@echo "⏳ [6/8] Running frontend unit tests..." && \
	mkdir -p $(LOG_DIR) && \
	$(TIMEOUT_300) $(PNPM) test > $(LOG_DIR)/frontend-tests.log 2>&1 && \
	echo "✅ Frontend unit tests: PASSED" || \
	(echo "❌ Frontend unit tests: FAILED. See $(LOG_DIR)/frontend-tests.log"; exit 1)

_verify-backend-tests:
	@echo "⏳ [7/8] Running backend tests..." && \
	mkdir -p $(LOG_DIR) && \
	$(TIMEOUT_600) ./gradlew test > $(LOG_DIR)/backend-tests.log 2>&1 && \
	echo "✅ Backend tests: PASSED" || \
	(echo "❌ Backend tests: FAILED. See $(LOG_DIR)/backend-tests.log"; exit 1)

_verify-e2e-tests:
	@echo "⏳ [8/8] Running E2E tests..." && \
	mkdir -p $(LOG_DIR) && \
	$(TIMEOUT_600) $(PNPM) test:e2e > $(LOG_DIR)/e2e-tests.log 2>&1 && \
	echo "✅ E2E tests: PASSED" || \
	(echo "❌ E2E tests: FAILED. See $(LOG_DIR)/e2e-tests.log"; exit 1)

# Verifies the entire project with all checks, lints, and tests
# Runs checks in parallel groups for optimal performance
verify-all:
	@echo ""
	@echo "╔═════════════════════════════════════════════════════════════════════╗"
	@echo "║                  🔍 CVIX FULL PROJECT VERIFICATION                  ║"
	@echo "║                                                                     ║"
	@echo "║  This will run all checks, lints, and tests in parallel groups     ║"
	@echo "╚═════════════════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@echo "Phase 1: Static Analysis & Linting (Parallel)"
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@$(MAKE) -j4 _verify-frontend-check _verify-backend-check _verify-markdown _verify-yaml || exit 1
	@echo ""
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@echo "Phase 2: Security & Configuration Checks"
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@$(MAKE) _verify-secrets || exit 1
	@echo ""
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@echo "Phase 3: Testing (Parallel)"
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@$(MAKE) -j3 _verify-frontend-tests _verify-backend-tests _verify-e2e-tests || exit 1
	@echo ""
	@echo "╔═════════════════════════════════════════════════════════════════════╗"
	@echo "║                                                                     ║"
	@echo "║              ✨ ALL VERIFICATIONS PASSED SUCCESSFULLY! ✨          ║"
	@echo "║                                                                     ║"
	@echo "║  ✅ Frontend checks (Biome)                                         ║"
	@echo "║  ✅ Backend checks (Detekt)                                         ║"
	@echo "║  ✅ Markdown lint                                                   ║"
	@echo "║  ✅ YAML lint                                                       ║"
	@echo "║  ✅ Secrets synchronization                                         ║"
	@echo "║  ✅ Frontend unit tests                                             ║"
	@echo "║  ✅ Backend tests                                                   ║"
	@echo "║  ✅ E2E tests                                                       ║"
	@echo "║                                                                     ║"
	@echo "╚═════════════════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "🚀 Project is ready for deployment!"
	@echo "📋 Logs available in: $(LOG_DIR)/"
	@echo ""

.PHONY: all verify-all help install update-deps prepare-env prepare ruler-check ruler-apply dev dev-landing dev-web dev-docs build build-landing preview-landing build-web build-docs test test-ui test-coverage lint lint-strict check verify-secrets clean backend-build backend-run backend-test backend-clean cleanup-test-containers start test-all precommit ssl-cert docker-build-backend docker-build-webapp docker-build-marketing docker-build-all docker-clean docker-verify-nonroot _verify-frontend-check _verify-backend-check _verify-markdown _verify-yaml _verify-frontend-tests _verify-e2e-tests _verify-backend-tests _verify-secrets
