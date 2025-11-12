# LaTeX Resume Templates

Este directorio contiene las plantillas LaTeX para generar CVs en diferentes idiomas utilizando StringTemplate 4 (ST4).

## Estructura de archivos

```
templates/resume/
├── .gitignore              # Ignora archivos auxiliares de LaTeX
├── README.md               # Este archivo
├── resume-template-en.st   # Plantilla para CV en inglés
└── resume-template-es.st   # Plantilla para CV en español
```

## Extensión de archivos

Los templates utilizan la extensión `.st` (StringTemplate) en lugar de `.tex` por los siguientes motivos:

1. **Compatibilidad con ST4**: StringTemplate 4 busca automáticamente archivos con extensión `.st`
2. **Claridad**: Indica que son templates, no archivos LaTeX directos
3. **Separación de concerns**: Diferencia entre templates (`.st`) y output final (`.tex`)

## Contenido de los templates

Los templates contienen:
- Sintaxis LaTeX válida para el documento final
- Variables de ST4 usando delimitadores `@...@` (configurado en `StringTemplateConfiguration`)
- Lógica condicional de ST4 para secciones opcionales (`@if(...)@`)
- Iteraciones para listas (`@work:{...}@`)

**Nota**: Usamos `@` como delimitadores en lugar de `$` (el default de ST4) para evitar conflictos con LaTeX, especialmente en modo matemático donde `$` se usa frecuentemente.

## Seguridad

El renderizador (`LatexTemplateRenderer`) implementa las siguientes medidas de seguridad:

1. **Validación de contenido**: Escanea todo el input del usuario para detectar comandos LaTeX peligrosos
2. **Escape automático**: Todos los datos del usuario pasan por `escapeLatex()` antes de ser insertados
3. **Lista negra de comandos**: Bloquea comandos como `\input`, `\write`, `\def`, etc.

## Agregar un nuevo idioma

Para agregar soporte para un nuevo idioma:

1. Crear un nuevo archivo de template: `resume-template-{locale}.st`
   - Ejemplo: `resume-template-fr.st` para francés
2. Copiar una plantilla existente como base
3. Traducir los labels y textos estáticos
4. Ajustar el formato según las convenciones del idioma (fechas, etc.)

## Compilación local (para testing)

Si necesitas compilar un template localmente para verificar el output LaTeX:

```bash
# Generar un archivo .tex desde el template
# (esto normalmente lo hace la aplicación)

# Compilar con pdflatex
pdflatex resume-template-en.st

# Limpiar archivos auxiliares
rm -f *.aux *.log *.out *.fdb_latexmk *.fls *.synctex.gz
```

**Nota**: Los archivos auxiliares generados por LaTeX (`.aux`, `.log`, etc.) están en `.gitignore` y no deben subirse al repositorio.

## Referencias

- [StringTemplate 4 Documentation](https://github.com/antlr/stringtemplate4/blob/master/doc/index.md)
- [ModernCV LaTeX Class](https://www.ctan.org/pkg/moderncv)
- `LatexTemplateRenderer.kt` - Implementación del renderizador
