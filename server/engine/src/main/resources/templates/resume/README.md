# Resume Templates

This directory contains all available resume template definitions. Each template consists of two files:

## Structure

```text
templates/resume/
├── engineering/
│   ├── template.stg          # StringTemplate file (LaTeX/PDF output)
│   ├── engineering.stg       # Main template file (can be named after template)
│   ├── metadata.yaml         # Template metadata and configuration
│   └── preview.png           # (Optional) Preview image
├── marketing/
│   ├── template.stg
│   ├── metadata.yaml
│   └── preview.png
└── minimal/
    ├── template.stg
    ├── metadata.yaml
    └── preview.png
```

## Template Metadata Format

Each template must have a `metadata.yaml` file with the following structure:

```yaml
# Unique identifier for the template
id: engineering

# Display name shown in the UI
name: Engineering Resume

# Semantic version of the template
version: 1.0.0

# Localized descriptions for the template
descriptions:
  en: English description
  es: Spanish description

# List of supported locales
supportedLocales:
  - en
  - es

# Path to the template file (StringTemplate)
templatePath: classpath:templates/resume/engineering/engineering.stg

# (Optional) URL to a preview image
previewUrl: /assets/previews/engineering.png

# (Optional) Template parameters and their default values
params:
  colorPalette:
    default: blue
    options: [blue, green, red, gray]
  fontFamily:
    default: Roboto
    options: [Roboto, OpenSans, SourceSansPro]
  spacing:
    default: normal
    options: [compact, normal, comfortable]
  density:
    default: comfortable
    options: [compact, comfortable, spacious]
  includePhoto:
    default: true
    type: boolean
  highlightSkills:
    default: true
    type: boolean
```

## How Templates are Loaded

The `YamlTemplateMetadataLoader` automatically discovers and loads all templates by:

1. Scanning the classpath for `classpath:templates/resume/**/metadata.yaml` files
2. Parsing each YAML file into a `TemplateMetadata` object
3. Validating required fields (id, name, version, templatePath, supportedLocales)
4. Converting locale codes (en, es) into `Locale` enum values
5. Parsing localized descriptions into a `Map<Locale, String>`
6. Extracting template parameters with default values

## Adding a New Template

To add a new template:

1. Create a new directory under `templates/resume/`:
   ```bash
   mkdir -p templates/resume/{template-name}
   ```

2. Create the template file (e.g., `template.stg`):
   ```bash
   touch templates/resume/{template-name}/template.stg
   ```

3. Create the metadata file:
   ```bash
   touch templates/resume/{template-name}/metadata.yaml
   ```

4. Fill in the `metadata.yaml` with required fields:
   - `id`: Unique identifier (lowercase, no spaces)
   - `name`: Display name
   - `version`: Semantic version (e.g., 1.0.0)
   - `descriptions`: Multi-language descriptions
   - `supportedLocales`: List of supported locale codes
   - `templatePath`: Path to the .stg file

5. Implement the template logic in the `.stg` file

6. (Optional) Add a preview image (`preview.png`)

## Supported Locales

The following locales are currently supported:

- `en`: English
- `es`: Spanish

To add a new locale, update the `Locale` enum in `domain/Locale.kt`.

## Validation

The loader validates:

- ✅ Required fields are present in metadata.yaml
- ✅ Template file exists at the specified path
- ✅ Locale codes are valid
- ✅ No duplicate template IDs
- ⚠️ Invalid metadata files are logged and skipped (don't fail the entire load)

## Error Handling

If a template fails to load:

- An error is logged with the file path and reason
- The template is skipped
- Loading continues with other templates
- If NO templates load, the application will fail at startup (throws IllegalStateException)

## Performance Considerations

- Templates are loaded **once** at application startup (lazy singleton)
- Metadata is parsed from YAML during initialization
- In-memory cache of all templates after loading
- O(1) lookup by template ID using List.find()
