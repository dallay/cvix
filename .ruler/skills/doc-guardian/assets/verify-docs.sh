#!/usr/bin/env bash

# Doc Guardian - Documentation Verification Script
# Checks for common documentation issues and gaps

set -euo pipefail

DOCS_DIR="client/apps/docs/src/content/docs"
EXIT_CODE=0

echo "🔍 Doc Guardian - Running documentation checks..."
echo ""

# Check 1: Dead internal links (simplified)
echo "1️⃣  Checking for markdown links..."
LINK_COUNT=$(rg '\[.*\]\(.+\)' "$DOCS_DIR" -c 2>/dev/null | awk -F: '{sum += $2} END {print sum+0}')
if [[ -n "$LINK_COUNT" ]] && [[ $LINK_COUNT -gt 0 ]]; then
  echo "   ✅ Found $LINK_COUNT links (manual verification recommended)"
else
  echo "   ✅ No links found or all external"
fi
echo ""

# Check 2: TODO markers
echo "2️⃣  Checking for TODO markers..."
TODOS=$(rg 'TODO|FIXME|XXX' "$DOCS_DIR" -n 2>/dev/null || true)
if [[ -z "$TODOS" ]]; then
  echo "   ✅ No TODO markers found"
else
  echo "   ⚠️  Found TODO markers:"
  echo "$TODOS" | head -10
  echo "   (Not blocking, but should be resolved)"
fi
echo ""

# Check 3: Stale dates
echo "3️⃣  Checking for outdated 'last_updated' dates..."
CURRENT_YEAR=$(date +%Y)
STALE_THRESHOLD=$((CURRENT_YEAR - 2))

# Extract dates and filter stale ones
STALE_DATES=""
while IFS=: read -r file line content; do
  # Extract year from the date in format YYYY-MM-DD or YYYY
  year=$(echo "$content" | grep -oE '20[0-9]{2}' | head -1)
  if [[ -n "$year" ]] && [[ "$year" -le "$STALE_THRESHOLD" ]]; then
    STALE_DATES="${STALE_DATES}${file}:${line}:${content}\n"
  fi
done < <(rg "last_updated.*20[0-9]{2}" "$DOCS_DIR" -n 2>/dev/null || true)

if [[ -z "$STALE_DATES" ]]; then
  echo "   ✅ No stale dates found"
else
  echo "   ⚠️  Found stale dates (2+ years old):"
  echo -e "$STALE_DATES" | head -10
  echo "   (Not blocking, but consider reviewing)"
fi
echo ""

# Check 4: Code examples syntax
echo "4️⃣  Checking for code blocks..."
TS_BLOCKS=$(rg '```typescript' "$DOCS_DIR" -c 2>/dev/null | awk -F: '{sum += $2} END {print sum+0}')
KOTLIN_BLOCKS=$(rg '```kotlin' "$DOCS_DIR" -c 2>/dev/null | awk -F: '{sum += $2} END {print sum+0}')

echo "   Found $TS_BLOCKS TypeScript blocks"
echo "   Found $KOTLIN_BLOCKS Kotlin blocks"
echo "   ✅ Code examples present (syntax validation requires manual testing)"
echo ""

# Check 5: Empty files
echo "5️⃣  Checking for empty documentation files..."
EMPTY_FILES=$(fd -e md -e mdx . "$DOCS_DIR" -x sh -c 'if [[ $(wc -l < "{}") -lt 5 ]]; then echo "{}"; fi' 2>/dev/null || true)
if [[ -z "$EMPTY_FILES" ]]; then
  echo "   ✅ No suspiciously small files found"
else
  echo "   ❌ Found files with less than 5 lines:"
  echo "$EMPTY_FILES"
  EXIT_CODE=1
fi
echo ""

# Check 6: Missing frontmatter
echo "6️⃣  Checking for files without frontmatter..."
MISSING_FRONTMATTER=$(fd -e md -e mdx . "$DOCS_DIR" -x sh -c '
  first_line=$(head -n 1 "{}")
  if [[ "$first_line" != "---" ]]; then
    echo "{}"
  fi
' 2>/dev/null || true)

if [[ -z "$MISSING_FRONTMATTER" ]]; then
  echo "   ✅ All files have frontmatter"
else
  echo "   ⚠️  Files without frontmatter (must start with --- on line 1):"
  echo "$MISSING_FRONTMATTER" | head -5
  echo "   (Frontmatter must be on line 1, no leading blank lines)"
fi
echo ""

# Check 7: Documentation structure
echo "7️⃣  Checking documentation structure..."
REQUIRED_DIRS=("overview" "developer-guide" "backend" "frontend")
MISSING_DIRS_ARRAY=()

for dir in "${REQUIRED_DIRS[@]}"; do
  if [[ ! -d "$DOCS_DIR/$dir" ]]; then
    MISSING_DIRS_ARRAY+=("  - $dir/")
  fi
done

if [[ ${#MISSING_DIRS_ARRAY[@]} -eq 0 ]]; then
  echo "   ✅ All required directories exist"
else
  echo "   ⚠️  Missing recommended directories:"
  printf '%s\n' "${MISSING_DIRS_ARRAY[@]}"
fi
echo ""

# Summary
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [[ $EXIT_CODE -eq 0 ]]; then
  echo "✅ All critical documentation checks passed!"
  echo ""
  echo "Next steps:"
  echo "  1. Review TODO markers if any"
  echo "  2. Verify code examples actually work"
  echo "  3. Click through internal links manually"
  echo "  4. Update stale dates in frontmatter"
else
  echo "❌ Some documentation checks failed"
  echo "   Fix the issues above and run again"
fi
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

exit $EXIT_CODE
