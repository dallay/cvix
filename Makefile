# ====================================================================================
# CVIX MAKEFILE
#
# Monorepo management simplified.
# For detailed explanation, run: make help
# ====================================================================================

.DEFAULT_GOAL := help

# ------------------------------------------------------------------------------------
# VARIABLES & CONFIGURATION
# ------------------------------------------------------------------------------------

# Operating System Detection & Shell Normalization
ifeq ($(OS),Windows_NT)
    DETECTED_OS := Windows
    # Find bash.exe and convert to short path (8.3) to avoid space issues
    SHELL_PATH := $(shell for /f "delims=" %i in ('where bash.exe 2^>NUL') do @(for %j in ("%i") do @echo %~sj & exit /b 0))
    ifeq ($(SHELL_PATH),)
        $(error ❌ A bash-compatible shell (Git Bash, WSL) is required on Windows. See CONTRIBUTING.md)
    endif
    SHELL := $(SHELL_PATH)
    TIMEOUT_CMD := timeout
else
    DETECTED_OS := $(shell uname -s 2>/dev/null || echo Unknown)
    SHELL := /bin/bash
    # Detect timeout command (gtimeout on macOS via brew, or timeout on Linux)
    TIMEOUT_CMD := $(shell command -v timeout || command -v gtimeout)
endif

# Common Constants
GRADLEW  := ./gradlew
PNPM     := pnpm
DEV_NULL := /dev/null
MKDIR_P  := mkdir -p

# Timeouts
TIMEOUT_300 := $(if $(TIMEOUT_CMD),$(TIMEOUT_CMD) 300,)
TIMEOUT_600 := $(if $(TIMEOUT_CMD),$(TIMEOUT_CMD) 600,)

# Tools Required
REQUIRED_TOOLS := pnpm npx docker git java

# Project Filters
CLIENT_LANDING_FILTER   := --filter @cvix/marketing
CLIENT_WEBAPP_FILTER    := --filter @cvix/webapp
CLIENT_BLOG_FILTER      := --filter @cvix/blog
CLIENT_DOCS_FILTER      := --filter @cvix/docs
CLIENT_SUBSCRIBE_FILTER := --filter @cvix/subscribe-forms

# Configuration
LOG_DIR := build/logs
TAG     ?= latest

# ------------------------------------------------------------------------------------
# CORE & HELP
# ------------------------------------------------------------------------------------

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z0-9_-]+:.*?## / {printf "  \033[36m%-25s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

check-tools: ## Verify required tools are installed
	@$(foreach tool,$(REQUIRED_TOOLS),\
		command -v $(tool) >/dev/null 2>&1 || { echo "❌ Error: '$(tool)' is not installed."; exit 1; };)

# ------------------------------------------------------------------------------------
# DEPENDENCY & ENVIRONMENT
# ------------------------------------------------------------------------------------

install: check-tools ## Install all dependencies
	@$(PNPM) install

update-deps: ## Update all dependencies to latest
	@$(PNPM) update-deps

prepare-env: ## Prepare local developer environment (.env, hooks)
	@$(SHELL) ./scripts/prepare-env.sh

prepare: install ## Prepare the development environment
	@$(PNPM) prepare

agents-check: ## Check agent configuration synchronization
	@$(SHELL) ./scripts/sync-agents.sh --dry-run

agents-sync: ## Apply agent configurations
	@$(SHELL) ./scripts/sync-agents.sh

# ------------------------------------------------------------------------------------
# DEVELOPMENT
# ------------------------------------------------------------------------------------

dev: ## Run all apps in development mode
	@$(PNPM) dev

dev-landing: ## Run landing page
	@$(PNPM) $(CLIENT_LANDING_FILTER) dev

dev-web: ## Run web application
	@$(PNPM) $(CLIENT_WEBAPP_FILTER) dev

dev-docs: ## Run documentation
	@$(PNPM) $(CLIENT_DOCS_FILTER) dev

dev-blog: ## Run blog
	@$(PNPM) $(CLIENT_BLOG_FILTER) dev

dev-subscribe: ## Run subscribe forms
	@$(PNPM) $(CLIENT_SUBSCRIBE_FILTER) dev

ssl-cert: ## Generate local SSL certificates
	@$(SHELL) ./scripts/generate-ssl-certificate.sh

# ------------------------------------------------------------------------------------
# BUILD
# ------------------------------------------------------------------------------------

build: check-tools ## Build all applications (Frontend + Backend)
	@echo "🏗️  Building Frontend..."
	@$(PNPM) build
	@echo "🏗️  Building Backend..."
	@$(MAKE) backend-build

build-landing: ## Build landing page
	@$(PNPM) $(CLIENT_LANDING_FILTER) build

build-web: ## Build web application
	@$(PNPM) $(CLIENT_WEBAPP_FILTER) build

build-docs: ## Build documentation
	@$(PNPM) $(CLIENT_DOCS_FILTER) build

build-blog: ## Build blog
	@$(PNPM) $(CLIENT_BLOG_FILTER) build

build-subscribe: ## Build subscribe forms
	@$(PNPM) $(CLIENT_SUBSCRIBE_FILTER) build

# ------------------------------------------------------------------------------------
# TESTING & QA
# ------------------------------------------------------------------------------------

test: ## Run all unit tests
	@$(PNPM) test

test-ui: ## Run UI tests
	@$(PNPM) test:ui

test-coverage: ## Run tests with coverage
	@$(PNPM) test:coverage

test-all: ## Run all tests (unit + integration + e2e)
	@$(PNPM) test:all

lint: ## Lint all code
	@$(PNPM) lint

lint-strict: ## Lint in strict mode
	@$(PNPM) lint:strict

check: ## Run type checks and validation
	@$(PNPM) check

verify-secrets: ## Verify Docker secrets sync
	@$(SHELL) ./scripts/verify-secrets-sync.sh

precommit: ## Run pre-commit checks manually
	@$(PNPM) precommit

# ------------------------------------------------------------------------------------
# BACKEND SPECIFIC
# ------------------------------------------------------------------------------------

backend-build: ## Build backend binary (Gradle)
	@$(GRADLEW) build

backend-run: ## Run backend locally
	@$(GRADLEW) bootRun

backend-test: ## Run backend tests
	@$(GRADLEW) test

backend-clean: ## Clean backend build artifacts
	@$(GRADLEW) clean

cleanup-test-containers: ## Cleanup Testcontainers leftovers
	@$(SHELL) ./scripts/cleanup-test-containers.sh

# ------------------------------------------------------------------------------------
# DOCKER
# ------------------------------------------------------------------------------------

docker-build-backend: ## Build Backend Docker image
	docker buildx build --load -t cvix-engine:$(TAG) -f server/engine/Dockerfile .

docker-build-webapp: ## Build WebApp Docker image
	docker buildx build --load -t cvix-webapp:$(TAG) -f client/apps/webapp/Dockerfile .

docker-build-marketing: ## Build Marketing Docker image
	docker buildx build --load -t cvix-marketing:$(TAG) -f client/apps/marketing/Dockerfile .

docker-build-all: docker-build-backend docker-build-marketing docker-build-webapp ## Build all Docker images

docker-clean: ## Remove local Docker images and stop containers
	@echo "🛑 Stopping containers for tag '$(TAG)'..."
	@ids=$$(docker ps -q --filter ancestor=cvix-engine:$(TAG)); [ -n "$$ids" ] && docker stop $$ids || true
	@ids=$$(docker ps -q --filter ancestor=cvix-webapp:$(TAG)); [ -n "$$ids" ] && docker stop $$ids || true
	@ids=$$(docker ps -q --filter ancestor=cvix-marketing:$(TAG)); [ -n "$$ids" ] && docker stop $$ids || true
	@echo "🧹 Removing images..."
	@docker rmi -f cvix-engine:$(TAG) 2>$(DEV_NULL) || true
	@docker rmi -f cvix-webapp:$(TAG) 2>$(DEV_NULL) || true
	@docker rmi -f cvix-marketing:$(TAG) 2>$(DEV_NULL) || true
	@echo "✅ Docker cleanup complete"

docker-verify-nonroot: ## Verify containers run as non-root
	@$(SHELL) ./scripts/verify-nonroot-docker.sh

# ------------------------------------------------------------------------------------
# FULL VERIFICATION (CI/CD)
# ------------------------------------------------------------------------------------

# Helper for parallel verification
define verify_step
	@echo "⏳ [$(1)/10] $(2)..." && \
	$(MKDIR_P) $(LOG_DIR) && \
	$(3) > $(LOG_DIR)/$(4).log 2>&1 && \
	echo "✅ $(2): PASSED" || \
	(echo "❌ $(2): FAILED. See $(LOG_DIR)/$(4).log"; exit 1)
endef

# Private targets for parallel execution
_verify-frontend-build:
	$(call verify_step,1,Building frontend,$(PNPM) build,pnpm-build)

_verify-backend-build:
	$(call verify_step,2,Building backend,$(GRADLEW) --no-daemon assemble < $(DEV_NULL),backend-build)

_verify-frontend-check:
	$(call verify_step,3,Frontend Checks (Biome),$(PNPM) check,frontend-check)

_verify-backend-check:
	$(call verify_step,4,Backend Checks (Detekt),$(GRADLEW) --no-daemon detektAll < $(DEV_NULL),backend-check)

_verify-markdown:
	$(call verify_step,5,Markdown Lint,npx --no-install markdownlint-cli2 '**/*.{md,mdx}' --config .markdownlint.json,markdown-lint)

_verify-yaml:
	@echo "⏳ [6/10] YAML Lint..." && $(MKDIR_P) $(LOG_DIR) && \
	if command -v yamllint >$(DEV_NULL) 2>&1; then \
		yamllint . > $(LOG_DIR)/yaml-lint.log 2>&1 && echo "✅ YAML Lint: PASSED" || (echo "❌ YAML Lint: FAILED"; exit 1); \
	else \
		echo "⚠️  YAML Lint: SKIPPED (missing tool)"; \
	fi

_verify-secrets:
	$(call verify_step,7,Secrets Sync Check,$(SHELL) ./scripts/check-secrets.sh,secrets-check)

_verify-frontend-tests:
	$(call verify_step,8,Frontend Unit Tests,$(TIMEOUT_300) $(PNPM) test,frontend-tests)

_verify-backend-tests:
	$(call verify_step,9,Backend Tests,$(TIMEOUT_600) $(GRADLEW) --no-daemon test < $(DEV_NULL),backend-tests)

_verify-e2e-tests:
	$(call verify_step,10,E2E Tests,FORCE_HTTP=true $(TIMEOUT_600) $(PNPM) test:e2e,e2e-tests)

verify-all: check-tools ## Run full project verification (Parallel)
	@echo ""
	@echo "╔═════════════════════════════════════════════════════════════════════╗"
	@echo "║                  🔍 CVIX FULL PROJECT VERIFICATION                  ║"
	@echo "╚═════════════════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "Phase 1: 🏗️  Building (Frontend & Backend)"
	@$(MAKE) -j2 _verify-frontend-build _verify-backend-build || exit 1
	@echo ""
	@echo "Phase 2: 🛡️  Static Analysis & Security"
	@$(MAKE) -j5 _verify-frontend-check _verify-backend-check _verify-markdown _verify-yaml _verify-secrets || exit 1
	@echo ""
	@echo "Phase 3: 🧪 Testing"
	@$(MAKE) -j3 _verify-frontend-tests _verify-backend-tests _verify-e2e-tests || exit 1
	@echo ""
	@echo "🚀 ALL SYSTEMS GO! Ready for deployment."
	@echo "📋 Logs: $(LOG_DIR)/"
	@echo ""

# ------------------------------------------------------------------------------------
# LIFECYCLE
# ------------------------------------------------------------------------------------

clean: ## Clean frontend artifacts
	@$(PNPM) clean

clean-all: clean backend-clean ## Clean EVERYTHING (Frontend + Backend)

all: install build test backend-test lint check ## Run full standard CI pipeline
	@echo "✨ All standard checks passed!"

.PHONY: help check-tools install update-deps prepare-env prepare dev dev-landing dev-web dev-docs \
        dev-blog dev-subscribe ssl-cert build build-landing build-web build-docs build-blog \
        build-subscribe test test-ui test-coverage test-all lint lint-strict check verify-secrets \
        precommit backend-build backend-run backend-test backend-clean cleanup-test-containers \
        docker-build-backend docker-build-webapp docker-build-marketing docker-build-all \
        docker-clean docker-verify-nonroot verify-all clean clean-all all
