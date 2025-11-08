# Resume Validator

This module implements validation for Resume objects based on the [JSON Resume Schema](https://jsonresume.org/schema/).

## Overview

The `JsonResumeValidator` class validates resume data against the official JSON Resume Schema specification (draft-07). It ensures that all sections, fields, and formats comply with the standard.

## Features

- ✅ **Complete Schema Validation**: Validates all sections of the JSON Resume Schema
- ✅ **Format Validation**: Validates emails, URLs, ISO 8601 dates, and country codes
- ✅ **Type Safety**: Fully typed with TypeScript
- ✅ **Comprehensive Testing**: 100% test coverage with 15+ test cases

## Usage

```typescript
import { JsonResumeValidator } from "@/core/resume/infrastructure/validation";
import type { Resume } from "@/core/resume/domain/Resume";

const validator = new JsonResumeValidator();

const resume: Resume = {
  basics: {
    name: "John Doe",
    email: "john@example.com",
    // ... other fields
  },
  // ... other sections
};

const isValid = validator.validate(resume);
console.log(isValid); // true or false
```

## Validation Rules

### Email Format

- Must follow standard email format: `user@domain.com`
- Pattern: `/^[^\s@]+@[^\s@]+\.[^\s@]+$/`

### URL Format

- Must be a valid HTTP or HTTPS URL
- Pattern: `/^https?:\/\/.+/`

### ISO 8601 Dates

Supports three formats as per JSON Resume Schema:

- `YYYY` - Year only (e.g., `2023`)
- `YYYY-MM` - Year and month (e.g., `2023-06`)
- `YYYY-MM-DD` - Full date (e.g., `2023-06-15`)

**Validation includes:**

- Month range: `01-12`
- Day range: `01-31`

### Country Codes

- Must be ISO 3166-1 alpha-2 format
- Two uppercase letters (e.g., `US`, `GB`, `DE`)

## Validated Sections

The validator checks all JSON Resume sections:

1. **basics** (required) - Personal and contact information
   - Validates email, URL, image URL, location, and profiles
2. **work** - Work experience
3. **volunteer** - Volunteer experience
4. **education** - Educational background
5. **awards** - Awards and recognitions
6. **certificates** - Professional certificates
7. **publications** - Published works
8. **skills** - Skills and expertise
9. **languages** - Language proficiency
10. **interests** - Personal interests
11. **references** - Professional references
12. **projects** - Projects

## Empty String Handling

The validator treats empty strings as valid (optional fields) but will validate non-empty strings according to their expected format. This allows for partial resume data while ensuring that provided data is valid.

## Error Handling

The validator returns `false` for:

- `null` or `undefined` resume objects
- Missing or invalid `basics` section
- Invalid email, URL, or date formats
- Invalid country codes
- Non-array values for array fields
- Invalid string arrays (arrays containing non-string values)

## Schema Reference

This implementation follows the official JSON Resume Schema:

- Schema URL: `https://raw.githubusercontent.com/jsonresume/resume-schema/master/schema.json`
- Specification: JSON Schema Draft 07

## Testing

Run the test suite:

```bash
pnpm --filter @loomify/webapp test:unit JsonResumeValidator
```

The test suite includes:

- Basic structure validation
- Email format validation
- URL format validation
- ISO 8601 date validation
- Country code validation
- Array validation
- String array validation
- Complete resume validation

## Implementation Notes

- The validator uses regex patterns for format validation
- All validation methods are private except the main `validate()` method
- The implementation prioritizes correctness over performance
- Empty strings are treated as optional/unset values

## Future Enhancements

Potential improvements:

- Add detailed error messages indicating which field failed validation
- Support for custom validation rules
- Integration with Zod or other schema validation libraries
- Support for JSON Resume Schema extensions
