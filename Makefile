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

# Verifies the entire project with detailed output showing each step
verify-all:
	@echo ""
	@echo "╔═════════════════════════════════════════════════════════════════════╗"
	@echo "║                  🔍 CVIX PROJECT VERIFICATION                       ║"
	@echo "╚═════════════════════════════════════════════════════════════════════╝"
	$(call run_verified_step,1/4,Running pnpm run check,$(PNPM) check,pnpm-check)
	$(call run_verified_step,2/4,Running pnpm run test,$(TIMEOUT_300) $(PNPM) test,pnpm-test)
	$(call run_verified_step,3/4,Running pnpm run build,$(TIMEOUT_600) $(PNPM) build,pnpm-build)
	$(call run_verified_step,4/4,Running backend tests,$(MAKE) backend-test,backend-test)
	@echo ""
	@echo "╔═════════════════════════════════════════════════════════════════════╗"
	@echo "║                                                                     ║"
	@echo "║              ✨ ALL COMMANDS PASSED SUCCESSFULLY! ✨               ║"
	@echo "║                                                                     ║"
	@echo "║  ✅ Linting & Formatting verified                                   ║"
	@echo "║  ✅ Frontend tests passed                                           ║"
	@echo "║  ✅ Frontend build successful                                       ║"
	@echo "║  ✅ Backend tests passed                                            ║"
	@echo "║                                                                     ║"
	@echo "╚═════════════════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "🚀 Project is ready for deployment!"
	@echo ""

.PHONY: all verify-all help install update-deps prepare-env prepare ruler-check ruler-apply dev dev-landing dev-web dev-docs build build-landing preview-landing build-web build-docs test test-ui test-coverage lint lint-strict check verify-secrets clean backend-build backend-run backend-test backend-clean cleanup-test-containers start test-all precommit
