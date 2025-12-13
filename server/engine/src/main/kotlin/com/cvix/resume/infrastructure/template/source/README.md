# Template Source Repositories

This document explains how to load resume templates using different template sources.

## Available Template Sources

### 1. ClasspathTemplateSourceRepository (Default)

Loads templates from the application's classpath (bundled inside the JAR/WAR).

**Use Case:**

- Templates bundled with the application
- Production deployment with immutable templates
- No external filesystem access needed

**Location:**

- `src/main/resources/templates/resume/`

**Example Structure:**

```
src/main/resources/templates/resume/
├── engineering/
│   ├── engineering.stg
│   └── metadata.yaml
├── marketing/
│   ├── marketing.stg
│   └── metadata.yaml
└── minimal/
    ├── minimal.stg
    └── metadata.yaml
```

**Configuration:**
No configuration needed. Templates are automatically discovered at startup.

**Pros:**

- ✅ Templates bundled with application
- ✅ No external dependencies
- ✅ Fast loading (from classpath)
- ✅ Immutable (safe for production)

**Cons:**

- ❌ Requires rebuild to update templates
- ❌ Cannot add templates at runtime

---

### 2. FilesystemTemplateSourceRepository

Loads templates from an external filesystem directory.

**Use Case:**

- Dynamic template updates without rebuild
- External template management
- Development/testing with frequent changes
- Multi-tenant with custom templates per tenant

**Configuration:**

```yaml
# application.yml
resume:
    template:
        source:
            path: /path/to/templates  # Absolute path
            # OR
            path: templates/resume     # Relative to working directory
```

**Environment Variable:**

```bash
export TEMPLATE_SOURCE_PATH=/opt/app/templates
```

**Example Structure:**

```
/opt/app/templates/
├── engineering/
│   ├── engineering.stg
│   └── metadata.yaml
├── marketing/
│   ├── marketing.stg
│   └── metadata.yaml
└── custom-tenant-123/
    ├── custom.stg
    └── metadata.yaml
```

**Pros:**

- ✅ Update templates without rebuild
- ✅ Add/remove templates at runtime
- ✅ Different templates per environment
- ✅ Easy to manage externally

**Cons:**

- ❌ Requires filesystem access
- ❌ Security concerns (filesystem permissions)
- ❌ Need to sync templates across instances

---

## Choosing a Template Source

### Use ClasspathTemplateSourceRepository when:

- Templates are stable and rarely change
- You want templates bundled in the application
- Security is a priority (immutable)
- Simple deployment (single JAR)

### Use FilesystemTemplateSourceRepository when:

- Templates change frequently
- You need to update templates without rebuild
- Different environments have different templates
- Multi-tenant with custom templates

---

## Switching Between Repositories

Both repositories implement the same `TemplateRepository` interface, so switching is transparent to
the application.

### Option 1: Configuration Class (Recommended)

Create a configuration class to select the repository based on a property:

```kotlin
@Configuration
class TemplateRepositoryConfiguration {

    @Bean
    @ConditionalOnProperty(
        prefix = "resume.template.source",
        name = ["type"],
        havingValue = "filesystem"
    )
    fun filesystemTemplateRepository(
        properties: TemplateSourceProperties,
        loader: TemplateMetadataLoader
    ): TemplateRepository {
        return FilesystemTemplateSourceRepository(properties, loader)
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "resume.template.source",
        name = ["type"],
        havingValue = "classpath",
        matchIfMissing = true
    )
    fun classpathTemplateRepository(
        resolver: ResourcePatternResolver,
        loader: TemplateMetadataLoader
    ): TemplateRepository {
        return ClasspathTemplateSourceRepository(resolver, loader)
    }
}
```

Then configure in `application.yml`:

```yaml
resume:
    template:
        source:
            type: classpath  # or "filesystem"
            path: templates/resume  # Only used for filesystem
```

### Option 2: Spring Profiles

Use different profiles for different environments:

```yaml
# application.yml
spring:
    profiles:
        active: dev

---
# application-dev.yml
resume:
    template:
        source:
            type: filesystem
            path: /local/dev/templates

---
# application-prod.yml
resume:
    template:
        source:
            type: classpath
```

---

## Template Discovery

Both repositories follow the same discovery pattern:

1. **Scan for `metadata.yaml` files**
    - ClasspathTemplateSourceRepository: `classpath:templates/resume/**/metadata.yaml`
    - FilesystemTemplateSourceRepository: `{path}/**/metadata.yaml`

2. **Parse metadata using `TemplateMetadataLoader`**
    - Type-safe YAML parsing with SnakeYAML
    - Validates required fields (id, name, version, templatePath)
    - Converts locale codes to `Locale` enum

3. **Cache in memory**
    - Lazy initialization on first request
    - Single loading at startup
    - O(1) lookup by template ID

4. **Error handling**
    - Invalid templates are logged and skipped
    - Application continues with remaining templates
    - Empty list if no templates found

---

## Performance Considerations

### ClasspathTemplateSourceRepository

- **Startup Time:** Fast (resources already in memory)
- **Memory Usage:** Low (JAR compression)
- **Lookup Time:** O(1) in-memory lookup
- **Scalability:** Excellent (no I/O)

### FilesystemTemplateSourceRepository

- **Startup Time:** Depends on filesystem speed
- **Memory Usage:** Low (metadata only)
- **Lookup Time:** O(1) in-memory lookup
- **Scalability:** Good (lazy loading, cached)

Both repositories load templates **once** at startup and cache them in memory.

---

## Security Considerations

### ClasspathTemplateSourceRepository

- ✅ Templates are immutable (bundled in JAR)
- ✅ No filesystem access needed
- ✅ Safe for production

### FilesystemTemplateSourceRepository

- ⚠️ Requires filesystem read access
- ⚠️ Path traversal attacks possible (validate paths)
- ⚠️ File permissions must be restricted
- ⚠️ Use absolute paths in production
- ⚠️ Consider read-only filesystem

**Best Practices:**

```bash
# Restrict permissions
chmod 750 /opt/app/templates
chown app-user:app-group /opt/app/templates

# Use read-only filesystem in Docker
docker run -v /templates:/app/templates:ro myapp
```

---

## Troubleshooting

### No templates found (ClasspathTemplateSourceRepository)

```
WARN: No template files found matching pattern: classpath:templates/resume/**/metadata.yaml
```

**Solution:**

- Check that `src/main/resources/templates/resume/` exists
- Verify `metadata.yaml` files are present
- Rebuild the application (`./gradlew build`)

### No templates found (FilesystemTemplateSourceRepository)

```
WARN: Template base path does not exist: /opt/app/templates
```

**Solution:**

- Verify the path exists: `ls -la /opt/app/templates`
- Check filesystem permissions: `stat /opt/app/templates`
- Verify `TEMPLATE_SOURCE_PATH` environment variable
- Check if path is absolute or relative

### Template loading failed

```
WARN: Failed to load template metadata from: engineering/metadata.yaml
```

**Solution:**

- Validate YAML syntax: `yamllint metadata.yaml`
- Check required fields: id, name, version, templatePath
- Verify locale codes are valid (en, es)
- Check template file exists at `templatePath`

---

## Migration Guide

### From InMemoryTemplateRepository to ClasspathTemplateSourceRepository

1. Create `metadata.yaml` for each template
2. Place in `src/main/resources/templates/resume/{category}/`
3. Remove hardcoded metadata from `InMemoryTemplateRepository`
4. Use `@Primary` on `ClasspathTemplateSourceRepository` to override

### From ClasspathTemplateSourceRepository to FilesystemTemplateSourceRepository

1. Copy templates from `src/main/resources/templates/resume/` to filesystem
2. Configure `resume.template.source.path` in application.yml
3. Switch active repository using `@ConditionalOnProperty`
4. Restart application
5. Verify templates loaded: `GET /api/templates`

---

## Example: Multi-Tenant Setup

For multi-tenant applications with custom templates per tenant:

```
/opt/app/templates/
├── default/           # Default templates (fallback)
│   ├── engineering/
│   └── marketing/
├── tenant-acme/       # Tenant-specific templates
│   ├── custom-acme/
│   └── branded-resume/
└── tenant-xyz/
    └── corporate-template/
```

Load tenant-specific templates by appending tenant ID to path:

```kotlin
@Service
class TenantTemplateRepository(
    private val baseProperties: TemplateSourceProperties,
    private val loader: TemplateMetadataLoader
) : TemplateRepository {

    override suspend fun findAll(): List<TemplateMetadata> {
        val tenantId = getCurrentTenantId()
        val tenantPath = "${baseProperties.path}/$tenantId"

        // Load tenant-specific + default templates
        return loadFromPath(tenantPath) + loadFromPath("${baseProperties.path}/default")
    }
}
```

---

## API Endpoints

Both repositories expose templates via the same API:

```bash
# List all templates
GET /api/templates

# Get template by ID
GET /api/templates/{id}

# Check if template exists
HEAD /api/templates/{id}
```

Response example:

```json
{
    "templates": [
        {
            "id": "engineering",
            "name": "Engineering Resume",
            "version": "1.0.0",
            "descriptions": {
                "en": "Professional resume for engineers",
                "es": "Currículum profesional para ingenieros"
            },
            "supportedLocales": [
                "en",
                "es"
            ],
            "templatePath": "classpath:templates/resume/engineering/engineering.stg",
            "previewUrl": "/assets/previews/engineering.png",
            "params": {
                "colorPalette": "blue",
                "fontFamily": "Roboto"
            }
        }
    ]
}
```

