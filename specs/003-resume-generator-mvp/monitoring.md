# Resume Generator Monitoring & SLO Documentation

**Last Updated**: November 3, 2025
**Feature**: Resume Generator MVP
**Spec**: `specs/003-resume-generator-mvp/spec.md`

## Overview

This document describes the monitoring, alerting, and SLO (Service Level Objective) strategy for the Resume Generator MVP feature. It serves as the operator runbook for incident response and performance optimization.

## Service Level Objectives (SLOs)

### 1. API Latency SLO

**Objective**: 95th percentile API response time ≤ 200ms (excluding PDF generation)

**Measurement**:

- Metric: `resume_api_latency_seconds`
- Query: `histogram_quantile(0.95, rate(resume_api_latency_seconds_bucket[5m]))`

**Thresholds**:

- ✅ **Green**: p95 ≤ 200ms
- ⚠️ **Warning**: p95 > 200ms (alert after 5 minutes)
- 🚨 **Critical**: p95 > 250ms (alert after 2 minutes)

**What to Check**:

1. Check Spring Boot Actuator health endpoint: `/actuator/health`
2. Review application logs for slow database queries
3. Check Keycloak response times (OAuth2 validation)
4. Monitor JVM heap usage and GC pauses
5. Review rate limiter configuration

**Remediation**:

- Scale horizontally by adding more application instances
- Review and optimize database queries
- Increase connection pool sizes if needed
- Check for memory leaks or excessive GC

---

### 2. PDF Generation Duration SLO

**Objective**: 95th percentile PDF generation time < 8 seconds

**Measurement**:

- Metric: `resume_pdf_generation_duration_seconds`
- Query: `histogram_quantile(0.95, rate(resume_pdf_generation_duration_seconds_bucket[5m]))`

**Thresholds**:

- ✅ **Green**: p95 < 8s
- ⚠️ **Warning**: p95 ≥ 8s (alert after 5 minutes)
- 🚨 **Critical**: p95 ≥ 10s (approaching timeout, alert after 2 minutes)

**What to Check**:

1. Check Docker daemon health: `/actuator/health` (look for `docker` component)
2. Monitor Docker container pool utilization
3. Review LaTeX compilation errors
4. Check host CPU and memory usage
5. Verify TeX Live Docker image is cached locally

**Remediation**:

- Increase `resume.pdf.docker.maxConcurrentContainers` if pool is saturated
- Pre-pull TeX Live image to avoid pull delays: `docker pull dallay/texlive:2025`
- Increase Docker resource limits (memory/CPU) if host has capacity
- Review LaTeX templates for complexity (nested tables, large images)
- Consider adding a template compilation cache

---

### 3. Error Rate SLO

**Objective**: Error rate < 3% of total requests

**Measurement**:

- Metric: `resume_error_rate` (derived gauge)
- Query: `rate(resume_requests_failure_total[5m]) / rate(resume_requests_total[5m])`

**Thresholds**:

- ✅ **Green**: Error rate < 3%
- ⚠️ **Warning**: Error rate ≥ 3% (alert after 5 minutes)
- 🚨 **Critical**: Error rate ≥ 5% (alert after 2 minutes)

**What to Check**:

1. Review error type breakdown (validation, LaTeX, timeout, Docker, rate limit)
2. Check application logs for stack traces
3. Monitor Docker container failure rate
4. Review recent code deployments
5. Check for infrastructure issues (disk space, network)

**Remediation by Error Type**:

- **Validation errors** (400): Check frontend validation, update schemas
- **LaTeX errors** (422): Review LaTeX templates, check for injection
- **Timeout errors** (504): See PDF Generation SLO remediation
- **Docker errors** (500): Check Docker daemon health, restart if needed
- **Rate limit errors** (429): Expected during traffic spikes, monitor for abuse

---

### 4. Uptime SLO

**Objective**: 99.5% uptime (no more than 3.65 hours downtime per month)

**Measurement**:

- External synthetic monitoring probe (e.g., UptimeRobot, Pingdom, or DataDog Synthetics)
- Probe endpoint: `https://api.cvix.com/health` or `/actuator/health/readiness`
- Probe frequency: Every 60 seconds
- Probe locations: Multiple geographic regions

**Thresholds**:

- ✅ **Green**: Health check returns 200 OK
- 🚨 **Critical**: Health check fails for 2 consecutive probes (120s)

**What to Check**:

1. Check application status: `kubectl get pods` or Docker container status
2. Review load balancer health checks
3. Check database connectivity (R2DBC pool)
4. Verify Keycloak availability
5. Check Docker daemon health
6. Review infrastructure metrics (CPU, memory, disk)

**Remediation**:

- Restart application if unresponsive
- Check database connection pool exhaustion
- Verify Keycloak is reachable
- Check for resource exhaustion (disk space, memory)
- Review recent deployments for issues

---

## Metrics Reference

### Request Metrics

| Metric Name               | Type    | Description                      |
| ------------------------- | ------- | -------------------------------- |
| `resume_requests_total`   | Counter | Total resume generation requests |
| `resume_requests_success` | Counter | Successful requests (200 OK)     |
| `resume_requests_failure` | Counter | Failed requests (4xx, 5xx)       |
| `resume_error_rate`       | Gauge   | Error rate (failures / total)    |

### Latency Metrics

| Metric Name                      | Type  | Description                              |
| -------------------------------- | ----- | ---------------------------------------- |
| `resume_api_latency`             | Timer | API endpoint latency (excludes PDF gen)  |
| `resume_pdf_generation_duration` | Timer | PDF generation duration (LaTeX + Docker) |

### Error Type Metrics

| Metric Name                | Type    | Description                    |
| -------------------------- | ------- | ------------------------------ |
| `resume_errors_validation` | Counter | Validation errors (400)        |
| `resume_errors_latex`      | Counter | LaTeX compilation errors (422) |
| `resume_errors_timeout`    | Counter | Timeout errors (504)           |
| `resume_errors_docker`     | Counter | Docker execution errors (500)  |
| `resume_errors_ratelimit`  | Counter | Rate limit errors (429)        |

### Docker Container Metrics

| Metric Name                             | Type    | Description                       |
| --------------------------------------- | ------- | --------------------------------- |
| `docker_container_created`              | Counter | Containers created                |
| `docker_container_started`              | Counter | Containers started                |
| `docker_container_completed`            | Counter | Containers completed successfully |
| `docker_container_failed`               | Counter | Containers failed (exit code ≠ 0) |
| `docker_container_timeout`              | Counter | Containers timed out              |
| `docker_container_cleanup`              | Counter | Containers cleaned up             |
| `docker_container_concurrent_active`    | Gauge   | Active containers                 |
| `docker_container_concurrent_available` | Gauge   | Available container slots         |
| `docker_container_execution_duration`   | Timer   | Container execution time          |

---

## Health Checks

### Spring Boot Actuator Endpoints

**Base URL**: `/actuator`

#### 1. Health Endpoint

**URL**: `/actuator/health`

**Response** (UP):

```json
{
  "status": "UP",
  "components": {
    "docker": {
      "status": "UP",
      "details": {
        "docker.version": "24.0.7",
        "docker.apiVersion": "1.43",
        "docker.os": "linux",
        "docker.arch": "amd64",
        "texlive.image": "ghcr.io/dallay/texlive",
        "concurrent.max": 10,
        "timeout.seconds": 30
      }
    },
    "ping": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

**Response** (DOWN):

```json
{
  "status": "DOWN",
  "components": {
    "docker": {
      "status": "DOWN",
      "details": {
        "error": "Docker daemon is not accessible",
        "texlive.image": "dallay/texlive:2025"
      }
    }
  }
}
```

#### 2. Prometheus Metrics Endpoint

**URL**: `/actuator/prometheus`

**Purpose**: Exposes all application metrics in Prometheus format for scraping.

---

## Alerting Rules

All alerting rules are defined in `/infra/prometheus/resume-generator-alerts.yml`.

### Critical Alerts (Require Immediate Action)

1. **ResumeApiLatencyCritical**: p95 API latency > 250ms
2. **ResumePdfGenerationTimeout**: p95 PDF generation ≥ 10s
3. **ResumeErrorRateCritical**: Error rate > 5%
4. **ResumeDockerHealthFailed**: Docker daemon not responding

### Warning Alerts (Investigate During Business Hours)

1. **ResumeApiLatencyHigh**: p95 API latency > 200ms
2. **ResumePdfGenerationSlow**: p95 PDF generation > 8s
3. **ResumeErrorRateHigh**: Error rate > 3%
4. **ResumeDockerPoolSaturated**: Container pool > 80% full
5. **ResumeDockerContainerFailureHigh**: High container failure rate
6. **ResumeDockerTimeoutSpike**: Container timeout spike detected
7. **ResumeLatexErrorsHigh**: High LaTeX compilation error rate

---

## Grafana Dashboard

**Location**: `/infra/grafana/dashboards/resume-generator-sla.json`

**Dashboard URL**: `https://grafana.cvix.com/d/resume-generator-sla`

### Panels

1. **API Latency SLO** (p95 ≤ 200ms): Time series graph with SLO threshold line
2. **PDF Generation Duration SLO** (<8s): Time series graph with SLO threshold line
3. **Error Rate SLO** (<3%): Stat panel with color thresholds
4. **Request Rate**: Stat panel showing requests per minute
5. **Docker Container Pool Utilization**: Gauge panel (0-100%)
6. **Success vs Failure Requests**: Time series graph
7. **Error Types Breakdown**: Pie chart
8. **Docker Container Lifecycle Events**: Time series graph
9. **API Latency Percentiles**: Time series graph (p50, p75, p95, p99)
10. **PDF Generation Duration Percentiles**: Time series graph (p50, p75, p95, p99)

---

## External Uptime Monitoring Setup

### Recommended Tools

1. **UptimeRobot** (Free tier available)
2. **Pingdom** (Paid, advanced features)
3. **DataDog Synthetics** (Paid, full observability platform)
4. **Checkly** (Developer-focused, API-first)

### Configuration

**Endpoint**: `https://api.cvix.com/actuator/health/readiness`

**Check Frequency**: 60 seconds

**Probe Locations**: Configure at least 3 geographic locations:

- North America (US East)
- Europe (Frankfurt or London)
- Asia Pacific (Singapore or Tokyo)

**Success Criteria**: HTTP 200 OK, response body contains `"status":"UP"`

**Alert Threshold**: 2 consecutive failures (120 seconds downtime)

**Notification Channels**:

- PagerDuty (for critical incidents)
- Slack #alerts channel
- Email to on-call engineer

### Example UptimeRobot Configuration

```yaml
Monitor Type: HTTP(s)
Friendly Name: CVIX Resume API - Readiness
URL: https://api.cvix.com/actuator/health/readiness
Monitoring Interval: 60 seconds
Monitor Timeout: 30 seconds
HTTP Method: GET
HTTP Authentication: Basic (if required)
Keyword to Check: "UP"
Keyword Type: Exists
Alert Contacts: [on-call-email, slack-webhook, pagerduty-integration]
```

---

## Runbook Links

- [Resume API Latency Troubleshooting](https://github.com/dallay/cvix/wiki/runbooks/resume-api-latency)
- [Resume PDF Generation Troubleshooting](https://github.com/dallay/cvix/wiki/runbooks/resume-pdf-generation)
- [Resume Error Rate Troubleshooting](https://github.com/dallay/cvix/wiki/runbooks/resume-error-rate)
- [Docker Pool Saturation](https://github.com/dallay/cvix/wiki/runbooks/docker-pool-saturation)
- [Docker Health Check Failures](https://github.com/dallay/cvix/wiki/runbooks/docker-health)
- [Docker Container Failures](https://github.com/dallay/cvix/wiki/runbooks/docker-container-failures)
- [Docker Timeout Issues](https://github.com/dallay/cvix/wiki/runbooks/docker-timeouts)
- [LaTeX Compilation Errors](https://github.com/dallay/cvix/wiki/runbooks/latex-errors)

---

## Configuration Reference

### Application Properties

```yaml
# Docker PDF Generator Configuration
resume:
  pdf:
    docker:
      image: dallay/texlive:2025
      maxConcurrentContainers: 10  # Adjust based on host capacity
      timeoutSeconds: 30
      memoryLimitMb: 512
      cpuQuota: 0.5  # 50% of one CPU core

# Management Endpoints
management:
  endpoints:
    web:
      exposure:
        include:
          - health
          - prometheus
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  health:
    docker:
      enabled: true
  prometheus:
    metrics:
      export:
        enabled: true
        step: 60
  metrics:
    distribution:
      percentiles-histogram:
        all: true
      percentiles:
        all: 0, 0.5, 0.75, 0.95, 0.99, 1.0
```

---

## Related Documentation

- [Feature Specification](../../../specs/003-resume-generator-mvp/spec.md)
- [Implementation Plan](../../../specs/003-resume-generator-mvp/plan.md)
- [Quickstart Guide](../../../specs/003-resume-generator-mvp/quickstart.md)
- [API Contracts](../../../specs/003-resume-generator-mvp/contracts/resume-api.yaml)
