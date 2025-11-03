# ====================================================================================
# LOOMIFY MAKEFILE
#
# This Makefile provides a centralized set of commands for managing the loomify
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
CLIENT_LANDING_FILTER := --filter @loomify/marketing
CLIENT_WEBAPP_FILTER := --filter @loomify/webapp
CLIENT_DOCS_FILTER := --filter @loomify/docs

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
	@echo "All targets built successfully"

.PHONY: all help install update-deps prepare ruler-check ruler-apply dev dev-landing dev-web dev-docs build build-landing preview-landing build-web build-docs test test-ui test-coverage lint lint-strict check clean backend-build backend-run backend-test backend-clean cleanup-test-containers start test-all precommit
