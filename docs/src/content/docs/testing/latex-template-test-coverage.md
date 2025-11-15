---
title: "LaTeX Template Test Coverage"
description: "Testing the template generation"
---

## Overview

This document describes the comprehensive test suite for LaTeX resume template rendering, which is critical for the PDF generation feature of the product.

## Test Suite Summary

**Location**: `server/engine/src/test/kotlin/com/loomify/resume/infrastructure/template/LatexTemplateRendererTest.kt`

**Total Tests**: 12 tests covering all critical scenarios

**Status**: ✅ All tests passing

## Test Categories

### 1. Complete Feature Test
- **Test**: `should render complete resume with all fields populated`
- **Fixture**: `john-doe.json` (comprehensive resume with all fields)
- **Validates**: Full rendering pipeline with all optional fields populated
- **Compares against**: Expected LaTeX fixture file

### 2. Minimal Data Test
- **Test**: `should render minimal resume with only required fields`
- **Fixture**: `minimal-resume.json`
- **Validates**: Template handles sparse data gracefully
- **Checks**: Required fields (name, email, phone) and at least one section (skills)

### 3. LaTeX Special Characters Escaping
- **Test**: `should properly escape LaTeX special characters`
- **Fixture**: `special-chars-resume.json`
- **Critical Characters Tested**:
  - `$` (Dollar signs) → `\$` - Prevents math mode activation
  - `%` (Percent signs) → `\%` - Prevents line comments
  - `&` (Ampersands) → `\&` - Prevents table column errors
  - `#` (Hash symbols) → `\#` - Prevents parameter errors
  - `_` (Underscores) → `\_` - Prevents subscript errors
  - `{`, `}` → `\{`, `\}` - Prevents grouping errors
  - `~`, `^` → `\textasciitilde{}`, `\textasciicircum{}` - Prevents special spacing/superscript

**Why This Matters**: Without proper escaping, user content like "$5B revenue" or "40% growth" would cause LaTeX compilation to fail with "Command \item invalid in math mode" errors.

### 4. Unicode and International Characters
- **Test**: `should handle Unicode and international characters`
- **Fixture**: `unicode-resume.json`
- **Validates**:
  - French accents: àáâäæçéèêëïîôœùûüÿ
  - Spanish characters: ñ, ü, í, ó, ú
  - Non-ASCII names and text
- **Ensures**: UTF-8 encoding is preserved through the rendering pipeline

### 5. Long Content Handling
- **Test**: `should handle long content without breaking LaTeX structure`
- **Fixture**: `long-content-resume.json`
- **Validates**:
  - Extensive text blocks in summaries
  - Large lists (10+ items)
  - Long descriptions that might cause pagination issues
- **Checks**: All items are rendered and LaTeX structure remains valid

### 6. Null/Empty Fields
- **Test**: `should handle null and empty optional fields gracefully`
- **Fixture**: `null-fields-resume.json`
- **Validates**: Missing optional data doesn't cause crashes or invalid LaTeX
- **Checks**: Template conditionals work correctly

### 7. LaTeX Structure Validation
- **Test**: `should generate valid LaTeX document structure`
- **Validates**:
  - Document class declaration
  - Begin/end document tags
  - Package imports
  - Section headers
  - Balanced environments (all `\begin{X}` have matching `\end{X}`)
  - No unescaped math mode triggers in user content

### 8. Security: LaTeX Injection Prevention
- **Test**: `should reject dangerous LaTeX commands` (parameterized)
- **Dangerous Commands Blocked**:
  - `\input` - File inclusion
  - `\include` - File inclusion
  - `\write` - File writing
  - `\def` - Command definition
  - `\newcommand` - Command creation
- **Implementation**: `LatexTemplateRenderer.validateContent()` scans all user input

## Test Fixtures

### JSON Fixtures (`src/test/resources/data/json/`)
1. `john-doe.json` - Complete resume with all fields
2. `minimal-resume.json` - Only required fields
3. `special-chars-resume.json` - LaTeX special characters
4. `unicode-resume.json` - International characters
5. `long-content-resume.json` - Extensive text and lists
6. `null-fields-resume.json` - Null optional fields

### Expected LaTeX Fixtures (`src/test/resources/data/latex/`)
1. `john-doe.tex` - Expected output for complete resume

## Key Implementation Details

### LaTeX Escaper
**Location**: `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/template/util/LatexEscaper.kt`

Escapes all dangerous characters in user content:
```kotlin
fun escape(input: String): String = input
    .replace("\\", "\\textbackslash{}")
    .replace("&", "\\&")
    .replace("%", "\\%")
    .replace("$", "\\$")
    .replace("#", "\\#")
    .replace("_", "\\_")
    .replace("{", "\\{")
    .replace("}", "\\}")
    .replace("~", "\\textasciitilde{}")
    .replace("^", "\\textasciicircum{}")
```

### Template Bullet Point Fix
Changed from nested math mode to simple text bullet:
```latex
% Old (caused math mode issues):
\renewcommand\labelitemi{$\vcenter{\hbox{\small$\bullet$}}$}

% New (safe):
\renewcommand\labelitemi{\textbullet}
```

### URL Renderer
**Location**: `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/template/renders/UrlRenderer.kt`

Handles URL escaping separately with format options:
- `short` - Removes protocol
- `latex` - Escapes LaTeX special chars
- `short-latex` - Both

## Running the Tests

```bash
# Run all LaTeX template tests
./gradlew :server:engine:test --tests '*LatexTemplateRendererTest'

# Run specific test
./gradlew :server:engine:test --tests '*LatexTemplateRendererTest.should properly escape LaTeX special characters*'

# Enable document persistence for debugging
# Set persistGeneratedDocument = true in the test file
# Generated files will be in: build/test-output/*.tex
```

## Why This Test Suite is Critical

1. **PDF Generation is Core Feature**: Resume PDF export is a primary product feature
2. **User Content is Unpredictable**: Users can input any text, including LaTeX special characters
3. **Compilation Failures are User-Facing**: LaTeX errors result in failed PDF generation
4. **Security**: Prevents LaTeX injection attacks that could read/write files
5. **Data Integrity**: Ensures all user data is preserved and correctly rendered
6. **International Support**: Validates UTF-8 and Unicode handling

## Coverage Metrics

- **Edge Cases Covered**: ✅
  - Empty optional fields
  - Maximum length content
  - Special characters in all positions
  - Unicode/UTF-8
  - Null values

- **Security Scenarios**: ✅
  - LaTeX injection attempts
  - Command execution prevention
  - File access prevention

- **Data Integrity**: ✅
  - Character escaping
  - Encoding preservation
  - Structure validation

## Future Enhancements

1. **Performance Tests**: Measure rendering time for large resumes
2. **Actual PDF Compilation**: Run pdflatex on generated .tex files in CI
3. **Visual Regression**: Compare rendered PDFs pixel-by-pixel
4. **Additional Templates**: When new templates are added, replicate this test structure
5. **Parameterized Edge Cases**: Add more edge case combinations

## Maintenance

- **Adding New Fields**: Update `special-chars-resume.json` with special chars in new fields
- **Template Changes**: Update `john-doe.tex` fixture to match
- **New Escaping Rules**: Add tests to `should properly escape LaTeX special characters`
- **Security Updates**: Add new dangerous commands to parameterized test

---

Last Updated: November 14, 2024
Test Suite Version: 1.0
Status: All tests passing ✅

