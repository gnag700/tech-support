# Tech-Support – DevOps Strategy Document

**Project:** Tech-Support Helpdesk System  
**Author:** Winston (DevOps Architect Agent)  
**Date:** 2025-11-06  
**Version:** 1.0  
**Status:** Ready for Implementation

---

## 1. Purpose & Scope

This document defines the operational model for delivering, deploying, and running the Tech-Support helpdesk platform in production. It complements the architecture document by detailing environments, pipelines, automation, monitoring, backup, and incident response procedures tailored for the on-premise medical facility deployment.

The strategy focuses on:

- Reliable builds and deployments for the modular monolith (Spring Boot 4.0.0-RC2 + React 18)
- Repeatable operations aligned with hospital change control requirements
- Health monitoring, alerting, and recovery procedures that meet the 99.5% uptime target
- Security and compliance expectations for handling medical support data (GDPR)

Out of scope:

- Feature-level functional design (covered in PRD and architecture documents)
- Post-MVP file attachment storage (future enhancement)
- Organization-wide IT policies (handled by hospital IT department)

---

## 2. Goals and Non-Goals

### Goals

- Provide end-to-end CI/CD automation from commit to production release
- Ensure deployments are predictable, auditable, and reversible
- Define environment topology (dev, staging, production) and configuration management
- Package backend and frontend into OCI-compliant images and manage lifecycle through container registries
- Establish monitoring, logging, and alerting baselines before go-live
- Define backup, restore, and disaster recovery procedures for PostgreSQL 17.6 and application configuration
- Integrate security controls (secrets management, vulnerability scanning, access control) into operations

### Non-Goals

- Full infrastructure automation via IaC (tracked on roadmap)
- Auto-scaling or high-availability clustering (single-node deployment is sufficient for current load)
- Enterprise Kubernetes adoption; container orchestration handled via Docker Compose on single-node host for now

---

## 3. Architecture Alignment & Operating Constraints

### 3.1 Solution Overview

- Modular monolith (Spring Boot 4.0.0-RC2 + Spring Modulith 2.0 RC2) packaged into an OCI image via multi-stage Gradle build
- React 18 frontend compiled by Vite and baked into the backend image under `/app/static` for unified deployment
- PostgreSQL 17.6 provided as managed container with persistent volume claims (optionally externalized to dedicated VM if hospital policy requires)
- Deployment footprint: single Ubuntu 24.04 LTS container host (Docker Engine 24.x) running `docker compose` stack with separate volumes for database and application logs
- Infrastructure managed by hospital IT; deployments executed by DevOps team using privileged service account on container host

### 3.2 Operational Constraints

- On-premise only; no outbound deployment to public cloud
- Container runtime restricted to Linux hosts hardened via CIS benchmark (rootless Docker where possible)
- Change windows limited to evenings (22:00–01:00 local time) and weekends for high-risk updates
- Hospital IT policy requires dual-approval for production changes (DevOps + IT lead)
- Audit trails must be retained for minimum 12 months
- 99.5% monthly uptime target (allowable downtime ≈ 3.6 hours/month)
- Recovery Time Objective (RTO): 1 hour, Recovery Point Objective (RPO): 15 minutes

---

## 4. Environment Topology

| Environment | Purpose | Hosting | Data Source | Deployment Trigger | Notes |
|-------------|---------|---------|-------------|--------------------|-------|
| Local Dev | Developer workstations, feature development | Windows/macOS laptops with Docker Desktop / Colima | Local Docker Compose stack (`backend`, `db`, `pgadmin`) | Manual (`docker compose up --build`) | `.env.local` for developer credentials |
| CI | Automated test execution | GitHub Actions (`ubuntu-latest`) with DinD service | Ephemeral PostgreSQL Testcontainers | Pull request events | Runs unit, integration, contract tests, image build, Trivy scan |
| Staging | Pre-production validation | Ubuntu 24.04 LTS VM (Docker Engine 24.x) | Daily anonymized DB snapshot from prod | Manual via pipeline approval | Uses `docker compose -f compose.prod.yml` with staging overrides |
| Production | Live system | Ubuntu 24.04 LTS (bare-metal or VM) container host | Production PostgreSQL volume on RAID storage | Manual, change-controlled | Single-node Docker Compose stack with persistent volumes |

### 4.1 Configuration Management

- Configuration stored in environment-specific `application-*.yml` files and `compose.<env>.yml` overrides; container secrets injected via `.env` files secured with OS ACLs
- Sensitive values (JWT secret, DB credentials, SMTP tokens, Telegram bot token) mounted as Docker secrets (Linux) or environment variables sourced from encrypted vault on deployment
- Configuration drift monitored via nightly checksum comparison (`sha256sum` on `/etc/techsupport`) logged to hospital SIEM

---

## 5. Infrastructure Topology

### 5.1 Network Layout

- Clients (hospital staff) access application via hospital intranet; HTTPS terminated at HAProxy appliance before traffic reaches container host
- Container host and database volume storage reside in same VLAN with restricted firewall rules
- Only container host may connect to PostgreSQL service (port 5432); remote administration requires VPN + bastion jump host
- Outbound internet access limited to OS updates, container registry endpoints, npm, and Maven Central proxies; repository mirror configured to comply with security policies

### 5.2 Server Specifications

| Component | Minimum Specs | Recommended Specs | Notes |
|-----------|---------------|-------------------|-------|
| Container Host | 8 vCPU, 16 GB RAM, 256 GB NVMe | 12 vCPU, 24 GB RAM, 512 GB NVMe | Runs Docker Engine 24.x, tech-support stack, monitoring agents |
| Persistent Storage | 500 GB RAID1 SSD | 1 TB RAID10 SSD | Mounted at `/var/lib/docker` with separate `/var/lib/postgresql/data` bind mount |
| Backup Target | N/A | 2 TB network-attached storage | Receives pg_dump, WAL archives, configuration snapshots |

### 5.3 Dependencies

- Docker Engine 24.x (rootless mode where supported) with Compose v2 plugin
- Java 21 Temurin runtime (for CI build containers only)
- Node.js 20 LTS and npm (CI build containers)
- Gradle Wrapper committed in repository (invoked within builder image)
- Bash and PowerShell 7 for cross-platform automation scripts (deployment host uses Bash; Windows operators have PowerShell equivalents)

### 5.4 Secrets Management

- Production secrets stored in HashiCorp Vault (or hospital-sanctioned secret store) with one-time retrieval during deployment to populate `.env.prod`
- Staging secrets maintained in separate namespace with restricted access (DevOps + QA leads)
- Secrets rotated quarterly or immediately upon personnel changes; CI uses short-lived OpenID Connect tokens to fetch deployment credentials
- No secrets stored in source control; CI environment variables masked and scoped to deploy job only

---

## 6. Build & Packaging Strategy

### 6.1 Multi-Stage Container Build

- Single Dockerfile builds frontend and backend artefacts and outputs hardened runtime image (~300 MB)
- Version tagging format: `registry.crb.local/tech-support/app:YYYY.MM.DD-buildNumber`
- Base image: `eclipse-temurin:21-jre` (Ubuntu Jammy) hardened with non-root user `app`
- Build executed via `docker buildx bake --set *.tags=...`
- Flyway migrations included in image and executed on startup; `SPRING_PROFILES_ACTIVE` set via environment

**Dockerfile (excerpt):**

```dockerfile
# syntax=docker/dockerfile:1.7
FROM node:20.11 AS frontend-builder
WORKDIR /usr/src/frontend
COPY tech-support-frontend/package*.json ./
RUN npm ci
COPY tech-support-frontend/ ./
RUN npm run build

FROM gradle:8.11.0-jdk21 AS backend-builder
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle tech-support-backend/ ./
RUN ./gradlew clean bootJar

FROM eclipse-temurin:21.0.2_13-jre
RUN useradd --system --create-home --uid 1001 app
WORKDIR /app
COPY --from=backend-builder /home/gradle/project/build/libs/tech-support-*.jar ./app.jar
COPY --from=frontend-builder /usr/src/frontend/dist ./static
EXPOSE 8080
USER app
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}"]
```

### 6.2 Local Container Stack

- Development stack managed through `docker compose -f compose.dev.yml`
- Services: `app`, `db`, `mailhog`, `prometheus`, `grafana`
- Volume bindings for hot-reload: mount local source into builder container for rapid feedback (`./tech-support-backend:/workspace` with Gradle daemon)
- Local profile uses `SPRING_PROFILES_ACTIVE=dev` and disables secure cookies for JWT
- UID/GID mappings configured via `user: "${LOCAL_UID}:${LOCAL_GID}"` to prevent permission drift on host filesystem

**compose.dev.yml (excerpt):**

```yaml
services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
      target: backend-builder
    command: ./gradlew bootRun
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/techsupport
      SPRING_PROFILES_ACTIVE: dev
  db:
    image: postgres:17.6
    environment:
      POSTGRES_DB: techsupport
      POSTGRES_USER: techsupport
      POSTGRES_PASSWORD: techsupport
    volumes:
      - db-data:/var/lib/postgresql/data
volumes:
  db-data:
```

### 6.3 Container Registry

- Primary registry: `registry.crb.local` (Harbor) with project `tech-support`
- Images signed with cosign and vulnerability scanned (Trivy) before promotion
- Retention policy keeps last 10 successful builds plus tagged releases
- Offline export available via `docker save` for air-gapped recovery scenarios

### 6.4 Dependency Management

- Dependabot enabled for Gradle and npm to surface security patches
- `npm ci` and Gradle dependency locking executed inside build containers for reproducibility
- Docker build cache persisted using BuildKit remote cache on `cache.crb.local`

---

## 7. CI/CD Pipeline Design

### 7.1 Pipeline Overview

```text
Commit → Lint & Unit Tests → Integration Tests → Contract Tests → Build Artifacts → Security Scans → Publish → Staging Deploy → Smoke Tests → Prod Approval → Production Deploy → Post-Deploy Validation
```

### 7.2 GitHub Actions Workflow (High-Level)

- **Workflow:** `.github/workflows/ci.yml`
- **Triggers:** `push` to main, `pull_request`
- **Jobs:**
  - `lint`: Run `npm run lint` and `./gradlew spotlessCheck`
  - `unit-tests`: `./gradlew test --tests '*Test'`
  - `integration-tests`: `./gradlew test --tests '*IntegrationTest'`
  - `contract-tests`: `./gradlew pactVerify`
  - `build-image`: depends on tests; runs `docker buildx build --push` targeting staging registry namespace
  - `security-scan`: executes `./gradlew dependencyCheckAnalyze`, `npm audit --omit=dev`, and `trivy image`
  - `publish`: promotes image tag from staging to production repository after manual approval

### 7.3 Deployment Pipelines

- **Staging Deployment (manual approval):**
  - Triggered via workflow dispatch with selected image tag (`registry.crb.local/tech-support/app:<tag>`)
  - Steps: `docker compose pull`, `docker compose up -d --remove-orphans`, run smoke tests via dedicated job container, capture compose logs
- **Production Deployment (manual, change-controlled):**
  - Requires approvals from DevOps lead and hospital IT manager
  - Service window scheduled via ticketing system (minimum 24h notice)
  - Deployment executed using signed Bash script under service account (`deploy.sh <tag>`), includes DB backup, `docker compose pull`, `docker compose up -d`, post-deploy verification

### 7.4 Branching Strategy

- Trunk-based development with short-lived feature branches
- `main` branch protected; requires PR review + passing CI
- Release tags applied post-production deployment (`v2025.11.06`) for audit traceability
- Hotfixes branch off latest tag, merged back via PR after validation

---

## 8. Deployment Automation

### 8.1 Deployment Assets

- Container image: `registry.crb.local/tech-support/app:<tag>`
- Compose files: `compose.prod.yml` (base) + `compose.prod.secrets.env` (mounted via `.env`)
- Deployment scripts: `deploy.sh`, `rollback.sh`, `smoke-test.sh`
- Checksums (SHA-256) and cosign signatures stored alongside release metadata (`releases/2025-11-06.json`)

**compose.prod.yml (excerpt):**

```yaml
services:
  app:
    image: ${TECHSUPPORT_IMAGE}
    restart: unless-stopped
    env_file:
      - compose.prod.secrets.env
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SERVER_PORT: 8080
    ports:
      - "8443:8080"
    volumes:
      - app-logs:/var/log/app
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 5s
      retries: 3
    networks:
      - default
      - monitoring
  db:
    image: postgres:17.6
    restart: unless-stopped
    environment:
      POSTGRES_DB: techsupport
      POSTGRES_USER: techsupport
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - db-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U techsupport"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  app-logs:
  db-data:

networks:
  monitoring:
    external: true
```

### 8.2 Deployment Script (Simplified)

```bash
#!/usr/bin/env bash
set -euo pipefail

TAG="$1"
echo "Deploying Tech-Support tag ${TAG}"

# 1. Backup database
pg_dump --dbname="$PG_URL" --file="/backups/techsupport-$(date +%F-%H%M).sql"

# 2. Pull latest images
docker compose -f compose.prod.yml pull app

# 3. Update tag and apply stack
export TECHSUPPORT_IMAGE="registry.crb.local/tech-support/app:${TAG}"
docker compose -f compose.prod.yml up -d --remove-orphans

# 4. Run smoke tests
./scripts/smoke-test.sh

echo "Deployment successful"
```

### 8.3 Database Migrations

- Flyway executes automatically when container starts; pending migrations checked via `docker compose run --rm app ./gradlew flywayInfo`
- Pre-deployment pipeline step fails build if new migrations were not applied in staging
- Rollback handled via restoration from pre-deployment backup (see Section 10)

### 8.4 Rollback Strategy

- Maintain last three promoted image tags (`release`, `previous`, `fallback`) in registry
- Rollback procedure:
  1. Stop stack: `docker compose -f compose.prod.yml down --timeout 30`
  2. Restore DB backup from pre-deployment snapshot
  3. Redeploy using previous tag: `deploy.sh previous-tag`
  4. Run smoke tests and postmortem review

### 8.5 Downtime Management

- Deployments occur during predefined change windows
- Expected downtime per deployment: <2 minutes (container restart with health checks)
- Future enhancement: explore blue-green deployment using dual compose projects to achieve near-zero downtime

---

## 9. Observability & Monitoring

### 9.1 Logging

- Logback writes JSON to `/var/log/app/application.log`; Docker logging driver rotates files (`max-size=50m`, `max-file=5`)
- Filebeat container tails `/var/log/app` volume and ships to centralized ELK stack
- Sensitive data (PII) redacted by `PiiRedactionFilter` before serialization

### 9.2 Metrics & Health

- Spring Boot Actuator exposed on `/actuator` (secured via basic auth + IP allowlist) and mounted to `monitoring` overlay network
- Prometheus container scrapes metrics every 30 seconds; key metrics:
  - `http_server_requests_seconds` (latency SLIs)
  - `hikaricp_connections_active`
  - `spring_modulith_event_publication_pending`
  - Custom metric `techsupport_failed_logins_total`
- Grafana dashboards: response times, error rates, DB connection usage, container resource consumption, event outbox backlog

### 9.3 Alerting

- Alert thresholds:
  - Error rate >5% for 5 minutes → PagerDuty (Sev2)
  - Hikari pool usage >80% for 10 minutes → Ops channel
  - Pending outbox events >100 or oldest event >1h → Ops channel
  - Disk usage >80% → IT infrastructure queue
- On-call rota shared between DevOps and hospital IT (weekly rotation)

### 9.4 Synthetic Monitoring

- Scheduled cron job (every 5 minutes) performs login + ticket list fetch using service account; failures trigger incident ticket

---

## 10. Backup & Disaster Recovery

### 10.1 Database Backups

- Nightly full backups using `pg_dump` (retention: 14 daily + 12 monthly)
- WAL archiving to secondary storage for point-in-time recovery (retention: 7 days)
- Backups encrypted at rest (AES-256) before upload to secure network share
- Quarterly restore drills executed to validate backup integrity

### 10.2 Application Configuration

- `application-prod.yml` and deployment scripts versioned in Git (without secrets)
- Secrets backup stored in encrypted vault (LUKS-protected volume mounted on backup server)
- Documented restore procedure for configuration files

### 10.3 Disaster Recovery Procedure

1. Assess outage severity (Sev1 if production unavailable >15 minutes)
2. Initiate incident bridge (Teams) and assign incident commander
3. Restore latest clean DB backup (ensure WAL replay to meet RPO)
4. Redeploy latest stable application image (`deploy.sh <previous-tag>`)
5. Execute smoke tests and close incident with root cause analysis

### 10.4 RPO/RTO Alignment

- RPO 15 minutes achieved via WAL archiving frequency
- RTO 1 hour achieved through automated deployment scripts and runbooks

---

## 11. Security Operations Integration

### 11.1 Secrets & Certificates

- TLS certificates issued by hospital internal CA; renewal tracked in DevOps calendar (60-day reminder)
- JWT secret rotated every 6 months; rotation procedure includes dual-secret rollout (accept old + new) for 24 hours
- SMTP and Telegram tokens stored with limited permissions; usage audited quarterly

### 11.2 Vulnerability Management

- Dependency scans executed in CI (`dependencyCheckAnalyze`, `npm audit`, `trivy image`)
- Monthly `nmap` and `OpenVAS` scans scheduled by hospital security team
- Critical vulnerabilities patched within 48 hours; high severity within 7 days

### 11.3 Access Control

- Production deployment rights limited to DevOps team (2 engineers) and IT manager
- Audit logs for deployment scripts captured via `journalctl -u techsupport-deploy` and forwarded to SIEM
- GitHub repository uses SSO + MFA; admin rights restricted

---

## 12. Runbooks & Operational Procedures

### 12.1 Routine Tasks

- Daily: Check Grafana dashboards, review overnight alerts, verify backups completed, confirm `docker ps` reports healthy containers
- Weekly: Review pending event publications, rotate API keys expiring within 30 days, analyze failed login trends
- Monthly: Patch Ubuntu container host (`apt-get upgrade`), rotate Docker Engine if security updates released, review audit logs

### 12.2 Incident Response

| Severity | Description | Response Time | Actions |
|----------|-------------|---------------|---------|
| Sev1 | Total outage or data loss | Immediate | Incident bridge, restore service, notify stakeholders |
| Sev2 | Degradation affecting >25% users | 15 minutes | Engage DevOps + IT, apply mitigation, monitor |
| Sev3 | Minor degradation or single feature impact | 1 hour | Triage during business hours, schedule fix |

### 12.3 Common Runbooks

- **Restart Service:** `docker compose -f compose.prod.yml restart app`, confirm `/actuator/health` returns `UP`
- **Clear Event Outbox:** `docker compose -f compose.prod.yml exec db psql -f scripts/archive_outbox.sql` after root cause analysis
- **Rotate JWT Secret:** Update Vault secret, regenerate `compose.prod.secrets.env`, redeploy with dual-secret profile, remove old secret after 24 hours
- **Restore Backup:** Follow Section 10.3, document root cause, update incident postmortem

---

## 13. Tooling & Responsibilities

| Area | Tooling | Owner |
|------|---------|-------|
| CI/CD | GitHub Actions, Bash deploy scripts, Cosign | DevOps Team |
| Monitoring | Prometheus, Grafana, PagerDuty | DevOps + IT |
| Logging | Logback, Filebeat, ELK Stack | IT Operations |
| Backups | PostgreSQL pg_dump, WAL archiving, Velero (optional) | DBA Team |
| Security | DependencyCheck, Trivy, MFA, Access reviews | Security Officer |

Communication cadence: weekly sync between DevOps, IT, and Security to review operational metrics and upcoming releases.

---

## 14. Roadmap & Continuous Improvement

- Implement Infrastructure as Code (Terraform + Ansible) to fully codify container host provisioning (Q1 2026)
- Evaluate lightweight orchestration upgrade (k3s or HashiCorp Nomad) once load exceeds single-node Compose capacity
- Integrate automated performance regression testing (Gatling) into pipeline
- Enhance deployment pipeline with blue-green strategy to eliminate downtime
- Implement centralized secrets management (HashiCorp Vault) replacing `.env`-based secret injection
- Extend monitoring with synthetic API tests and rum (real user monitoring)

---

## 15. Appendices

### 15.1 Environment Variables (Production)

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | JDBC connection string | `jdbc:postgresql://db-prod:5432/techsupport` |
| `SPRING_DATASOURCE_USERNAME` | DB user | `techsupport_app` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | Stored in credential vault |
| `JWT_SECRET` | HMAC secret for JWT | 64-char base64 key |
| `SMTP_HOST` | Mail relay host | `smtp.crb.local` |
| `TELEGRAM_BOT_TOKEN` | Bot token for notifications | Stored in credential vault |
| `LOGGING_LEVEL_ROOT` | Log level override | `INFO` |
| `TECHSUPPORT_IMAGE` | Deployed container image tag | `registry.crb.local/tech-support/app:2025.11.06-002` |
| `POSTGRES_PASSWORD` | Database password injected into db container | Loaded from Vault during deploy |
| `PG_URL` | Connection string used for deployment-time backups | `postgresql://techsupport_app:***@db:5432/techsupport` |

### 15.2 Smoke Test Checklist

- Login with admin account
- Create, assign, and resolve a ticket
- Verify audit log entry created
- Confirm notification (email/Telegram) delivered in staging
- Check `/actuator/health` returns `UP`

### 15.3 References

- Architecture Document: `docs/architecture-2025-11-06.md`
- PRD: `docs/PRD.md`
- Validation Report: `docs/validation-report-2025-11-05.md`
- Security Policy Handbook (Hospital IT) – internal repository

---

**Document Status:** Ready for implementation. Review with DevOps, IT, and Security stakeholders before first deployment. Update post-implementation to capture lessons learned and refined runbooks.

