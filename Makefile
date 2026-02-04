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

# Operating System Detection
ifeq ($(OS),Windows_NT)
    DETECTED_OS := Windows
else
    DETECTED_OS := $(shell uname -s 2>/dev/null || echo Unknown)
endif

# Platform-specific configurations
ifeq ($(DETECTED_OS),Windows)
    # Try to find bash.exe in the PATH
    SHELL_PATH := $(firstword $(shell where bash.exe 2>NUL))
    ifeq ($(SHELL_PATH),)
        SHELL := cmd.exe
        GRADLEW := gradlew.bat
        DEV_NULL := NUL
        MKDIR_P := mkdir
    else
        SHELL := $(SHELL_PATH)
        GRADLEW := ./gradlew
        DEV_NULL := /dev/null
        MKDIR_P := mkdir -p
    endif
else
    SHELL := /bin/bash
    GRADLEW := ./gradlew
    DEV_NULL := /dev/null
    MKDIR_P := mkdir -p
endif

PNPM := pnpm

# Project specific variables
CLIENT_LANDING_FILTER := --filter @cvix/marketing
CLIENT_WEBAPP_FILTER := --filter @cvix/webapp
CLIENT_BLOG_FILTER := --filter @cvix/blog
CLIENT_DOCS_FILTER := --filter @cvix/docs
CLIENT_SUBSCRIBE_FILTER := --filter @cvix/subscribe-forms

# Build configuration
LOG_DIR := build/logs
# Detect timeout command (gtimeout on macOS via brew, or timeout on Linux)
TIMEOUT_CMD := $(shell command -v timeout || command -v gtimeout)
TIMEOUT_300 := $(if $(TIMEOUT_CMD),$(TIMEOUT_CMD) 300,)
TIMEOUT_600 := $(if $(TIMEOUT_CMD),$(TIMEOUT_CMD) 600,)
# Markdown lint command with glob pattern
MARKDOWNLINT_CMD := npx --no-install markdownlint-cli2 '**/*.{md,mdx}' --config .markdownlint.json

# ------------------------------------------------------------------------------------
# HELP
# ------------------------------------------------------------------------------------

# Provides help information for the available commands.
help:
	@cat MAKE_HELP.md

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
	@$(SHELL) ./scripts/prepare-env.sh

# ------------------------------------------------------------------------------------
# DEVELOPMENT
# ------------------------------------------------------------------------------------

# Prepares the development environment.
prepare:
	@$(PNPM) prepare

# Checks the project's agent configuration synchronization.
agents-check:
	@$(SHELL) ./scripts/sync-agents.sh --dry-run

# Applies the project's agent configurations (creates symlinks).
agents-sync:
	@$(SHELL) ./scripts/sync-agents.sh

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

# Runs the blog in development mode.
dev-blog:
	@$(PNPM) $(CLIENT_BLOG_FILTER) dev

# Runs the subscribe-forms app in development mode.
dev-subscribe:
	@$(PNPM) $(CLIENT_SUBSCRIBE_FILTER) dev

# ------------------------------------------------------------------------------------
# LOCAL DEVELOPMENT UTILITIES
# ------------------------------------------------------------------------------------

# Generate local development SSL certificates (interactive; uses mkcert and openssl)
ssl-cert:
	@$(SHELL) ./scripts/generate-ssl-certificate.sh

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
	@docker rmi -f cvix-engine:$(TAG) 2>$(DEV_NULL) || true
	@docker rmi -f cvix-webapp:$(TAG) 2>$(DEV_NULL) || true
	@docker rmi -f cvix-marketing:$(TAG) 2>$(DEV_NULL) || true
	@echo "✅ Docker images with tag '$(TAG)' removed"

# Verifies Docker containers are running as non-root users
# Tests that all frontend containers properly run as non-root user
docker-verify-nonroot:
	@$(SHELL) ./scripts/verify-nonroot-docker.sh

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

# Builds the blog.
build-blog:
	@$(PNPM) $(CLIENT_BLOG_FILTER) build

# Previews the blog.
preview-blog:
	@$(PNPM) $(CLIENT_BLOG_FILTER) preview

# Builds the subscribe-forms app.
build-subscribe:
	@$(PNPM) $(CLIENT_SUBSCRIBE_FILTER) build

# Previews the subscribe-forms app.
preview-subscribe:
	@$(PNPM) $(CLIENT_SUBSCRIBE_FILTER) preview

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
	@$(SHELL) ./scripts/verify-secrets-sync.sh

# ------------------------------------------------------------------------------------
# CLEAN
# ------------------------------------------------------------------------------------

# Cleans all applications.
clean:
	@$(PNPM) clean

# Cleans both frontend and backend.
clean-all: clean backend-clean

# ------------------------------------------------------------------------------------
# BACKEND
# ------------------------------------------------------------------------------------

# Builds the backend.
backend-build:
	@$(GRADLEW) build

# Runs the backend.
backend-run:
	@$(GRADLEW) bootRun

# Runs the backend tests.
backend-test:
	@$(GRADLEW) test

# Cleans the backend.
backend-clean:
	@$(GRADLEW) clean

# Cleans up test containers left running from Testcontainers.
cleanup-test-containers:
	@$(SHELL) ./scripts/cleanup-test-containers.sh

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

# Helper function for verification steps (sequential)
# Usage: $(call run_verified_step, step_number, description, command, log_file_name)
define run_verified_step
	@echo ""
	@echo "⏳ Step $(1): $(2)..."
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@$(MKDIR_P) $(LOG_DIR)
	@$(3) > $(LOG_DIR)/$(4).log 2>&1 && echo "✅ $(2): PASSED" || (echo "❌ $(2): FAILED. See $(LOG_DIR)/$(4).log for details"; exit 1)
endef

# Helper function for parallel verification steps
# Usage: $(call verify_step, step_num, description, command, log_name)
define verify_step
	@echo "⏳ [$(1)/10] $(2)..." && \
	$(MKDIR_P) $(LOG_DIR) && \
	$(3) > $(LOG_DIR)/$(4).log 2>&1 && \
	echo "✅ $(2): PASSED" || \
	(echo "❌ $(2): FAILED. See $(LOG_DIR)/$(4).log"; exit 1)
endef

# Individual verification targets (for parallel execution)

# Verifies the entire project with all checks, lints, and tests
# Runs checks in parallel groups for optimal performance
verify-all:
	@echo ""
	@echo "╔═════════════════════════════════════════════════════════════════════╗"
	@echo "║                  🔍 CVIX FULL PROJECT VERIFICATION                  ║"
	@echo "║                                                                     ║"
	@echo "║  This will run all checks, lints, and tests in parallel groups      ║"
	@echo "╚═════════════════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@echo "Phase 1: Building (Frontend & Backend)"
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@$(MAKE) -j2 _verify-frontend-build _verify-backend-build || exit 1
	@echo ""
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@echo "Phase 2: Static Analysis, Security & Linting (Parallel)"
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@$(MAKE) -j5 _verify-frontend-check _verify-backend-check _verify-markdown _verify-yaml _verify-secrets || exit 1
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
	@echo "║  ✅ Frontend build                                                  ║"
	@echo "║  ✅ Backend build                                                   ║"
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

.PHONY: all verify-all help install update-deps prepare-env prepare agents-check agents-sync dev dev-landing dev-web dev-docs dev-blog build build-landing preview-landing build-web build-docs build-blog preview-blog test test-ui test-coverage lint lint-strict check verify-secrets clean clean-all backend-build backend-run backend-test backend-clean cleanup-test-containers start test-all precommit ssl-cert docker-build-backend docker-build-webapp docker-build-marketing docker-build-all docker-clean docker-verify-nonroot _verify-frontend-build _verify-backend-build _verify-frontend-check _verify-backend-check _verify-markdown _verify-yaml _verify-frontend-tests _verify-e2e-tests _verify-backend-tests _verify-secrets dev-subscribe build-subscribe preview-subscribe
