#!/usr/bin/env bash
# Script to apply Swagger documentation standard to all controllers
# Based on ContactController and WaitlistController as reference implementations

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/../../.."
CONTROLLERS_DIR="$PROJECT_ROOT/server/engine/src/main/kotlin"

echo "🔍 Finding controllers without proper Swagger documentation..."

# Find all controllers
CONTROLLERS=$(find "$CONTROLLERS_DIR" -name "*Controller.kt" | sort)

echo "📋 Controllers to process:"
echo "$CONTROLLERS" | nl

echo ""
echo "⚠️  WARNING: This script will modify controller files!"
echo "   Make sure you have uncommitted changes backed up."
echo ""
read -p "Continue? (y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Aborted."
    exit 1
fi

# Process each controller
for controller in $CONTROLLERS; do
    basename=$(basename "$controller")
    
    # Skip already compliant controllers
    if rg -q "@Tag.*description.*endpoints" "$controller" && \
       rg -q "Content.*schema.*Schema.*implementation" "$controller" && \
       rg -q "ProblemDetail" "$controller"; then
        echo "✅ SKIP: $basename (already compliant)"
        continue
    fi
    
    echo "🔧 Processing: $basename"
    
    # Add ProblemDetail import if missing
    if ! rg -q "import org.springframework.http.ProblemDetail" "$controller"; then
        sd 'import org.springframework.web.bind.annotation.RestController' \
           'import org.springframework.http.ProblemDetail\nimport org.springframework.web.bind.annotation.RestController' \
           "$controller"
    fi
    
    # Add Content and Schema imports if missing
    if ! rg -q "import io.swagger.v3.oas.annotations.media.Content" "$controller"; then
        sd 'import io.swagger.v3.oas.annotations.Operation' \
           'import io.swagger.v3.oas.annotations.Operation\nimport io.swagger.v3.oas.annotations.media.Content\nimport io.swagger.v3.oas.annotations.media.Schema' \
           "$controller"
    fi
    
    # Add @Tag import if missing
    if ! rg -q "import io.swagger.v3.oas.annotations.tags.Tag" "$controller"; then
        sd 'import io.swagger.v3.oas.annotations.responses.ApiResponses' \
           'import io.swagger.v3.oas.annotations.responses.ApiResponses\nimport io.swagger.v3.oas.annotations.tags.Tag' \
           "$controller"
    fi
    
    echo "   - Imports added/verified"
    
    # Note: Actual @Tag and Content+Schema additions require more complex logic
    # that would be better done with a proper Kotlin AST parser or manual review
    echo "   ⚠️  Manual review needed for: @Tag annotation and Content schemas"
done

echo ""
echo "✅ Import updates complete!"
echo ""
echo "📝 Next steps (MANUAL):"
echo "   1. Add @Tag annotation to each controller class"
echo "   2. Add Content(schema = Schema(implementation = ...)) to all @ApiResponse"
echo "   3. Use ProblemDetail::class for error responses"
echo "   4. Run: ./gradlew detektAll"
echo "   5. Run: make verify-all"
echo ""
echo "See: .ruler/skills/spring-boot/SWAGGER_STANDARD.md for the complete standard"
