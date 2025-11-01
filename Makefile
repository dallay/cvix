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
	@echo "Loomify Monorepo Makefile"
	@echo "-------------------------"
	@echo "Usage: make [command]"
	@echo ""
	@echo "Available commands:"
	@echo "  install                  Install all dependencies."
	@echo "  update-deps              Update all dependencies to their latest versions."
	@echo "  prepare                  Prepare the development environment."
	@echo "  ruler-check              Check the project's architecture rules."
	@echo "  ruler-apply              Apply the project's architecture rules."
	@echo "  dev                      Run all applications in development mode."
	@echo "  dev-landing              Run the landing page in development mode."
	@echo "  dev-web                  Run the web application in development mode."
	@echo "  dev-docs                 Run the documentation in development mode."
	@echo "  build                    Build all applications."
	@echo "  build-landing            Build the landing page."
	@echo "  preview-landing          Preview the landing page."
	@echo "  build-web                Build the web application."
	@echo "  build-docs               Build the documentation."
	@echo "  test                     Run all tests."
	@echo "  test-ui                  Run all UI tests."
	@echo "  test-coverage            Run all tests with coverage."
	@echo "  lint                     Lint all applications."
	@echo "  lint-strict              Lint all applications in strict mode."
	@echo "  check                    Run all checks."
	@echo "  clean                    Clean all applications."
	@echo "  backend-build            Build the backend."
	@echo "  backend-run              Run the backend."
	@echo "  backend-test             Run the backend tests."
	@echo "  backend-clean            Clean the backend."
	@echo "  start                    Start all applications."
	@echo "  test-all                 Run all tests for all applications."
	@echo "  precommit                Run the pre-commit checks."

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
	@$(PNPM) dev:landing

# Runs the web application in development mode.
dev-web:
	@$(PNPM) dev:web

# Runs the documentation in development mode.
dev-docs:
	@$(PNPM) dev:docs

# ------------------------------------------------------------------------------------
# BUILD
# ------------------------------------------------------------------------------------

# Builds all applications.
build:
	@$(PNPM) build

# Builds the landing page.
build-landing:
	@$(PNPM) build:landing

# Previews the landing page.
preview-landing:
	@$(PNPM) preview:landing

# Builds the web application.
build-web:
	@$(PNPM) build:web

# Builds the documentation.
build-docs:
	@$(PNPM) build:docs

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

.PHONY: help install update-deps prepare ruler-check ruler-apply dev dev-landing dev-web dev-docs build build-landing preview-landing build-web build-docs test test-ui test-coverage lint lint-strict check clean backend-build backend-run backend-test backend-clean start test-all precommit
