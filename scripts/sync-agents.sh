#!/usr/bin/env bash
# =============================================================================
# sync-agents.sh - Single Source of Truth for AI Agent Configurations
# =============================================================================
#
# This script synchronizes AI agent configurations from the central .agents/
# directory to the various locations required by different AI coding tools.
#
# Source of truth: .agents/
# - AGENTS.md          → Main instructions for all agents
# - skills/            → Shared skills/knowledge for agents
# - command/           → Agent commands (GitHub Copilot agents)
# - prompts/           → Reusable prompts
# - .mcp.json          → MCP server configurations
# - agents.config.json → This script's configuration
#
# Usage:
#   ./scripts/sync-agents.sh [--clean] [--dry-run] [--verbose]
#
# Options:
#   --clean     Remove existing symlinks before creating new ones
#   --dry-run   Show what would be done without making changes
#   --verbose   Show detailed output
#
# =============================================================================

set -euo pipefail

# ------------------------------------------------------------------------------
# Configuration
# ------------------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
AGENTS_DIR="$ROOT_DIR/.agents"
CONFIG_FILE="$AGENTS_DIR/agents.config.json"

# Colors
RED="\033[31m"
GREEN="\033[32m"
YELLOW="\033[33m"
CYAN="\033[36m"
BOLD="\033[1m"
RESET="\033[0m"

# Symbols
PASS="${GREEN}✔${RESET}"
FAIL="${RED}✘${RESET}"
WARN="${YELLOW}!${RESET}"
INFO="${CYAN}ℹ${RESET}"

# Options
CLEAN=false
DRY_RUN=false
VERBOSE=false

# ------------------------------------------------------------------------------
# Helpers
# ------------------------------------------------------------------------------
log() { echo -e "$1"; }
log_verbose() { [[ "$VERBOSE" == true ]] && echo -e "$1" || true; }
section() { echo -e "\n${BOLD}${CYAN}➤ $1${RESET}"; }

die() {
    echo -e "${FAIL} ${RED}Error: $1${RESET}" >&2
    exit 1
}

# Check if jq is available (needed for JSON parsing)
check_dependencies() {
    if ! command -v jq &>/dev/null; then
        die "jq is required but not installed. Install with: brew install jq"
    fi
}

# Create a symlink, handling existing files/links
create_symlink() {
    local source="$1"
    local dest="$2"
    local dest_dir
    dest_dir="$(dirname "$dest")"
    
    # Ensure source exists
    if [[ ! -e "$source" ]]; then
        log "${WARN} Source does not exist: $source"
        return 1
    fi
    
    # Create destination directory if needed
    if [[ ! -d "$dest_dir" ]]; then
        if [[ "$DRY_RUN" == true ]]; then
            log_verbose "  [dry-run] Would create directory: $dest_dir"
        else
            mkdir -p "$dest_dir"
            log_verbose "  Created directory: $dest_dir"
        fi
    fi
    
    # Handle existing destination
    if [[ -L "$dest" ]]; then
        # It's a symlink - check if it points to the right place
        local current_target
        current_target="$(readlink "$dest")"
        if [[ "$current_target" == "$source" ]]; then
            log_verbose "  ${PASS} Already linked: $dest"
            return 0
        else
            # Wrong target, remove and recreate
            if [[ "$DRY_RUN" == true ]]; then
                log_verbose "  [dry-run] Would update symlink: $dest -> $source"
            else
                rm "$dest"
                log_verbose "  Removed old symlink: $dest (was -> $current_target)"
            fi
        fi
    elif [[ -e "$dest" ]]; then
        # It's a regular file/directory - back it up and remove
        if [[ "$DRY_RUN" == true ]]; then
            log_verbose "  [dry-run] Would backup and replace: $dest"
        else
            local backup
            backup="$dest.bak.$(date +%Y%m%d%H%M%S)"
            mv "$dest" "$backup"
            log "${WARN} Backed up existing: $dest -> $backup"
        fi
    fi
    
    # Create the symlink
    if [[ "$DRY_RUN" == true ]]; then
        log "  [dry-run] Would link: $dest -> $source"
    else
        ln -s "$source" "$dest"
        log "${PASS} Linked: $dest -> $source"
    fi
}

# Create symlinks for all items in a directory
create_symlinks_for_contents() {
    local source_dir="$1"
    local dest_dir="$2"
    local pattern="${3:-*}"
    
    if [[ ! -d "$source_dir" ]]; then
        log "${WARN} Source directory does not exist: $source_dir"
        return 1
    fi
    
    # Create destination directory
    if [[ ! -d "$dest_dir" ]]; then
        if [[ "$DRY_RUN" == true ]]; then
            log_verbose "  [dry-run] Would create directory: $dest_dir"
        else
            mkdir -p "$dest_dir"
        fi
    fi
    
    # Find and link matching items
    local count=0
    for item in "$source_dir"/$pattern; do
        [[ -e "$item" ]] || continue
        local name
        name="$(basename "$item")"
        create_symlink "$item" "$dest_dir/$name"
        ((count++)) || true
    done
    
    log_verbose "  Processed $count items from $source_dir"
}

# Clean up symlinks for an agent
cleanup_agent_symlinks() {
    local agent="$1"
    local targets
    targets=$(jq -r ".agents[\"$agent\"].targets | keys[]" "$CONFIG_FILE" 2>/dev/null) || return 0
    
    for target in $targets; do
        local dest
        dest=$(jq -r ".agents[\"$agent\"].targets[\"$target\"].destination" "$CONFIG_FILE")
        dest="$ROOT_DIR/$dest"
        
        if [[ -L "$dest" ]]; then
            if [[ "$DRY_RUN" == true ]]; then
                log "  [dry-run] Would remove symlink: $dest"
            else
                rm "$dest"
                log "${PASS} Removed: $dest"
            fi
        elif [[ -d "$dest" ]]; then
            # For directories, remove symlinks inside but keep the directory
            find "$dest" -maxdepth 1 -type l -exec rm {} \; 2>/dev/null || true
            # Remove directory if empty
            rmdir "$dest" 2>/dev/null || true
        fi
    done
}

# Process a single agent configuration
process_agent() {
    local agent="$1"
    local enabled
    local description
    
    enabled=$(jq -r ".agents[\"$agent\"].enabled" "$CONFIG_FILE")
    description=$(jq -r ".agents[\"$agent\"].description // \"\"" "$CONFIG_FILE")
    
    if [[ "$enabled" != "true" ]]; then
        log_verbose "  Skipping disabled agent: $agent"
        return 0
    fi
    
    log "\n${BOLD}$agent${RESET} - $description"
    
    local targets
    targets=$(jq -r ".agents[\"$agent\"].targets | keys[]" "$CONFIG_FILE")
    
    for target in $targets; do
        local source dest type pattern
        source=$(jq -r ".agents[\"$agent\"].targets[\"$target\"].source" "$CONFIG_FILE")
        dest=$(jq -r ".agents[\"$agent\"].targets[\"$target\"].destination" "$CONFIG_FILE")
        type=$(jq -r ".agents[\"$agent\"].targets[\"$target\"].type" "$CONFIG_FILE")
        pattern=$(jq -r ".agents[\"$agent\"].targets[\"$target\"].pattern // \"*\"" "$CONFIG_FILE")
        
        local full_source="$AGENTS_DIR/$source"
        local full_dest="$ROOT_DIR/$dest"
        
        log_verbose "  Processing target: $target ($type)"
        
        case "$type" in
            symlink)
                create_symlink "$full_source" "$full_dest"
                ;;
            symlink-contents)
                create_symlinks_for_contents "$full_source" "$full_dest" "$pattern"
                ;;
            *)
                log "${WARN} Unknown type: $type for $agent.$target"
                ;;
        esac
    done
}

# Update .gitignore with managed entries
update_gitignore() {
    local managed marker
    managed=$(jq -r ".gitignore.managed" "$CONFIG_FILE")
    marker=$(jq -r ".gitignore.marker" "$CONFIG_FILE")
    
    if [[ "$managed" != "true" ]]; then
        log_verbose "Gitignore management disabled"
        return 0
    fi
    
    section "Updating .gitignore"
    
    local gitignore="$ROOT_DIR/.gitignore"
    local start_marker="# START $marker"
    local end_marker="# END $marker"
    
    # Get entries from config
    local entries
    entries=$(jq -r '.gitignore.entries[]' "$CONFIG_FILE")
    
    if [[ "$DRY_RUN" == true ]]; then
        log "[dry-run] Would update .gitignore with managed section"
        return 0
    fi
    
    # Remove existing managed section if present
    if grep -q "$start_marker" "$gitignore" 2>/dev/null; then
        # Create temp file without the managed section
        awk "/$start_marker/,/$end_marker/{next} 1" "$gitignore" > "$gitignore.tmp"
        mv "$gitignore.tmp" "$gitignore"
    fi
    
    # Remove any trailing blank lines at the end of the file
    # This ensures we don't accumulate blank lines over time
    sed -i.bak -e :a -e '/^\n*$/{$d;N;ba' -e '}' "$gitignore"
    rm -f "$gitignore.bak"
    
    # Append new managed section (with blank line separator)
    {
        echo ""
        echo "$start_marker"
        echo "$entries" | sort | uniq
        echo "$end_marker"
    } >> "$gitignore"
    
    log "${PASS} Updated .gitignore with managed entries"
}

# ------------------------------------------------------------------------------
# Main
# ------------------------------------------------------------------------------
main() {
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case "$1" in
            --clean)
                CLEAN=true
                shift
                ;;
            --dry-run)
                DRY_RUN=true
                shift
                ;;
            --verbose|-v)
                VERBOSE=true
                shift
                ;;
            --help|-h)
                echo "Usage: $0 [--clean] [--dry-run] [--verbose]"
                echo ""
                echo "Options:"
                echo "  --clean     Remove existing symlinks before creating new ones"
                echo "  --dry-run   Show what would be done without making changes"
                echo "  --verbose   Show detailed output"
                exit 0
                ;;
            *)
                die "Unknown option: $1"
                ;;
        esac
    done
    
    echo -e "${BOLD}${CYAN}"
    echo "╔═══════════════════════════════════════════════════════════════════╗"
    echo "║              AI Agent Configuration Sync                         ║"
    echo "╚═══════════════════════════════════════════════════════════════════╝"
    echo -e "${RESET}"
    
    [[ "$DRY_RUN" == true ]] && log "${INFO} Running in dry-run mode\n"
    
    # Verify we're in the right place
    if [[ ! -d "$AGENTS_DIR" ]]; then
        die "Agents directory not found: $AGENTS_DIR"
    fi
    
    if [[ ! -f "$CONFIG_FILE" ]]; then
        die "Config file not found: $CONFIG_FILE"
    fi
    
    check_dependencies
    
    # Get list of agents
    local agents
    agents=$(jq -r '.agents | keys[]' "$CONFIG_FILE")
    
    # Clean if requested
    if [[ "$CLEAN" == true ]]; then
        section "Cleaning existing symlinks"
        for agent in $agents; do
            cleanup_agent_symlinks "$agent"
        done
    fi
    
    # Process each agent
    section "Syncing agent configurations"
    for agent in $agents; do
        process_agent "$agent"
    done
    
    # Update gitignore
    update_gitignore
    
    echo -e "\n${BOLD}${GREEN}✨ Agent sync complete!${RESET}"
}

main "$@"
