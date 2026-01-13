# PDF Generation with Docker

This module provides PDF generation from LaTeX source using Docker containers for secure, isolated
execution.

## Configuration

The PDF generation can be configured via application properties under `resume.pdf.docker`:

```yaml
resume:
  pdf:
    docker:
      # Docker image to use (must have pdflatex installed)
      image: ghcr.io/dallay/texlive:2025
      # Maximum number of concurrent PDF generation containers
      max-concurrent-containers: 10
      # Timeout in seconds for PDF generation
      timeout-seconds: 30
      # Memory limit per container in MB
      memory-limit-mb: 512
      # CPU quota (0.5 = 50% of one CPU core)
      cpu-quota: 0.5
```

## Docker Image Requirements

The Docker image must have `pdflatex` installed and available in the system PATH. The default image
`ghcr.io/dallay/texlive:2025` includes:

- pdfTeX 3.141592653-2.6-1.40.28 (TeX Live 2025)
- Full LaTeX distribution with common packages
- Support for UTF-8 and international fonts

### Pulling the Image

Before running the application for the first time, pull the Docker image:

```bash
docker pull ghcr.io/dallay/texlive:2025
```

**Note**: This image is ~3GB in size and may take some time to download.

## How It Works

1. The adapter creates a temporary directory and writes the LaTeX source to `resume.tex`
2. A Docker container is created with:
    - Read-only root filesystem
    - No network access
    - Memory and CPU limits
    - The temp directory mounted at `/work`
3. The container runs `pdflatex` to compile the LaTeX source
4. The generated PDF is read from the temp directory
5. The container and temp directory are cleaned up

## Security Features

- **Container Isolation**: Each PDF generation runs in an isolated container
- **Resource Limits**: Memory and CPU limits prevent resource exhaustion
- **Network Isolation**: Containers have no network access (`--network=none`)
- **Read-only Filesystem**: Containers cannot modify the base image
- **Concurrency Control**: Semaphore limits concurrent container executions
- **Input Validation**: LaTeX source is validated before processing (in `LatexTemplateRenderer`)

## Metrics

The adapter exports the following metrics via Micrometer:

- `docker.container.created` - Total containers created
- `docker.container.started` - Total containers started
- `docker.container.completed` - Total successful completions
- `docker.container.failed` - Total failed containers
- `docker.container.timeout` - Total timeouts
- `docker.container.cleanup` - Total cleanups performed
- `docker.container.execution.duration` - Execution time distribution
- `docker.container.concurrent.active` - Current active containers
- `docker.container.concurrent.available` - Available container slots

## Troubleshooting

### Error: "pdflatex: executable file not found in $PATH"

This error occurs when using a Docker image that doesn't include pdflatex. Solutions:

1. Ensure you're using `ghcr.io/dallay/texlive:2025` (not `latest-minimal`)
2. Pull the correct image: `docker pull ghcr.io/dallay/texlive:2025`
3. Verify pdflatex is available: `docker run --rm ghcr.io/dallay/texlive:2025 which pdflatex`

### Timeout Errors

If you're experiencing timeouts:

1. Increase `timeout-seconds` in the configuration
2. Check Docker container logs for compilation errors
3. Verify the LaTeX source is valid
4. Ensure the Docker daemon has sufficient resources

### High Memory Usage

If containers are using too much memory:

1. Reduce `memory-limit-mb` (minimum ~256MB for simple documents)
2. Reduce `max-concurrent-containers` to limit total memory usage
3. Monitor with `docker stats` during PDF generation

## Environment Variables

You can override configuration via environment variables:

- `PDF_DOCKER_IMAGE` - Docker image to use
- `PDF_MAX_CONCURRENT_CONTAINERS` - Max concurrent containers
- `PDF_TIMEOUT_SECONDS` - Timeout in seconds
- `PDF_MEMORY_LIMIT_MB` - Memory limit in MB
- `PDF_CPU_QUOTA` - CPU quota (0.0-1.0)

Example:

```bash
export PDF_DOCKER_IMAGE=ghcr.io/dallay/texlive:2025
export PDF_TIMEOUT_SECONDS=60
export PDF_MAX_CONCURRENT_CONTAINERS=5
```
